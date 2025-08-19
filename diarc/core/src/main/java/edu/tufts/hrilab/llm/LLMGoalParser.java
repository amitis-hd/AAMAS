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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import edu.tufts.hrilab.boxbot.actions.Active;

import edu.tufts.hrilab.llm.ObservationComparator;

import edu.tufts.hrilab.llm.openai.response.OpenaiResponses;

public class LLMGoalParser extends DiarcComponent {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public LLMGoalParser() {
        super();
    }

    @TRADEService
    @Action
    public void parseGoals(Utterance inpututterance) {

        String input = inpututterance.getWordsAsString();
        log.info("Here is the input to parseGoals " + input);
        
        // Use absolute paths directly without relativizing
        Path aslfilePath = Paths.get("/mnt/c/Users/hamid/Desktop/myboxbot/diarc/core/src/main/resources/config/edu/tufts/hrilab/action/asl/domains/boxbot.asl"); 
        Path belieffilePath = Paths.get("/mnt/c/Users/hamid/Desktop/myboxbot/diarc/core/src/main/resources/config/edu/tufts/hrilab/belief/agents/boxbot.pl"); 

        String aslContents = "";
        String beliefContents = "";

        try {
            aslContents = Files.readString(aslfilePath);
            beliefContents = Files.readString(belieffilePath);
        } catch (IOException e) {
            log.error("Error reading input files for LLM prompt.", e);
            return; // Exit early if files couldn't be read
        }

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
            
            
            log.info("Here is the observations {}", obs);
        } catch (TRADEException e) {
            log.error("Failed to obtain getObservation service", e);
            return;
        }

            String promptTemplate =  """
            You are an AI planner for an autonomous robot agent. Your task is to produce a logically valid, efficient, and **creatively reasoned** plan — a sequence of necessary actions — that the agent can perform to achieve its goal(completely) given the current state of its environment (the position fields do not need to exactly match up for the agent to be at an object; rely on your memory of this conversation and the ROBOT'S INITIAL BELIEFS to determine what is in the agent's reach, as long as it is close enough in position).

            You must:
            - Before proposing any actions, assess if the TASK TO ACCOMPLISH is already met by the CURRENT ENVIRONMENT STATE. The plan should always reflect only the remaining necessary actions starting from the current status (given by current environment state).
            - **Only use actions defined in the .asl file**, and use them **exactly** as named (e.g., `standup()` if it has no argument and combine(blue, red) if it does (NO OTHER FORMAT)).
            - Respect all preconditions for each action. A precondition must be satisfied at the time the action is executed. A precondition like `pre: isAtBox` means that `isAtBox` must be in the current environment state when the action is performed.
            - A precondition prefixed with `~` (e.g., `~isHoldingBox`) means the fact must be **false** (i.e., not present in the current state).
            - Effects (under `success:`) are facts that become true after the action is successfully executed. But they are not always the only effects of the action being performed — use **common sense physical reasoning** to infer additional likely effects when the environment changes.
            - Given the current observations and the goal, generate a complete sequence of actions that will achieve all parts of the goal exactly as stated. The plan is only valid if all goal conditions are satisfied at the end, without leaving any part incomplete.
            - The agent's current state is defined by the environment state **plus its beliefs**. If something is not explicitly in the observations, but is present in the beliefs, you may still use it when planning.
            - Do not just add actions randomly. Performing an action must serve a purpose, bringing the agent closer to achieving its goal — even if the path to the goal involves creative intermediate steps.
            - FOLLOW THE FORMAT BELOW **EXACTLY** (no extra text or explanation): 
            action1()
            obs json you expect after action1 is performed
            action2()
            obs json you expect after action2 is performed

            You are encouraged to:
            - Use **creative chaining** of available actions to satisfy missing preconditions.
            - Use **objects, tools, or other entities from the beliefs file** even if they are not mentioned in the observations, if logically relevant.
            - Consider indirect strategies: if the goal is blocked, take steps to make it achievable (e.g., moving obstacles, opening containers, repositioning yourself).
            - Exploit opportunities to perform necessary setup actions early so later actions become simpler.

            TASK TO ACCOMPLISH:
            {GOAL}

            CURRENT ENVIRONMENT STATE (observations):  
            {OBS}

            OUTPUT FORMAT:
            Return a **valid and logically ordered** plan using only the actions from the .asl file. Each step should include:

            1. The action name (e.g., `moveToBox()`) (DO NOT INCLUDE ANY OTHER CHARS ON THIS LINE. JUST ACTION NAME AND PARENTHESES, **EVEN IF THERE ARE NO ARGUMENTS STILL INCLUDE '()'**)
            2. The ENVIRONMENT STATE (observations) JSON (all in one line, no \n characters in there.):
            - Edit the JSON's **boolean**/**string** observation values to represent what you expect them to be after the action is executed. **Use `null` for values that are irrelevant or not booleans or strings, including but not limited to all position values**.
            - USE THE ORIGINAL ENVIRONMENT OBSERVATION TO GUIDE YOUR STARTING VALUES (maintain which are booleans, which are strings, etc).

            - Only include fields from the observation JSON, in the same order.
            - Do **not** explain the reasoning.
            - The plan must be valid: all preconditions must be met at the time each action is executed.
            - Only include actions from the .asl file and use them exactly as named.
            - IMPORTANT: **Use your common sense, the success effects, and the robot's beliefs to predict what the observation boolean fields would be. Then ensure the predicted observation values allow the next action’s preconditions to be met.**

            ROBOT'S INITIAL BELIEFS (.pl format):  
            {BELIEFS}

            AVAILABLE ACTIONS (.asl format):  
            {ASL}

            EXAMPLE: 
            Given the goal clean the room, and actions moveto(location), pickup(), putdown() among others, and observation {isHoldingObject: false, agentLocation: [10,10] , toyLocation: [50,50] , chestLocation: [100,100]}, the plan would be:
            moveto(50,50)
            {isHoldingObject: false, agentLocation: null , toyLocation: null , chestLocation: null}
            pickup()
            {isHoldingObject: true, agentLocation: null , toyLocation: null , chestLocation: null}
            moveto(100,100)
            {isHoldingObject: true, agentLocation: null , toyLocation: null , chestLocation: null}
            putdown()
            {isHoldingObject: false, agentLocation: null , toyLocation: null , chestLocation: null}

            ACCEPTABLE format examples:
            action that takes no arguments: standup() 
            action that takes one argument: goto(door)
            action that takes two arguments: mix(blue, red)

            UNACCEPTABLE format examples (do not make these mistakes):
            action that takes no arguments: standup 
            action that takes one argument: goto(door())
            action that takes two arguments: mix(blue, red()


            """;

        

