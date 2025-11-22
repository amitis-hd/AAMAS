package edu.tufts.hrilab.llm;

import ai.thinkingrobots.trade.TRADEServiceConstraints;
import edu.tufts.hrilab.diarc.DiarcComponent;
import edu.tufts.hrilab.fol.Factory;
import edu.tufts.hrilab.fol.Predicate;
import edu.tufts.hrilab.fol.Symbol;
import edu.tufts.hrilab.fol.Variable;
import ai.thinkingrobots.trade.TRADE;
import ai.thinkingrobots.trade.TRADEException;
import ai.thinkingrobots.trade.TRADEService;
import com.google.gson.Gson;
import edu.tufts.hrilab.interfaces.NLUInterface;
import edu.tufts.hrilab.slug.common.Utterance;
import edu.tufts.hrilab.slug.common.UtteranceType;
import edu.tufts.hrilab.util.Http;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import edu.tufts.hrilab.llm.Prompts;
import edu.tufts.hrilab.llm.Completion;
import ai.thinkingrobots.trade.TRADEServiceInfo;
import ai.thinkingrobots.trade.TRADEServiceConstraints;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import edu.tufts.hrilab.action.annotations.Action;
import edu.tufts.hrilab.action.goal.GoalStatus;
import edu.tufts.hrilab.action.GoalManagerComponent;
import edu.tufts.hrilab.action.justification.Justification;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tufts.hrilab.fol.Term;
import edu.tufts.hrilab.boxbot.actions.Active;
import edu.tufts.hrilab.llm.ObservationComparator;
import edu.tufts.hrilab.llm.openai.response.OpenaiResponses;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * LLMGoalParser: Main framework component implementing Algorithm 1
 * Integrates GPT-4o-mini with DIARC for creative problem-solving through:
 * - Goal verification (Node 1)
 * - Queue management for hypothesis exploration (Node 2)
 * - Creative reasoning with divergent thinking (Node 3)
 * - Action-grounded planning (Node 4)
 * - Action validation (Node 5)
 * - Action execution and observation (Node 6)
 * - Failure diagnosis (Node 7)
 * - Observational learning (Node 8)
 * - Effect validation (Node 9)
 */
public class LLMGoalParser extends DiarcComponent {

    // Prompt template file paths
    private Path planningPromptPath = Paths.get("/mnt/c/Users/hamid/Desktop/myboxbot/diarc/core/src/main/resources/config/edu/tufts/hrilab/llm/inputs/planningPrompt.txt");
    private Path observationPromptPath = Paths.get("/mnt/c/Users/hamid/Desktop/myboxbot/diarc/core/src/main/resources/config/edu/tufts/hrilab/llm/inputs/observationPrompt.txt");
    private Path guessingPromptPath = Paths.get("/mnt/c/Users/hamid/Desktop/myboxbot/diarc/core/src/main/resources/config/edu/tufts/hrilab/llm/inputs/guessingPrompt.txt");
    private Path validationPromptPath = Paths.get("/mnt/c/Users/hamid/Desktop/myboxbot/diarc/core/src/main/resources/config/edu/tufts/hrilab/llm/inputs/validationPrompt.txt");
    private Path divergantPromptPath = Paths.get("/mnt/c/Users/hamid/Desktop/myboxbot/diarc/core/src/main/resources/config/edu/tufts/hrilab/llm/inputs/divergantPrompt.txt");

    // Agent knowledge base file paths
    private Path aslfilePath = Paths.get("/mnt/c/Users/hamid/Desktop/myboxbot/diarc/core/src/main/resources/config/edu/tufts/hrilab/action/asl/domains/boxbot.asl");
    private Path belieffilePath = Paths.get("/mnt/c/Users/hamid/Desktop/myboxbot/diarc/core/src/main/resources/config/edu/tufts/hrilab/belief/agents/boxbot.pl");

    // Cached file contents
    String aslContents = "";
    String beliefContents = "";
    String promptTemplate = "";
    String observationTemplate = "";
    String guessingTemplate = "";
    String validationTemplate = "";
    String divergantPromptTemplate = "";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String user_id = null;

    /**
     * Algorithm 1, Line 1: Exploration queue (Qe) - probability-ordered hypotheses
     */
    private Deque<String> explorationQueue = new LinkedList<>();

