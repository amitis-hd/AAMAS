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

public class LLMGoalParser extends DiarcComponent {

    /*****************************************************/
    /* */
    /* Grabbing the asl, pl, and prompt files            */
    /* */
    /*****************************************************/

    private Path planningPromptPath = Paths.get("/mnt/c/Users/hamid/Desktop/myboxbot/diarc/core/src/main/resources/config/edu/tufts/hrilab/llm/inputs/planningPrompt.txt");
    private Path observationPromptPath = Paths.get("/mnt/c/Users/hamid/Desktop/myboxbot/diarc/core/src/main/resources/config/edu/tufts/hrilab/llm/inputs/observationPrompt.txt");
    private Path guessingPromptPath = Paths.get("/mnt/c/Users/hamid/Desktop/myboxbot/diarc/core/src/main/resources/config/edu/tufts/hrilab/llm/inputs/guessingPrompt.txt");
    private Path validationPromptPath = Paths.get("/mnt/c/Users/hamid/Desktop/myboxbot/diarc/core/src/main/resources/config/edu/tufts/hrilab/llm/inputs/validationPrompt.txt");
    private Path divergantPromptPath = Paths.get("/mnt/c/Users/hamid/Desktop/myboxbot/diarc/core/src/main/resources/config/edu/tufts/hrilab/llm/inputs/divergantPrompt.txt");

    private Path aslfilePath = Paths.get("/mnt/c/Users/hamid/Desktop/myboxbot/diarc/core/src/main/resources/config/edu/tufts/hrilab/action/asl/domains/boxbot.asl");
    private Path belieffilePath = Paths.get("/mnt/c/Users/hamid/Desktop/myboxbot/diarc/core/src/main/resources/config/edu/tufts/hrilab/belief/agents/boxbot.pl");


    String aslContents = "";
    String beliefContents = "";
    String promptTemplate = "";
    String observationTemplate = "";
    String guessingTemplate = "";
    String validationTemplate = "";
    String divergantPromptTemplate = "";

    /*****************************************************/
    /* */
    /* private variables                    */
    /* */
    /*****************************************************/

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String user_id = null;

    /**
     * Queue for potential failure reasons (hypotheses) suggested by the LLM.
     */
    private Deque<String> explorationQueue = new LinkedList<>();

    /**
     * Queue for the current hypothesis the agent is actively trying to solve.
     */
    private Deque<String> clueQueue = new LinkedList<>();



    public LLMGoalParser() {
        super();
    }


    /*****************************************************/
    /* */
    /* Given an action, returns true if the action       */
    /* exists in the asl, and all its preconditions      */
    /* are met at functioncall time                      */
    /* */
    /*****************************************************/
    private boolean validateAction(String actionLine, String aslContents, StringBuilder failureReason) {

        // 1.Parse the incoming action command
        // Regex to capture the action name and its argument list
        Pattern pat = Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?:\\((.*)\\))?$");
        Matcher m = pat.matcher(actionLine.trim());

        if (!m.matches()) {
            String issue = "Skipping unparsable action: " + actionLine;
            // log.warn(issue);
            failureReason.append(issue);
            return false;
        }

        String actionName = m.group(1);
        String inputArgsStr = m.group(2) != null ? m.group(2) : "";

        // Requirement 1: Arguments from the actionLine are put into an array (a List in this case).
        // For open(door, key), inputArgs becomes ["door", "key"]
        List<String> inputArgs = inputArgsStr.isEmpty() ?
                new ArrayList<>() :
                Arrays.asList(inputArgsStr.split("\\s*,\\s*"));

        // 2. === Find action in ASL and extract its formal parameters ===
        // Regex for the ASL action signature: () = actionName[...](...) {
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

        // Find the matching closing brace by counting braces
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