        OpenaiResponses response;
        String new_user_id = "";
        String answer = "";
        String filledPrompt = "";

     try {
        filledPrompt = promptTemplate
            .replace("{GOAL}", input)
            .replace("{OBS}", obs)
            .replace("{BELIEFS}", beliefContents)
            .replace("{ASL}", aslContents);
        response = TRADE.getAvailableService(new TRADEServiceConstraints()
            .name("responses")
            .argTypes(String.class, String.class, String.class))
            .call(OpenaiResponses.class, "o3-mini-2025-01-31", filledPrompt, null);
        
        log.info("answer: " + response);
        new_user_id = response.id;
        
        if (response.output != null && !response.output.isEmpty()) {
            log.info(" output: " + response.output);
            
            // Iterate through the output to find the 'message' type
            for (OpenaiResponses.Output outputItem : response.output) {
                if ("message".equals(outputItem.type)) {
                    if (outputItem.content != null && !outputItem.content.isEmpty()) {
                        answer = outputItem.content.get(0).text;
                        log.info("Answer from LLM: " + answer); // Add a log for the successful answer
                        break; // Exit the loop once the message is found
                    } else {
                        log.error("Message output has no content.");
                    }
                }
            }
        } else {
            log.error("No output in response.");
        }
    } catch (TRADEException e) {
        log.error("Error calling LLM service for parseGoals.", e);
        return;
    }

        

        String[] actions = answer.split("\n");
        // 1) Safely look up the submitGoal service
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