    /**
     * Algorithm 1, Line 1: Clue queue (Qc) - current active hypothesis stack
     */
    private Deque<String> clueQueue = new LinkedList<>();

    public LLMGoalParser() {
        super();
    }

    /**
     * Node 5 (Algorithm 1, Line 21): Validates action existence and preconditions
     * Checks: (1) Action exists in ASL, (2) Argument count matches, (3) Preconditions satisfied
     * @param actionLine Action to validate (e.g., "open(door)")
     * @param aslContents ASL file contents
     * @param failureReason Output parameter for failure explanation
     * @return true if action is valid and executable
     */
    private boolean validateAction(String actionLine, String aslContents, StringBuilder failureReason) {
        Pattern pat = Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?:\\((.*)\\))?$");
        Matcher m = pat.matcher(actionLine.trim());

        if (!m.matches()) {
            String issue = "Skipping unparsable action: " + actionLine;
            failureReason.append(issue);
            return false;
        }

        String actionName = m.group(1);
        String inputArgsStr = m.group(2) != null ? m.group(2) : "";

        List<String> inputArgs = inputArgsStr.isEmpty() ?
                new ArrayList<>() :
                Arrays.asList(inputArgsStr.split("\\s*,\\s*"));

        // Extract action signature from ASL
        String actionStartRegex = "\\(\\)\\s*=\\s*" + Pattern.quote(actionName) + "\\[.*?\\]\\(([^)]*)\\)\\s*\\{";
        Pattern actionStartPattern = Pattern.compile(actionStartRegex);
        Matcher actionStartMatcher = actionStartPattern.matcher(aslContents);

        if (!actionStartMatcher.find()) {
            String issue = "Action \"" + actionName + "\" does not exist in ASL.";
            log.warn(issue);
            failureReason.append(issue);
            return false;
        }

        String aslParamsStr = actionStartMatcher.group(1).trim();

        // Find action body by matching braces
        int actionStart = actionStartMatcher.end();
        int braceCount = 1;
        int actionEnd = actionStart;

        while (actionEnd < aslContents.length() && braceCount > 0) {
            char c = aslContents.charAt(actionEnd);
            if (c == '{') braceCount++;
            else if (c == '}') braceCount--;
            actionEnd++;
        }

        if (braceCount != 0) {
            String issue = "Could not find matching closing brace for action \"" + actionName + "\".";
            failureReason.append(issue);
            return false;
        }

        String actionBody = aslContents.substring(actionStart, actionEnd - 1);

        // Extract formal parameter names
        List<String> aslParamNames = new ArrayList<>();
        Pattern paramNamePattern = Pattern.compile("\\?([a-zA-Z_][a-zA-Z0-9_]*)");
        Matcher paramNameMatcher = paramNamePattern.matcher(aslParamsStr);
        while (paramNameMatcher.find()) {
            aslParamNames.add(paramNameMatcher.group(1));
        }

        // Validate argument count
        if (inputArgs.size() != aslParamNames.size()) {
            String issue = String.format("Action '%s' expects %d arguments, but received %d.",
                    actionName, aslParamNames.size(), inputArgs.size());
            failureReason.append(issue);
            return false;
        }

        // Create parameter name to index mapping
        Map<String, Integer> argName_to_IndexMap = new HashMap<>();
        for (int i = 0; i < aslParamNames.size(); i++) {
            argName_to_IndexMap.put(aslParamNames.get(i), i);
        }

        // Check all preconditions
        Pattern conditionsPattern = Pattern.compile("(?i)conditions\\s*:\\s*\\{([\\s\\S]*?)\\}", Pattern.DOTALL);
        Matcher conditionsMatcher = conditionsPattern.matcher(actionBody);