        // Extract formal parameter names (e.g., "?object", "?tool") from the ASL definition
        List<String> aslParamNames = new ArrayList<>();
        // This regex finds variable names like "?object" from "Symbol ?object:physical"
        Pattern paramNamePattern = Pattern.compile("\\?([a-zA-Z_][a-zA-Z0-9_]*)");
        Matcher paramNameMatcher = paramNamePattern.matcher(aslParamsStr);
        while (paramNameMatcher.find()) {
            aslParamNames.add(paramNameMatcher.group(1)); // add "object", not "?object"
        }

        // 3.Validate argument count and create name-to-index mapping
        if (inputArgs.size() != aslParamNames.size()) {
            String issue = String.format("Action '%s' expects %d arguments, but received %d.",
                    actionName, aslParamNames.size(), inputArgs.size());
            // log.warn(issue);
            failureReason.append(issue);
            return false;
        }

        // Requirement 2: Create a map from the formal parameter name to its argument index.
        // For (?object, ?tool), this creates {"object": 0, "tool": 1}
        Map<String, Integer> argName_to_IndexMap = new HashMap<>();
        for (int i = 0; i < aslParamNames.size(); i++) {
            argName_to_IndexMap.put(aslParamNames.get(i), i);
        }


        // 4. Find and process all preconditions
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

                // Parse the precondition into its name and arguments
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

                // 5. Resolve precondition arguments using the name-to-index map
                List<Symbol> resolvedArgs = new ArrayList<>();
                if (!condArgsStr.isEmpty()) {
                    for (String arg : condArgsStr.split("\\s*,\\s*")) {
                        if (arg.startsWith("?")) {
                            String varName = arg.substring(1);

                            // Requirement 3: Look up index from the map, then get the value from the input array.
                            Integer index = argName_to_IndexMap.get(varName);

                            if (index == null) {
                                String issue = "Unresolved variable '" + arg + "' in precondition '" + rawCondition + "'. It's not defined in the action's signature.";
                                // log.error(issue);
                                failureReason.append(issue);
                                return false;
                            }

                            // Use the index to get the actual value from the input arguments list
                            String resolvedValue = inputArgs.get(index);
                            resolvedArgs.add(new Symbol(resolvedValue));
                        } else {
                            // Handle literal values (if they can appear in preconditions)
                            resolvedArgs.add(new Symbol(arg));
                        }
                    }
                }

                Term trade_term = new Term(conditionName, resolvedArgs);