        // First submit an initialization goal to ensure environment is ready
        Predicate initPredicate = Factory.createPredicate("getObservation", Factory.createSymbol("self"));
        try {
            // Submit the initialization goal
            long initGoalId = submitGoalSvc.call(Long.class, initPredicate);
            log.info("Submitted initialization goal [{}]: {}", initGoalId, initPredicate);
            
            // Give initialization time to complete
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.warn("Sleep interrupted while waiting for initialization", e);
                Thread.currentThread().interrupt(); // Restore interrupted status
            }
            
            // Now proceed with submitting the action goals...
            
        } catch (TRADEException e) {
            log.error("Failed to submit initialization goal:", e);
        }

        // 2) Regex to capture action name + args
        Pattern pat = Pattern.compile("^(\\w+)\\(([^)]*)\\)$");

        int actionIndex = 0;

        String new_obs = "";
        String new_knowledge = "";
        String obs_answer = "";


        while (actionIndex < actions.length) {
            String line = actions[actionIndex].trim();
            line = line.replaceAll("[^a-zA-Z()]", "");

            log.info("Here is the sanitized action: {}", line);

            Matcher m = pat.matcher(line);
            if (!m.matches()) {
                log.warn("Skipping unparsable action: {}", line);
                actionIndex++;  
                continue;
            }

            String actionName = m.group(1);
            String argsGroup  = m.group(2).trim();

            List<Symbol> symbols = new ArrayList<>();
            symbols.add(Factory.createSymbol("self"));
            if (!argsGroup.isEmpty()) {
                for (String tok : argsGroup.split("\\s*,\\s*")) {
                    symbols.add(Factory.createSymbol(tok));
                }
            }


            Predicate predicate = Factory.createPredicate(actionName, symbols);
            GoalStatus state = GoalStatus.fromString("FAILED"); 
            

            try {
                long goalId = submitGoalSvc.call(long.class, predicate);
                log.info("Submitted [{}]: {}", goalId, predicate);

                while (state != GoalStatus.fromString("SUCCEEDED")) {
                    try {
                        Thread.sleep(30);
                        // Get the goal status using the registered TRADE service
                        try {
                            
                            state = TRADE.getAvailableService(new TRADEServiceConstraints().name("getGoalStatus").argTypes(Long.class)).call(GoalStatus.class, goalId);
                        } catch (TRADEException e) {
                            log.error("Failed to obtain submitGoal service", e);
                            return;
        
                        }

                        
                    } catch (InterruptedException e) {
                        log.warn("Sleep interrupted while waiting for goal status", e);
                        Thread.currentThread().interrupt(); // Restore interrupted status
                    }
                }

                //get the observations
                
                try {
                    new_obs = TRADE.getAvailableService(new TRADEServiceConstraints().name("getObservationJson").argTypes()).call(String.class);

                    try{
                        JsonNode rootNode = objectMapper.readTree(new_obs);
                        JsonNode observationNode = rootNode.get("observation");
                        new_obs = observationNode.toString();
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                  
                    
                    log.info("Here is the observation {}", new_obs);
                } catch (TRADEException e) {
                    log.error("Failed to obtain getObservation service", e);
                    return;
                }

                //prompt the llm to see if the opbservations are as it expected

                List<String> mismatches = ObservationComparator.getMismatches(actions[actionIndex + 1], new_obs);

                if (!mismatches.isEmpty()) {
                    mismatches.forEach(System.out::println);
                


                
                    String obs_prompt = 
                        "You are analyzing whether an action worked as expected. Compare what you predicted would happen with what actually happened.\n\n" +

                        "**The agent was at environment state: " + obs + "\n when it performed action " + line + " as a step towards achieving goal:  " + input + " .\nAfter the action was performed the agent noticed that the environment state did not change as expected in the following ways: \n" + mismatches + " \n" + "It intern observed that the REAL state of its environment after performing that action is: " + new_obs + "\n"  +
                        
                        "Remember, the expected observation is invalid. It was what you expected when you made the plan, and now you are learning what you were wrong about based on the real observation." +
                        "If your previous belief was incorrect, you must deduce the correct environmental rule using the real observation only.\n" +
                    
                        
                        "ANALYSIS PROCESS:\n" +
                        "1. Compare expected vs actual observations and current vs previous actual observations\n" +
                        "Make sure not to confuse what you expected (wrong) with what really happened (current and prev real obs) \n\n" +
                        " An observation statement: true means the statement is true and statement: false means that statement is false in the environment. Hence isDay: true means it is daytime, and isDay: false means it is nighttime. \n\n" +
                        
                        "DECISION LOGIC:\n" +
                        "2. If there's a mismatch in any meaningful field → Learn from it\n\n" +
                        
                        "LEARNING FORMAT:\n" +
                        "1-2 sentences with the most logical conclusion based on the observations and evidence - only speculate if no clear logical conclusion exists but there is a clear common sense explenation. The conclusion must be a factual sentence about the environment in concise simple language that when added to the original goal helps the agent make a better plan to achieve its goal in the next planning round. The conclusion must be worded **in terms of observation fields, NOT actions**. \n" + 

                        "If you need to speculate, make sure to be clear about that in your answer. For example, use phrases like 'it might' or 'it is possible'. \n" +
                        
                        "CRITICAL RULES:\n" +
                        "- make sure your answer is worded in a way that is relevant to the agent's goal and considers the action that was just performed as its main idea\n" +
                        "- IF AND ONLY IF the mismatch does not throw the agent off its plan's track, simply state the learned fact. Do not speculate or make things up.\n"+
                        "- think and provide the best answer as shown above\n" +
                        "- Do not generalize based on common sense if the actual observation contradicts it\n" +
                        "- Base conclusions on logical deduction from the evidence/observation fields\n" +
                        "- Don't list field comparisons\n" +
                        "- Don't mention non boolean fields\n" +
                        "- Use your memory of previous observations when needed to make a logical conclusion about how different components of the environment work and interact (the conclusion is never about the agent, just about the environment components)\n\n"+
                        
                        "EXAMPLE:\n" +
                        "Goal is to move the car" +
                        "Expected: carmoving: true\n" +
                        "Actual: carmoving: false, parkingbreakon: true\n" +
                        "Actual from previous action step: carmoving: true, parkingbreakon: false" +
                        "→ Output: `The car will not move while the parking break is on. `\n\n" ;


                String temp = "";

                    try {
                        Thread.sleep(40000);

                        try {
                            response = TRADE.getAvailableService(new TRADEServiceConstraints()
                                        .name("responses")
                                        .argTypes(String.class, String.class, String.class))
                                        .call(OpenaiResponses.class, "o3-mini-2025-01-31", obs_prompt , new_user_id);

                            new_user_id = response.id;

                            if (response.output != null && !response.output.isEmpty()) {
                                
                                // Iterate through the output to find the 'message' type
                                for (OpenaiResponses.Output outputItem : response.output) {
                                    if ("message".equals(outputItem.type)) {
                                        if (outputItem.content != null && !outputItem.content.isEmpty()) {
                                            temp = outputItem.content.get(0).text;
                                            break; // Exit the loop once the message is found
                                        } else {
                                            log.error("Message output has no content.");
                                        }
                                    }
                                }
                            } else {
                                log.error("No output in response.");
                            }
                        
                        } catch (TRADEException e) {
                            log.error("Error calling LLM service for parseGoals.", e);
                            return;
                        }

                        log.info("Answer: {}" , temp);

                    } catch (InterruptedException e) {
                                // Handle the case where the sleep is interrupted
                                Thread.currentThread().interrupt(); // Restore the interrupted status
                    }

                    obs_answer += temp; 
                    obs_answer += " " ;

                    try {
                            response = TRADE.getAvailableService(new TRADEServiceConstraints()
                                        .name("responses")
                                        .argTypes(String.class, String.class, String.class))
                                        .call(OpenaiResponses.class, "o3-mini-2025-01-31", "Now, following the same rules as above, consider all the facts below that the agent has learned from interacting with its environment and make a coherent, concise statement (up to 3 sentences) in simple and clear language, about the environment that draws a logical conclusion from all these statements, worded in terms of the environment elements and the agent's goal to acomplish. If the agent made a speculation that was wrong, make sure to accurately reflect that. \n GOAL: " + input + " \n LEARNED FROM ENVIRONMENT: " + obs_answer , new_user_id);

                            new_user_id = response.id;

                            if (response.output != null && !response.output.isEmpty()) {
                                
                                // Iterate through the output to find the 'message' type
                                for (OpenaiResponses.Output outputItem : response.output) {
                                    if ("message".equals(outputItem.type)) {
                                        if (outputItem.content != null && !outputItem.content.isEmpty()) {
                                            temp = outputItem.content.get(0).text;
                                            break; // Exit the loop once the message is found
                                        } else {
                                            log.error("Message output has no content.");
                                        }
                                    }
                                }
                            } else {
                                log.error("No output in response.");
                            }

                        } catch (TRADEException e) {
                            log.error("Error calling LLM service for parseGoals.", e);
                            return;
                        }

                        log.info("Answer: {}" , temp);

                    try {
                        Thread.sleep(40000);

                        try {

                            String second_prompt = """
                            You are an AI planner for an autonomous robot agent. Your task is to produce a logically valid and creative plan — a sequence of necessary actions — that the agent can perform to achieve its goal.

                            You must:
                            
                            - Only use actions defined in the .asl file, and use them **exactly** as named (e.g., `pressswitch()`).
                            - Respect all preconditions for each action. A precondition must be satisfied at the time the action is executed. A precondition like `pre: isAtBox` means that `isAtBox` must be in the current environment state when the action is performed.
                            - A precondition prefixed with `~` (e.g., `~isHoldingBox`) means the fact must be **false** (i.e., not present in the current state).
                            - Effects (under `success:`) are facts that become true after the action is successfully executed. But they are not always the only effects of the action being performed (e.g., if the agent performs spillwater() on a table, isTableWet() would be true. Hence use common sense when it comes to objects in the agent's environment interacting ).
                            - The agent's initial state is defined by the environment state and its beliefs.
                            - Do not just add actions. Performing an action must serve a purpose, bringing the agent closer to achieving its goal.
                            - Make sure to refer to your previous plan and learn from your mistakes, but do not change things you were right about.

                            You are encouraged to:
                            - Use **common sense** and reason **creatively** with the available actions to achieve the goal, even if the goal is not directly achievable with a single action.
                            - Chain together actions in ways that produce intermediate conditions needed for later steps.
                            - Use tools, objects, or the environment in creative ways — for example, moving to intermediate locations if necessary.

                            OUTPUT FORMAT:
                            Return a **valid and logically ordered** plan using only the actions from the .asl file. Each step should include:

                            1. The action name (e.g., `moveToBox()`) (Do not include any other text on this line, and even if the action takes no arguments, include the parentheses)
                            2. The ENVIRONMENT STATE (observations) JSON (all in one line, no
                             characters in there.) :
                            - edit the json's boolean observation values to represent what you expect them to be after the action is executed. **Use `null` for values that are irrelevant or not booleans (true/false) **.


                            - Only include fields from the observation JSON, in the same order.
                            - Do **not** explain the reasoning.
                            - The plan must be valid: all preconditions must be met at the time each action is executed.
                            - Only include actions from the .asl file and use them exactly as named.
                            - IMPORTANT: **Use your common sense and the success fields to predict what the observation boolean fields would be. Then use the expected observation boolean field to make sure the pre conditions for actions you want to perform next are met.** Make sure to base all your expected observation fields on 1. What you have learned through experience from your environment (priority), carefully, without over generalizing, and 2. Common sense and why you chose the actions to achieve the goal. **Make sure to refer to the conversation memory and learn from your mistakes, but do not change things you were right about.**


                            TASK TO ACCOMPLISH:
                            {GOAL}

                            LAST ACTION PERFOMED FROM PREVIOUS PLAN:
                            {PREV_PLAN}

                            OBSERVATION WHILE THAT ACTION WAS PERFORMED:
                            {PREV_OBS}

                            LEARNED FROM ENVIRONMENT AND PREVIOUS MISTAKES: 
                            {KNOWLEDGE}

                            CURRENT ENVIRONMENT STATE (observations):  
                            {OBS}

                            AVAILABLE ACTIONS (.asl format):  
                            {ASL}

                            ROBOT'S INITIAL BELIEFS (.pl format):  
                            {BELIEFS}


                            EXAMPLE: 
                            Given the goal clean the room, and actions moveto(location), pickup(object), putdown(object) among others, and observation {isHoldingObject: false, agentLocation: [10,10] , toyLocation: [50,50] , chestLocation: [100,100]}, the plan would be:
                            moveto([50,50])
                            {isHoldingObject: false, agentLocation: null , toyLocation: null , chestLocation: null}
                            pickup(toy)
                            {isHoldingObject: true, agentLocation: null , toyLocation: null , chestLocation: null}
                            moveto([100,100])
                            {isHoldingObject: true, agentLocation: null , toyLocation: null , chestLocation: null}
                            putdown(toy)
                            {isHoldingObject: false, agentLocation: null , toyLocation: null , chestLocation: null}
                                    
                                    """;

                            String newFilledPrompt = promptTemplate
                                .replace("{GOAL}", input + " with background knowledge that: " + temp + " \nMake sure 1. not to repeat plans that you have already tried(use conversation memory)")
                                .replace("{OBS}", new_obs)
                                .replace("{BELIEFS}", beliefContents)
                                .replace("{ASL}", aslContents);

                            newFilledPrompt += " IMPORTANT: make sure to base all your expected observation fields on 1.What you have learned through experience from your environment (priority), carefully, without over generalizing, and 2. Common sense and why you chose the actions to achieve the goal. **Make sure to refer to the conversation memory and learn from your mistakes, but do not change things you were right about.** ";
                            
                            String new_prompt = 
                            "You are an AI planner for a robot agent. Your task is to generate a precise and executable sequence of actions to achieve a specific goal.\n" +
                            "You must strictly obey action preconditions and only use actions that are defined in the available .asl file.\n\n" +

                            "TASK TO ACCOMPLISH:\n" + input + "\n\n" +

                            "LEARNED FROM OBSERVATION: " + answer + "\n\n" +

                            "CURRENT ENVIRONMENT STATE:\n" + new_obs + "\n\n" +

                            "ROBOT'S INITIAL BELIEFS (.pl format):\n" + beliefContents + "\n\n" +

                            "AVAILABLE PRIMARY ACTIONS (.asl format):\n" + aslContents + "\n\n" +

                            "ACTION FORMAT STRUCTURE (.asl):\n" +
                            "Each action has the form:\n" +
                            "actionname[\"description\"]() {\n" +
                            "  conditions: {\n" +
                            "    pre: [precondition1];\n" +
                            "    pre infer: [precondition2];\n" +
                            "  }\n" +
                            "  effects: {\n" +
                            "    success: [effect1];\n" +
                            "  }\n" +
                            "}\n\n" ;


                            response = TRADE.getAvailableService(new TRADEServiceConstraints()
                                        .name("responses")
                                        .argTypes(String.class, String.class, String.class))
                                        .call(OpenaiResponses.class, "o3-mini-2025-01-31" , newFilledPrompt , new_user_id);

                            new_user_id = response.id;
                       

                            if (response.output != null && !response.output.isEmpty()) {
                                
                                // Iterate through the output to find the 'message' type
                                for (OpenaiResponses.Output outputItem : response.output) {
                                    if ("message".equals(outputItem.type)) {
                                        if (outputItem.content != null && !outputItem.content.isEmpty()) {
                                            answer = outputItem.content.get(0).text;
                                            break; // Exit the loop once the message is found
                                        } else {
                                            log.error("Message output has no content.");
                                        }
                                    }
                                }
                            } else {
                                log.error("No output in response.");
                            }




                            log.info("Prompt LLM got: " + newFilledPrompt);
                        } catch (TRADEException e) {
                            log.error("Error calling LLM service for parseGoals.", e);
                            return;
                        }

                        

                        log.info("Answer from LLM second round: " + answer);
                        actions = answer.split("\n");
                        actionIndex = 0; // <-- RESTART plan execution with new list
                        continue;
                            
                        } catch (InterruptedException e) {
                            // Handle the case where the sleep is interrupted
                            Thread.currentThread().interrupt(); // Restore the interrupted status
                        }
                    
                }
                obs = new_obs;     
                
                actionIndex ++; // Only go to next step if plan still valid


                
            } catch (TRADEException e) {
                log.error("Failed to submit goal or get goal status for {}:", predicate, e);
            }


        }
        

        return;


    }
    
}