        if (conditionsMatcher.find()) {
            String conditionsBlock = conditionsMatcher.group(1);
            Pattern prePattern = Pattern.compile("pre\\s*:\\s*([^;]+);");
            Matcher preMatcher = prePattern.matcher(conditionsBlock);

            while (preMatcher.find()) {
                String rawCondition = preMatcher.group(1).trim();
                boolean negated = rawCondition.startsWith("~");
                String condition = negated ? rawCondition.substring(1).trim() : rawCondition;

                Pattern condPattern = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?:\\((.*)\\))?");
                Matcher condMatcher = condPattern.matcher(condition);

                if (!condMatcher.matches()) {
                    String issue = "Could not parse precondition: " + condition;
                    log.warn(issue);
                    failureReason.append(issue);
                    return false;
                }

                String conditionName = condMatcher.group(1);
                String condArgsStr = condMatcher.group(2) != null ? condMatcher.group(2).trim() : "";

                // Resolve precondition arguments
                List<Symbol> resolvedArgs = new ArrayList<>();
                if (!condArgsStr.isEmpty()) {
                    for (String arg : condArgsStr.split("\\s*,\\s*")) {
                        if (arg.startsWith("?")) {
                            String varName = arg.substring(1);
                            Integer index = argName_to_IndexMap.get(varName);

                            if (index == null) {
                                String issue = "Unresolved variable '" + arg + "' in precondition '" + rawCondition + "'";
                                failureReason.append(issue);
                                return false;
                            }

                            String resolvedValue = inputArgs.get(index);
                            resolvedArgs.add(new Symbol(resolvedValue));
                        } else {
                            resolvedArgs.add(new Symbol(arg));
                        }
                    }
                }

                Term trade_term = new Term(conditionName, resolvedArgs);

                // Verify precondition via TRADE service call
                try {
                    log.info("Checking precondition '{}' with args: {}", conditionName, resolvedArgs);

                    List<?> result = TRADE.getAvailableService(
                            new TRADEServiceConstraints().name(conditionName).argTypes(Term.class)
                    ).call(List.class, trade_term);

                    boolean holds = !result.isEmpty();
                    if (negated) {
                        holds = !holds;
                    }

                    if (!holds) {
                        String issue = "Action " + actionName + " fails because precondition '" + rawCondition + 
                                     "' is not satisfied. (~condition means condition must be false)";
                        log.warn(issue);
                        failureReason.append(issue);
                        return false;
                    }
                } catch (TRADEException e) {
                    String issue = "TRADE service failed while checking '" + rawCondition + "': " + e.getMessage();
                    failureReason.append(issue);
                    return false;
                }
            }
        }

        log.info("Action {} is valid.", actionName);
        return true;
    }

  

    /**
     * Node 6 (Algorithm 1, Line 25): Retrieves current world state via TRADE service
     * @return JSON string of current observations
     */
    public String getObservation() {
        String obs = "";
        try {
            obs = TRADE.getAvailableService(new TRADEServiceConstraints()
                    .name("getObservationJson")
                    .argTypes())
                    .call(String.class);
            
            JsonNode rootNode = objectMapper.readTree(obs);
            JsonNode observationNode = rootNode.get("observation");
            obs = observationNode.toString();
        } catch (TRADEException | Exception e) {
            log.error("Failed to obtain observation", e);
            return null;
        }
        return obs;
    }

    /**
     * Main creative problem-solving loop implementing Algorithm 1
     * Called by DIARC goal manager when user submits a task
     * @param inpututterance User's natural language goal
     */
    @TRADEService
    @Action
    public void parseGoals(Utterance inpututterance) {
        String originalGoal = inpututterance.getWordsAsString();
        log.info("User goal received: {}", originalGoal);
        
        // Load prompts and knowledge base files
        try {
            aslContents = Files.readString(aslfilePath);
            beliefContents = Files.readString(belieffilePath);
            promptTemplate = Files.readString(planningPromptPath);
            observationTemplate = Files.readString(observationPromptPath);
            guessingTemplate = Files.readString(guessingPromptPath);
            validationTemplate = Files.readString(validationPromptPath);
            divergantPromptTemplate = Files.readString(divergantPromptPath);
        } catch (IOException e) {
            log.error("Error reading input files for LLM prompt.", e);
        }

        // Get DIARC goal submission service
        TRADEServiceInfo submitGoalSvc;
        try {
            submitGoalSvc = TRADE.getAvailableService(
                    new TRADEServiceConstraints()
                            .name("submitGoal")
                            .argTypes(Predicate.class)
            );
        } catch (TRADEException e) {
            log.error("Failed to obtain submitGoal service", e);
            return;
        }

        Pattern pat = Pattern.compile("^(\\w+)\\(([^)]*)\\)$");

        // Algorithm 1, Line 1: Initialize state variables
        String obs = getObservation();
        List<String> actions = new ArrayList<>();
        int actionIndex = -1;
        String new_knowledge = "The agent's location field format: room-obj1-obj2... " +
                              "(room is overall location, obj are nearby reachable items)";
        boolean cluePopped = false;
        boolean clueAdded = false;
        String additional_prompt = "";
        String divAnswer = "";
        String finalCheck = "";

        // Algorithm 1, Line 2: Main problem-solving loop
        while (true) {
            
            // Algorithm 1, Lines 3-9: Goal verification after plan completion
            if (actionIndex == actions.size()) {
                log.info("Plan execution finished. Checking goal achievement.");
                obs = getObservation();

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                try {
                    // Node 1: Goal verification
                    OpenaiResponses response = TRADE.getAvailableService(new TRADEServiceConstraints()
                                    .name("responses")
                                    .argTypes(String.class, String.class, String.class))
                            .call(OpenaiResponses.class, "o4-mini-2025-04-16", 
                                "Goal: " + originalGoal + ". Check if ALL aspects achieved or impossible. " +
                                "Current observations: " + obs + ". Check conversation history for non-observable effects. " +
                                "If incomplete, explain what remains. Otherwise return DONE.", 
                                this.user_id);

                    this.user_id = response.id;

                    if (response.output != null && !response.output.isEmpty()) {
                        for (OpenaiResponses.Output outputItem : response.output) {
                            if ("message".equals(outputItem.type) && outputItem.content != null && !outputItem.content.isEmpty()) {
                                finalCheck = outputItem.content.get(0).text;
                                break;
                            }
                        }
                    }

                    // Algorithm 1, Lines 5-6: Exit if goal achieved
                    if (finalCheck.isEmpty() || finalCheck.trim().equalsIgnoreCase("DONE")) {
                        log.info("Goal achieved successfully!");
                        break;
                    }

                    log.info("Goal incomplete: {}", finalCheck);
                    actionIndex = -1; // Trigger replanning
                } catch (TRADEException e) {
                    log.error("Error in goal verification", e);
                    return;
                }
            }

            // Algorithm 1, Lines 10-15: Planning phase
            if (actionIndex > actions.size() || actionIndex == -1) {
                log.info("PLANNING PHASE");

                String newClue = "";

                // Algorithm 1, Line 11: Queue management (Node 2)
                if (!explorationQueue.isEmpty() && clueQueue.isEmpty()) {
                    newClue = explorationQueue.pollFirst();
                    clueQueue.addLast(newClue);
                    log.info("Popped hypothesis from Qe, added to Qc: '{}'", newClue);
                } else if (cluePopped || clueAdded) {
                    if (explorationQueue.isEmpty()) {
                        log.info("No valid plan exists. Goal impossible.");
                        break;
                    }
                    newClue = explorationQueue.pollFirst();
                    while (newClue.equals("POP CLUE")) {
                        String removed = clueQueue.pollFirst();
                        log.info("Discarding failed hypothesis: {}", removed);
                        newClue = explorationQueue.pollFirst();
                    }
                    clueQueue.addLast(newClue);
                }

                clueAdded = false;
                cluePopped = false;

                log.info("Exploration queue (Qe): {}", explorationQueue);
                log.info("Active hypotheses (Qc): {}", clueQueue);

                // Algorithm 1, Line 12: Construct hypothesis-grounded goal
                String goalForPrompt = originalGoal;
                if (!clueQueue.isEmpty()) {
                    List<String> processed = new ArrayList<>();
                    int size = clueQueue.size();
                    int i = 0;
                    for (String clue : clueQueue) {
                        i++;
                        if (i < size) {
                            String[] parts = clue.split("Workaround subplan", 2);
                            processed.add(parts[0].trim());
                        } else {
                            processed.add(clue.trim());
                        }
                    }
                    String clues = String.join(" and ", processed);
                    goalForPrompt = String.format(
                        "To achieve '%s', investigating hypothesis: '%s'. Provide plan assuming hypothesis is true.",
                        originalGoal, clues
                    );
                }

                if (!new_knowledge.isEmpty()) {
                    goalForPrompt += " Consider this learned fact: " + new_knowledge;
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                obs = getObservation();

                // Algorithm 1, Line 13: Creative reasoning (Node 3)
                try {
                    log.info("Creative reasoning (Node 3)");
                    String divfilledPrompt = divergantPromptTemplate
                            .replace("{goal}", goalForPrompt)
                            .replace("{obs}", obs)
                            .replace("{beliefContents}", beliefContents)
                            .replace("{aslContents}", aslContents);

                    OpenaiResponses response = TRADE.getAvailableService(new TRADEServiceConstraints()
                                    .name("responses")
                                    .argTypes(String.class, String.class, String.class))
                            .call(OpenaiResponses.class, "o4-mini-2025-04-16", 
                                finalCheck + "\n" + additional_prompt + "\n" + divfilledPrompt, 
                                this.user_id);

                    this.user_id = response.id;

                    if (response.output != null && !response.output.isEmpty()) {
                        for (OpenaiResponses.Output outputItem : response.output) {
                            if ("message".equals(outputItem.type) && outputItem.content != null && !outputItem.content.isEmpty()) {
                                divAnswer = outputItem.content.get(0).text;
                                break;
                            }
                        }
                    }

                    if (divAnswer.isEmpty() || divAnswer.trim().equalsIgnoreCase("DONE")) {
                        log.info("LLM indicated completion.");
                        break;
                    }

                    log.info("Creative insights: {}", divAnswer);
                } catch (TRADEException e) {
                    log.error("Error in creative reasoning", e);
                    return;
                }

                // Algorithm 1, Line 14: Action-grounded planning (Node 4)
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                try {
                    log.info("Action-grounded planning (Node 4)");
                    obs = getObservation();
                    String filledPrompt = promptTemplate
                            .replace("{GOAL}", goalForPrompt)
                            .replace("{OBS}", obs)
                            .replace("{BELIEFS}", beliefContents)
                            .replace("{plan}", divAnswer.split("Reasoning")[1])
                            .replace("{ASL}", aslContents);

                    filledPrompt += " Base expected observations on: 1) Learned experience (priority), 2) Common sense. " +
                                  "Learn from mistakes but don't change correct assumptions. " +
                                  "Plan must continue from current state, not repeat completed actions.";

                    OpenaiResponses response = TRADE.getAvailableService(new TRADEServiceConstraints()
                                    .name("responses")
                                    .argTypes(String.class, String.class, String.class))
                            .call(OpenaiResponses.class, "o4-mini-2025-04-16", 
                                finalCheck + "\n" + additional_prompt + "\n" + filledPrompt, 
                                this.user_id);

                    this.user_id = response.id;
                    additional_prompt = "";
                    finalCheck = "";

                    String answer = "";
                    if (response.output != null && !response.output.isEmpty()) {
                        for (OpenaiResponses.Output outputItem : response.output) {
                            if ("message".equals(outputItem.type) && outputItem.content != null && !outputItem.content.isEmpty()) {
                                answer = outputItem.content.get(0).text;
                                break;
                            }
                        }
                    }

                    if (answer.isEmpty() || answer.trim().equalsIgnoreCase("DONE")) {
                        log.info("LLM indicated completion.");
                        break;
                    }

                    log.info("Generated plan: {}", answer);
                    actions = new ArrayList<>(Arrays.asList(answer.split("\n")));
                    actionIndex = 0;
                } catch (TRADEException e) {
                    log.error("Error in action-grounded planning", e);
                    return;
                }
            }

            // Algorithm 1, Lines 16-48: Action execution loop
            String line = actions.get(actionIndex).trim();
            line = line.replaceAll("[^a-zA-Z0-9(),]", "");

            log.info("Executing action: {}", line);

            Matcher m = pat.matcher(line);
            if (!m.matches()) {
                log.warn("Skipping unparsable action: {}", line);

                // Algorithm 1, Lines 17-20: Handle END_VAL bookkeeping
                if ("END_VALIDATION".equals(line)) {
                    log.info("Ending validation context");
                    int idx = originalGoal.lastIndexOf("VALIDATION:");
                    if (idx != -1) {
                        int endIdx = originalGoal.indexOf(".", idx);
                        if (endIdx != -1) {
                            originalGoal = originalGoal.substring(0, idx) + 
                                         originalGoal.substring(endIdx + 1).trim();
                        } else {
                            originalGoal = originalGoal.substring(0, idx).trim();
                        }
                    }
                }
                actionIndex++;
                continue;
            }

            // Algorithm 1, Line 21: Action validation (Node 5)
            log.info("VALIDATION (Node 5)");
            StringBuilder failureReason = new StringBuilder();
            if (!validateAction(actions.get(actionIndex), aslContents, failureReason)) {
                log.warn("Validation failed: {}. Replanning.", failureReason.toString());
                additional_prompt = "Plan invalid:\n" + failureReason.toString() +
                        "\nRegenerate plan. Action " + actions.get(actionIndex) + 
                        " failed preconditions. Satisfy them first or try different approach.";
                actionIndex = actions.size() + 1;
                continue;
            }

            // Algorithm 1, Line 25: Execute action and observe (Node 6)
            log.info("EXECUTION (Node 6)");
            String actionName = m.group(1);
            String argsGroup = m.group(2).trim();

            List<Symbol> symbols = new ArrayList<>();
            symbols.add(Factory.createSymbol("self"));
            if (!argsGroup.isEmpty()) {
                for (String tok : argsGroup.split("\\s*,\\s*")) {
                    symbols.add(Factory.createSymbol(tok));
                }
            }

            Predicate predicate = Factory.createPredicate(actionName, symbols);
            GoalStatus state = GoalStatus.fromString("UNKNOWN");

            try {
                long goalId = submitGoalSvc.call(long.class, predicate);
                log.info("Submitted goal [{}]: {}", goalId, predicate);

                // Monitor goal status
                while (state != GoalStatus.fromString("SUCCEEDED") && 
                       state != GoalStatus.fromString("FAILED")) {
                    try {
                        Thread.sleep(30);
                        state = TRADE.getAvailableService(new TRADEServiceConstraints()
                                .name("getGoalStatus")
                                .argTypes(Long.class))
                                .call(GoalStatus.class, goalId);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (TRADEException e) {
                        log.error("Failed to get goal status", e);
                        state = GoalStatus.fromString("FAILED");
                    }
                }
            } catch (TRADEException e) {
                log.error("Failed to submit goal: {}", predicate, e);
                state = GoalStatus.fromString("FAILED");
            }

            String new_obs = getObservation();

            // Algorithm 1, Lines 26-38: Success case
            if (state == GoalStatus.fromString("SUCCEEDED")) {
                log.info("Action succeeded: {}", predicate);
                
                List<String> mismatches = null;
                String missmatch_input = null;

                // Algorithm 1, Line 27: Compare expected vs actual observations
                if (!actions.get(actionIndex + 1).equals("FAILURE")) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {};

                        Map<String, Object> expectedObsMap = mapper.readValue(actions.get(actionIndex + 1), typeRef);
                        Map<String, Object> actualObsMap = mapper.readValue(new_obs, typeRef);

                        // Remove position fields from comparison
                        expectedObsMap.keySet().removeIf(key -> key.equals("location") || key.endsWith("Pos"));
                        actualObsMap.keySet().removeIf(key -> key.equals("location") || key.endsWith("Pos"));

                        String filteredExpectedObsJson = mapper.writeValueAsString(expectedObsMap);
                        String filteredActualObsJson = mapper.writeValueAsString(actualObsMap);

                        mismatches = ObservationComparator.getMismatches(filteredExpectedObsJson, filteredActualObsJson);
                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    
                    if (!mismatches.isEmpty()) {
                        missmatch_input = String.join("\n", mismatches);
                    }
                } else {
                    missmatch_input = "Action expected to fail but succeeded.";
                }

                log.info("OBSERVATION ANALYSIS (Node 8)");

                // Algorithm 1, Lines 28-34: Observational learning
                if (missmatch_input != null) {
                    log.warn("Observation mismatch detected: {}", missmatch_input);

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    try {
                        // Node 8: Observational learning
                        String obs_prompt = observationTemplate
                                .replace("{obs}", obs)
                                .replace("{line}", line)
                                .replace("{input}", originalGoal)
                                .replace("{mismatches}", missmatch_input)
                                .replace("{new_obs}", new_obs);

                        OpenaiResponses response = TRADE.getAvailableService(new TRADEServiceConstraints()
                                        .name("responses")
                                        .argTypes(String.class, String.class, String.class))
                                .call(OpenaiResponses.class, "o4-mini-2025-04-16", obs_prompt, this.user_id);

                        this.user_id = response.id;
                        String obs_answer = "";
                        if (response.output != null && !response.output.isEmpty()) {
                            for (OpenaiResponses.Output outputItem : response.output) {
                                if ("message".equals(outputItem.type) && outputItem.content != null && !outputItem.content.isEmpty()) {
                                    obs_answer = outputItem.content.get(0).text;
                                    break;
                                }
                            }
                        }
                        
                        log.info("Learned from mismatch: {}", obs_answer);
                        new_knowledge += obs_answer + ". ";

                        // Algorithm 1, Lines 30-33: Trigger replanning if needed
                        if (!obs_answer.startsWith("NO REPLAN NEEDED") && !obs_answer.startsWith("NON OBSERVABLE")) {
                            additional_prompt = "Plan failed - unexpected action effects. Learned:\n" + obs_answer + 
                                    "\nRegenerate plan for: " + originalGoal + 
                                    ". Use new knowledge. Consider different strategies.";
                            actionIndex = actions.size() + 1;
                            continue;
                        } else {
                            log.info("No replan needed. Checking non-observable effects (Node 9)");
                            
                            if (obs_answer.startsWith("NO REPLAN NEEDED")) {
                                obs_answer = obs_answer.replace("NO REPLAN NEEDED", "").trim();
                                if (!obs_answer.isEmpty()) {
                                    new_knowledge += obs_answer + ". ";
                                }
                            }

                            // Algorithm 1, Lines 35-38: Effect validation (Node 9)
                            String validation_prompt = validationTemplate
                                    .replace("{OBS}", new_obs)
                                    .replace("{line}", line)
                                    .replace("{goal}", originalGoal)
                                    .replace("{mismatches}", obs_answer)
                                    .replace("{BELIEFS}", beliefContents)
                                    .replace("{ASL}", aslContents);

                            response = TRADE.getAvailableService(new TRADEServiceConstraints()
                                        .name("responses")
                                        .argTypes(String.class, String.class, String.class))
                                    .call(OpenaiResponses.class, "o4-mini-2025-04-16", validation_prompt, this.user_id);

                            this.user_id = response.id;
                            String val_answer = "";
                            if (response.output != null && !response.output.isEmpty()) {
                                for (OpenaiResponses.Output outputItem : response.output) {
                                    if ("message".equals(outputItem.type) && outputItem.content != null && !outputItem.content.isEmpty()) {
                                        val_answer = outputItem.content.get(0).text;
                                        break;
                                    }
                                }
                            }
                            
                            log.info("Effect validation result: {}", val_answer);

                            if (!val_answer.startsWith("SUCCESS")) {
                                log.info("Inserting validation actions into plan");
                                actions.add(actionIndex + 1, "END_VALIDATION");
                                actions.addAll(actionIndex + 1, Arrays.asList(val_answer.split("PLAN")[1].split("\n")));
                                originalGoal += "VALIDATION: checking non-observable effects of " + line + ". ";
                            }
                            actionIndex++;
                        }
                    } catch (TRADEException e) {
                        log.error("Error in observational learning", e);
                        actionIndex++;
                    }
                } else {
                    // No mismatch, still check non-observable effects (Node 9)
                    log.info("Observations match. Checking non-observable effects (Node 9)");
                    
                    try {
                        String validation_prompt = validationTemplate
                                .replace("{OBS}", new_obs)
                                .replace("{line}", line)
                                .replace("{goal}", originalGoal)
                                .replace("{mismatches}", "No mismatches detected")
                                .replace("{BELIEFS}", beliefContents)
                                .replace("{ASL}", aslContents);

                        OpenaiResponses response = TRADE.getAvailableService(new TRADEServiceConstraints()
                                    .name("responses")
                                    .argTypes(String.class, String.class, String.class))
                                .call(OpenaiResponses.class, "o4-mini-2025-04-16", validation_prompt, this.user_id);

                        this.user_id = response.id;
                        String val_answer = "";
                        if (response.output != null && !response.output.isEmpty()) {
                            for (OpenaiResponses.Output outputItem : response.output) {
                                if ("message".equals(outputItem.type) && outputItem.content != null && !outputItem.content.isEmpty()) {
                                    val_answer = outputItem.content.get(0).text;
                                    break;
                                }
                            }
                        }
                        
                        log.info("Effect validation result: {}", val_answer);

                        if (!val_answer.startsWith("SUCCESS")) {
                            actions.add(actionIndex + 1, "END_VALIDATION");
                            actions.addAll(actionIndex + 1, Arrays.asList(val_answer.split("PLAN")[1].split("\n")));
                            originalGoal += "VALIDATION: checking non-observable effects of " + line + ". ";
                        }
                        actionIndex++;
                    } catch (TRADEException e) {
                        log.error("Error in effect validation", e);
                        actionIndex++;
                    }
                }

            // Algorithm 1, Lines 39-43: Failure case
            } else if (state == GoalStatus.fromString("FAILED")) {
                log.warn("Goal failed: {}. Diagnosing failure (Node 7)", predicate);
                
                if (actions.get(actionIndex + 1).equals("FAILURE")) {
                    actionIndex++;
                    continue;
                }

                try {
                    // Node 7: Failure diagnosis
                    String filledGuessingPrompt = guessingTemplate
                            .replace("{input}", originalGoal)
                            .replace("{line}", line)
                            .replace("{obs}", obs)
                            .replace("{beliefContents}", beliefContents)
                            .replace("{aslContents}", aslContents);

                    OpenaiResponses replanresponse = TRADE.getAvailableService(new TRADEServiceConstraints()
                                    .name("responses")
                                    .argTypes(String.class, String.class, String.class))
                            .call(OpenaiResponses.class, "o4-mini-2025-04-16", filledGuessingPrompt, this.user_id);

                    this.user_id = replanresponse.id;
                    String replananswer = "";
                    if (replanresponse.output != null && !replanresponse.output.isEmpty()) {
                        for (OpenaiResponses.Output outputItem : replanresponse.output) {
                            if ("message".equals(outputItem.type) && outputItem.content != null && !outputItem.content.isEmpty()) {
                                replananswer = outputItem.content.get(0).text;
                                break;
                            }
                        }
                    }

                    // Algorithm 1, Line 41: Add hypotheses to exploration queue
                    if (!replananswer.isEmpty()) {
                        clueAdded = true;
                        log.info("Failure diagnosis: {}", replananswer);

                        String[] responseLines = replananswer.trim().split("\\R");
                        if (responseLines.length > 0 && responseLines[0].trim().equalsIgnoreCase("FAILURE TO DIAGNOSE")) {
                            if (!clueQueue.isEmpty()) {
                                String failedClue = clueQueue.pollLast();
                                cluePopped = true;
                                log.info("Diagnosis failed. Discarding hypothesis: '{}'", failedClue);
                            }
                        } else {
                            List<String> newSuggestions = new ArrayList<>();
                            for (String suggestionLine : responseLines) {
                                String cleanSuggestion = suggestionLine.replaceAll("^\\d+[.)]?\\s*", "").trim();
                                if (!cleanSuggestion.isEmpty()) {
                                    newSuggestions.add(cleanSuggestion);
                                }
                            }
                            Collections.reverse(newSuggestions);
                            if (!explorationQueue.isEmpty()) {
                                explorationQueue.addFirst("POP CLUE");
                            }
                            for (String suggestion : newSuggestions) {
                                explorationQueue.addFirst(suggestion);
                                log.info("Adding hypothesis to Qe: '{}'", suggestion);
                            }
                        }
                    }
                } catch (TRADEException e) {
                    log.error("Error in failure diagnosis", e);
                }
                
                actionIndex = actions.size() + 1; // Trigger replanning
            }

            // Algorithm 1, Line 47: Update current state
            obs = new_obs;
        }
    }
}