                // 6. Call the TRADE service with resolved arguments to check for pre condition validity
                try {
                    log.info("Making TRADEService call for '{}' with args: {}\n", conditionName, resolvedArgs);

                    List<?> result = TRADE.getAvailableService(
                            new TRADEServiceConstraints().name(conditionName).argTypes(Term.class)
                    ).call(List.class, trade_term);

                    boolean holds = !result.isEmpty();
                    if (negated) {
                        holds = !holds; // Apply negation
                    }

                    if (!holds) {
                        String issue = "Action " + actionName + " fails because precondition '" + rawCondition + "' is not satisfied. Remember, ~condition() means for the action to be performed condition() must return false. (~ means not/negation)";
                        log.warn(issue);
                        failureReason.append(issue);
                        return false;
                    }
                } catch (TRADEException e) {
                    String issue = "TRADE service failed while checking '" + rawCondition + "': " + e.getMessage();
                    // log.error(issue, e);
                    failureReason.append(issue);
                    return false;
                }
            }
        }

        // If all preconditions pass, the action is valid
        log.info("Action {} is valid.\n", actionName);
        return true;
    }


    /*****************************************************/
    /* */
    /* given the reason why the previous plan failed     */
    /* reprompts the llm to regenerate a new plan        */
    /* */
    /*****************************************************/
    private String retryPlan(String issue, String type) {
        String plan = "";
        String obs = getObservation();
        try {
            // Pause for 2 seconds
            Thread.sleep(2000); 
            System.out.println("Resumed after 2 seconds.");

        } catch (InterruptedException e) {
            System.err.println("Thread was interrupted!");
            // Re-interrupt the current thread to propagate the interruption status
            Thread.currentThread().interrupt(); 
        }

        String prompt = "The plan you just produced was invalid:\n" + issue +
                                    "\nWith that in mind, regenerate a valid plan following the exact same rules as before. Your plan failed because you attemted action: " + type + " when its preconditions were not met. Think about why that could be. Consider satisfying the preconditions and then retrying, a change of plan, etc. Here is the current state of the world: " + obs;
        if (type.equals("obs")) {
            prompt = "The plan you just produced was invalid because an action you performed did not have the effects you were expecting. Here is what you learned from your environment from that experience: \n" + issue + "\nWith that in mind, regenerate a valid plan following the exact same rules as before to achieve your main goal. Here is the current state of the world: " + obs;
        }

        try {
            OpenaiResponses responseObj = TRADE.getAvailableService(new TRADEServiceConstraints()
                            .name("responses")
                            .argTypes(String.class, String.class, String.class))
                    .call(OpenaiResponses.class,
                            "o4-mini-2025-04-16",
                            prompt,
                            this.user_id);

            this.user_id = responseObj.id;

            if (responseObj.output != null && !responseObj.output.isEmpty()) {
                for (OpenaiResponses.Output outputItem : responseObj.output) {
                    if ("message".equals(outputItem.type) && outputItem.content != null && !outputItem.content.isEmpty()) {
                        plan = outputItem.content.get(0).text;
                        break;
                    }
                }
            } else {
                log.error("No output in response.");
            }

        } catch (TRADEException e) {
            log.error("Error calling LLM service for parseGoals.", e);
            return null;
        }

        log.info("\n\n here is the retried plan: {} \n\n" , plan);

        return plan;
    }

    public String getObservation() {

        String obs = "";

        try {

            obs = TRADE.getAvailableService(new TRADEServiceConstraints().name("getObservationJson").argTypes()).call(String.class);
            try {
                JsonNode rootNode = objectMapper.readTree(obs);
                JsonNode observationNode = rootNode.get("observation");
                obs = observationNode.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (TRADEException e) {
            log.error("Failed to obtain getObservation service", e);
            return null;
        }

        return obs;

    }









    /*****************************************************/
    /* */
    /* The goal parsing trade service called by the      */
    /* config file                                       */
    /* */
    /*****************************************************/
    @TRADEService
    @Action
    public void parseGoals(Utterance inpututterance) {


        String originalGoal = inpututterance.getWordsAsString();
        log.info("User prompt recieved by LLMGoalParser: {} \n\n" , originalGoal);
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


        // Safely look up the submitGoal service
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

        // Regex to capture action name + args
        Pattern pat = Pattern.compile("^(\\w+)\\(([^)]*)\\)$");

        


        // Initialize state variables for the main loop
        String obs = getObservation();
        List<String> actions = new ArrayList<>();   // CHANGED: was String[]
        int actionIndex = -1;
        String new_knowledge = "The agent's location field is in the format overalplace-obj1-obj2... where overall place is room/outdoors, and the obj are any object the agent is next to. This is an accurate representation of the agent's position and reachable items. The location of the agent hence does not require emperical testing.";

        boolean cluePopped = false; 
        boolean clueAdded = false;

        String additional_prompt = "";

        String explenation = "";
        String divAnswer = "";
        String finalCheck = "";

        


        while (true) {
            if (actionIndex == actions.size()) {
                log.warn("The plan has succeeded?");

                obs = getObservation();

                try {

                    log.info("Checking for goal acomplishment");

                    OpenaiResponses response = TRADE.getAvailableService(new TRADEServiceConstraints()
                                    .name("responses")
                                    .argTypes(String.class, String.class, String.class))
                            .call(OpenaiResponses.class, "o4-mini-2025-04-16", "You were given a goal " + originalGoal + " and now have come to the end of your plan. Your job is to assess if ALL ASPECTS OF THIS GOAL have been achieved or proven to be impossible. You must check your current observations of the world: " + obs + " for all observable effects, as well as your conversation history. You also must check your conversation history for evidance of checking non observable effects of ALL ELEMENTS of the goal, and their successful assertion. If there is a part of the goal that has not been achieved or confirmed, reply with an explenation of what that is. Otherwise, return DONE (all caps).", this.user_id);

                    this.user_id = response.id;
                 

                   
                    if (response.output != null && !response.output.isEmpty()) {
                        for (OpenaiResponses.Output outputItem : response.output) {
                            if ("message".equals(outputItem.type) && outputItem.content != null && !outputItem.content.isEmpty()) {
                                finalCheck = outputItem.content.get(0).text;
                                break;
                            }
                        }
                    }

                    if (finalCheck.isEmpty() || finalCheck.trim().equalsIgnoreCase("DONE")) {
                        log.info("Plan succeeded !!!!!!!!!!!!!!!!!!!!!!!");
                        break; // Exit the main while loop
                    }

                    log.info("LLM's check answer:\n {}\n\n", finalCheck);

                    actionIndex = -1;
                    


                } catch (TRADEException e) {
                    log.error("Error calling LLM service for planning. Aborting.", e);
                    return; // Exit the method on planning failure
                }

         
            }
            // =========================================================================
            // I. PLANNING PHASE: Generate a new plan if the current one is finished,
            // or if a replan is needed. setting index larger than the size forces replanning
            // =========================================================================
            if (actionIndex > actions.size() || actionIndex == -1) {  
                log.info("\n\nPLANNING\n\n");
              

                String newClue = "";

                //If explorationQueue is not empty, but clueQueue is empty, we are starting a new exploration process
                if (!explorationQueue.isEmpty() && clueQueue.isEmpty()) {
                    //pop the top element of exploration queue and put it onto clue queue
                    newClue = explorationQueue.pollFirst();
                    clueQueue.addLast(newClue);
                    log.info("Popped hypothesis '{}' from exploration queue. Adding to active clue queue.\n\n", newClue);
                }

                else if (cluePopped || clueAdded) {
                    if (explorationQueue.isEmpty()) {
                        log.info("No valid plan exists, sorry.");
                        break;
                    }

                    newClue = explorationQueue.pollFirst();


                    while (newClue.equals("POP CLUE")) {

                        String removed = clueQueue.pollFirst();
                        log.info("Giving up on hypothesis {}\n" , removed);

                    }

                    clueQueue.addLast(newClue);
            
                }

                clueAdded = false;
                cluePopped = false;

                log.info("Current list of clues left to explore: {}\n\n", explorationQueue);
                log.info("Current clues for replanning: {}\n\n", clueQueue);


                String goalForPrompt = originalGoal;

                // If we are working on a clue, formulate the prompt around it.
                if (!clueQueue.isEmpty()) {
                    List<String> processed = new ArrayList<>();
                    int size = clueQueue.size();
                    int i = 0;

                    for (String clue : clueQueue) {
                        i++;
                        if (i < size) {
                            // Split and keep only the part before "Workaround subplan"
                            String[] parts = clue.split("Workaround subplan", 2);
                            processed.add(parts[0].trim());
                        } else {
                            // For the last element, keep it as-is
                            processed.add(clue.trim());
                        }
                    }

                    String clues = String.join(" and ", processed);
                    goalForPrompt = String.format(
                        "To achieve the main goal '%s', the agent is currently investigating the hypothesis that: '%s'. Please provide a plan to achieve the main goal, by assuming the hypothesis is true and the way to do it.",
                        originalGoal, clues
                    );
                }

                // Add any newly learned knowledge to the prompt
                if (!new_knowledge.isEmpty()) {
                    goalForPrompt += " Additionally, make sure to consider this new learned fact about your environment: " + new_knowledge;
                }

                //divergant planning
                try {
                    // Pause for 2 seconds
                    Thread.sleep(2000); 
                    System.out.println("Resumed after 2 seconds.");

                } catch (InterruptedException e) {
                    System.err.println("Thread was interrupted!");
                    // Re-interrupt the current thread to propagate the interruption status
                    Thread.currentThread().interrupt(); 
                }

                obs = getObservation();

                try {

                    log.info("Divergant planning");
                    String divfilledPrompt = divergantPromptTemplate
                            .replace("{goal}", goalForPrompt)
                            .replace("{obs}", obs)
                            .replace("{beliefContents}", beliefContents)
                            .replace("{aslContents}", aslContents);

                    OpenaiResponses response = TRADE.getAvailableService(new TRADEServiceConstraints()
                                    .name("responses")
                                    .argTypes(String.class, String.class, String.class))
                            .call(OpenaiResponses.class, "o4-mini-2025-04-16", finalCheck + "\n" + additional_prompt + " \n" + divfilledPrompt, this.user_id);

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
                        log.info("LLM provided no new actions or indicated completion. Ending goal execution.");
                        break; // Exit the main while loop
                    }

                    log.info("LLM's divergance:\n {}\n\n", divAnswer);
                    


                } catch (TRADEException e) {
                    log.error("Error calling LLM service for planning. Aborting.", e);
                    return; // Exit the method on planning failure
                }



                //Get plan
                try {
                    try {
                        // Pause for 2 seconds
                        Thread.sleep(2000); 
                        System.out.println("Resumed after 2 seconds.");

                    } catch (InterruptedException e) {
                        System.err.println("Thread was interrupted!");
                        // Re-interrupt the current thread to propagate the interruption status
                        Thread.currentThread().interrupt(); 
                    }

                    obs = getObservation();
                    log.info("\n\nobservation before planning : \n {}\n\n", obs);
                    String filledPrompt = promptTemplate
                            .replace("{GOAL}", goalForPrompt)
                            .replace("{OBS}", obs)
                            .replace("{BELIEFS}", beliefContents)
                            .replace("{plan}", divAnswer.split("Reasoning")[1])
                            .replace("{ASL}", aslContents);

                    filledPrompt += " IMPORTANT: make sure to base all your expected observation fields on 1.What you have learned through experience from your environment (priority), carefully, without over generalizing, and 2. Common sense and why you chose the actions to achieve the goal. **Make sure to refer to the conversation memory and learn from your mistakes, but do not change things you were right about.** VERY IMPORTANT: make sure (especially when replanning) that your plan is the steps the agent must take starting from its current state. consider what it has already acomplished, and do not repeat those actions in your plan. You must at each point check 1. what do i have left to acomplish (acomplish means that aspect of the goal is currently satisfied, it DOES NOT mean you attempted it and failed), and 2. how do i acomplish it?";

                    OpenaiResponses response = TRADE.getAvailableService(new TRADEServiceConstraints()
                                    .name("responses")
                                    .argTypes(String.class, String.class, String.class))
                            .call(OpenaiResponses.class, "o4-mini-2025-04-16", finalCheck + "\n" + additional_prompt + "\n"  + filledPrompt, this.user_id);

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
                        log.info("LLM provided no new actions or indicated completion. Ending goal execution.");
                        break; // Exit the main while loop
                    }

                    log.info("LLM's plan:\n {}\n\n", answer);
                    // explenation = answer.split("PLAN")[0];
                    actions = new ArrayList<>(Arrays.asList(answer.split("\n")));   
                    actionIndex = 0; // Reset to execute the new plan from the beginning

                } catch (TRADEException e) {
                    log.error("Error calling LLM service for planning. Aborting.", e);
                    return; // Exit the method on planning failure
                }
            }

            // =========================================================================
            // II. EXECUTION PHASE: Execute the current action from the plan.
            // =========================================================================
            String line = actions.get(actionIndex).trim();   
            line = line.replaceAll("[^a-zA-Z0-9(),]", "");
            


            log.info("Executing action: {} \n\n", line);

            Matcher m = pat.matcher(line);
            if (!m.matches()) {
                log.warn("Skipping unparsable action: {}", line);

                if ("END_VALIDATION".equals(line)) {
                    log.warn("ending validation");
                    // remove the most recently added sentence in new_knowledge that starts with "VALIDATION:"
                    int idx = originalGoal.lastIndexOf("VALIDATION:");
                    if (idx != -1) {
                        int endIdx = originalGoal.indexOf(".", idx);
                        if (endIdx != -1) {
                            // include the period in the removal
                            originalGoal = originalGoal.substring(0, idx) 
                                        + originalGoal.substring(endIdx + 1).trim();
                        } else {
                            // no period found, just cut everything from VALIDATION: onward
                            originalGoal = originalGoal.substring(0, idx).trim();
                        }
                    }
                }

                actionIndex++;
                continue;
            }

            // Validate action and its preconditions before execution
          
            log.info("\n\n VALIDATING\n\n");
            StringBuilder failureReason = new StringBuilder();
            if (!validateAction(actions.get(actionIndex), aslContents, failureReason)) {
                log.warn("Action validation failed: {}. Requesting a new plan.", failureReason.toString());
                additional_prompt = "The plan you just produced was invalid:\n" + failureReason.toString() +
                                    "\nWith that in mind, regenerate a valid plan following the rules below. Your plan failed because you attemted action: " + actions.get(actionIndex) + " when its preconditions were not met. Think about why that could be. Consider satisfying the preconditions and then retrying, a change of plan, etc.";
                actionIndex = actions.size() + 1;
                continue; // Restart the loop with the new plan
            }


            log.info("\n\nPERFORMING\n\n");

            // Submit the validated goal
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
                log.info("Submitted [{}]: {}", goalId, predicate);

                // Monitor the goal's status until it succeeds or fails
                while (state != GoalStatus.fromString("SUCCEEDED") && state != GoalStatus.fromString("FAILED")) {
                    try {
                        Thread.sleep(30);
                        state = TRADE.getAvailableService(new TRADEServiceConstraints().name("getGoalStatus").argTypes(Long.class)).call(GoalStatus.class, goalId);
                    } catch (InterruptedException e) {
                        log.warn("Sleep interrupted while waiting for goal status", e);
                        Thread.currentThread().interrupt();
                    } catch (TRADEException e) {
                        log.error("Failed to get goal status service", e);
                        state = GoalStatus.fromString("FAILED"); // Assume failure if status is unavailable
                    }
                }

            } catch (TRADEException e) {
                log.error("Failed to submit goal: {}", predicate, e);
                state = GoalStatus.fromString("FAILED");
            }

            // =========================================================================
            // III. OBSERVATION & LEARNING PHASE: Process the action's outcome.
            // =========================================================================
            String new_obs = getObservation();

            if (state == GoalStatus.fromString("SUCCEEDED")) {
                /***************************/
                /* SUCCESS CASE            */
                /***************************/
                
                log.info("Action [{}] succeeded.\n\n", predicate);
                List<String> mismatches = null; 
                String missmatch_input = null; 

                if(!actions.get(actionIndex + 1).equals("FAILURE")) {
                
                    // Check for mismatches between expected and actual observations
                    
                    try {
                        // 1. Initialize a JSON mapper and define the map type
                        ObjectMapper mapper = new ObjectMapper();
                        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {};

                        // 2. Parse the JSON strings into Map objects
                        Map<String, Object> expectedObsMap = mapper.readValue(actions.get(actionIndex + 1), typeRef);
                        Map<String, Object> actualObsMap = mapper.readValue(new_obs, typeRef);

                        // 3. Remove the 'location' and '*Pos' fields from the maps
                        expectedObsMap.keySet().removeIf(key -> key.equals("location") || key.endsWith("Pos"));
                        actualObsMap.keySet().removeIf(key -> key.equals("location") || key.endsWith("Pos"));

                        // 4. Convert the filtered maps back into JSON strings
                        String filteredExpectedObsJson = mapper.writeValueAsString(expectedObsMap);
                        String filteredActualObsJson = mapper.writeValueAsString(actualObsMap);

                        // 5. Pass the new filtered JSON strings to the comparator
                        mismatches = ObservationComparator.getMismatches(filteredExpectedObsJson, filteredActualObsJson);

                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                        // Handle potential JSON parsing/serialization errors
                        e.printStackTrace();
                    }
                    if (!mismatches.isEmpty()) {
                        missmatch_input = String.join("\n ", mismatches);
                    }

                    
                } else {
                    missmatch_input = "The action was expected to fail but it performed.";
                }
                
                log.info("\n\nMISMATCH?\n\n");


                if (missmatch_input != null) {

                    

                    log.warn("Observation mismatch detected after action '{}'. Learning from outcome.", line);
                    log.info(missmatch_input);

                    try {

                        try {
                            // Pause for 2 seconds
                            Thread.sleep(2000); 
                            System.out.println("Resumed after 2 seconds.");

                        } catch (InterruptedException e) {
                            System.err.println("Thread was interrupted!");
                            // Re-interrupt the current thread to propagate the interruption status
                            Thread.currentThread().interrupt(); 
                        }

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
                        log.info("Learned new knowledge from mismatch: {}\n\n", obs_answer);
                        new_knowledge += obs_answer + ". ";
                        

                        if(!obs_answer.startsWith("NO REPLAN NEEDED") && !obs_answer.startsWith("NON OBSERVABLE")) {
                        
                            additional_prompt = "The plan you just produced was invalid because an action you performed did not have the effects you were expecting. Here is what you learned from your environment from that experience: \n" + obs_answer + "\nWith that in mind, regenerate a valid plan following the rules below to achieve your main goal. " + originalGoal + " You must use this new learned knowledge in your new plan. You must consider different strategies. Remember, you can use everything in your surrounding environment (in your pl file or observations) but you are not aware of everything in your environment (hence you can look for things)";

                            actionIndex = actions.size() + 1;

                            // String answer = retryPlan(failureReason.toString(), "obs");
                            // if (answer != null && !answer.isEmpty()) {
                            //     actions = answer.split("\n");
                            //     actionIndex = 0;
                            // } else {
                            //     log.error("Failed to get a valid retry plan. Aborting.");
                            //     return;
                            // }
                            continue; // Restart the loop with the new plan

                        }
                        else {

                  
                            log.info("no replan needed, checking for non observable effects\n");
                            // remove "no replan needed" from obs_answer and add it to new_knowledge
                            if (obs_answer.startsWith("NO REPLAN NEEDED")) {

                                obs_answer = obs_answer.replace("NO REPLAN NEEDED", "").trim();
                                if (!obs_answer.isEmpty()) {
                                    new_knowledge += obs_answer + ". ";
                                }

                            

                            } 
                            

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
                            log.info("validation plan: {}\n\n", val_answer);

                            if (!val_answer.startsWith("SUCCESS")) {
                                log.info("need to check the non observable effects\n");
                                // First add a line "END_VALIDATION" to the top of the actions array 
                                // then add val_answer to the top of the actions array
                                // then add to new_knowledge:
                                // "VALIDATION: currently performing tasks to make sure the action " + line + " performed as expected and succeeded in non observable ways"
                                actions.add(actionIndex + 1, "END_VALIDATION");
                                actions.addAll(actionIndex + 1, Arrays.asList(val_answer.split("PLAN")[1].split("\n")));
                                originalGoal += "VALIDATION: currently performing tasks to make sure the action " 
                                    + line + " performed as expected and succeeded in non observable ways. ";

                                //make sure if obs = FAILURE we dont replan
                                // make sure if end validation is reached, the sentence adter VALIDATION is removed from the knowledge string. (last instance of it)
                            }

                            actionIndex++;
                        }


                    } catch (TRADEException e) {
                        log.error("Error calling LLM for observation learning.", e);
                        actionIndex++; // Failsafe: move on despite error
                    }
                } else {
                    log.info("Observation matches expectation. checking for non-observable effects.");
                    try {

                    
                        String validation_prompt = validationTemplate
                            .replace("{OBS}", new_obs)
                            .replace("{line}", line)
                            .replace("{goal}", originalGoal)
                            .replace("{mismatches}", "No missmatches detected")
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
                        log.info("validation plan: {}\n\n", val_answer);

                        if (!val_answer.startsWith("SUCCESS")) {
                            // First add a line "END_VALIDATION" to the top of the actions array 
                            // then add val_answer to the top of the actions array
                            // then add to new_knowledge:
                            // "VALIDATION: currently performing tasks to make sure the action " + line + " performed as expected and succeeded in non observable ways"
                            actions.add(actionIndex + 1, "END_VALIDATION");
                            actions.addAll(actionIndex + 1, Arrays.asList(val_answer.split("PLAN")[1].split("\n")));
                            originalGoal += "VALIDATION: currently performing tasks to make sure the action " 
                                + line + " performed as expected and succeeded in non observable ways. ";

                            //make sure if obs = FAILURE we dont replan
                            // make sure if end validation is reached, the sentence adter VALIDATION is removed from the knowledge string. (last instance of it)
                        }

                        actionIndex++; // No mismatch, perfect success. Move to the next action.
                    
                    } catch (TRADEException e) {
                        log.error("Error calling LLM for observation learning.", e);
                        actionIndex++; // Failsafe: move on despite error
                    }
                }

            } else if (state == GoalStatus.fromString("FAILED")) {
                /***************************/
                /* FAILURE CASE            */
                /***************************/
                log.warn("Goal failed: {}. Attempting to diagnose the failure.\n\n", predicate);
                if (actions.get(actionIndex + 1).equals("FAILURE")) {
                    actionIndex++;
                    continue; 
                }

                try {
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

                    // Process the diagnosis and update the exploration queue
                    if (!replananswer.isEmpty()) {
                        clueAdded = true; 
                        log.info("LLM diagnosis for failure:\n {}\n\n", replananswer);
                        
                        String[] responseLines = replananswer.trim().split("\\R");
                        if (responseLines.length > 0 && responseLines[0].trim().equalsIgnoreCase("FAILURE TO DIAGNOSE")) {
                            if (!clueQueue.isEmpty()) {
                                String failedClue = clueQueue.pollLast();
                                cluePopped = true; 
                                log.info("Diagnosis failed. Discarding current clue as a dead end: '{}'\n", failedClue);
                            }
                        } else {
                            List<String> newSuggestions = new ArrayList<>();
                            for (String suggestionLine : responseLines) {
                                String cleanSuggestion = suggestionLine.replaceAll("^\\d+[.)]?\\s*", "").trim();
                                if (!cleanSuggestion.isEmpty()) {
                                    newSuggestions.add(cleanSuggestion);
                                }
                            }
                            Collections.reverse(newSuggestions); // Add to front of queue in order
                            if(!explorationQueue.isEmpty()) {
                                explorationQueue.addFirst("POP CLUE");
                                log.info("new queue on stack \n\n");
                            }
                            for (String suggestion : newSuggestions) {
                                explorationQueue.addFirst(suggestion);
                                log.info("Adding new exploration hypothesis to queue: '{}'\n", suggestion);
                            }
                        }
                        
                    }
                } catch (TRADEException e) {
                    log.error("Error calling LLM for failure diagnosis.", e);
                }
                // Force a replan to handle the failure
                actionIndex = actions.size() + 1;
            }
            

            // Update our view of the world state for the next loop iteration
            obs = new_obs;
        }
    }
}