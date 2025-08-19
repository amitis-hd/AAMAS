/*
 * Copyright © Thinking Robots, Inc., Tufts University, and others 2024.
 */

package edu.tufts.hrilab.boxbot.actions;

import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.tufts.hrilab.boxbot.BoxBotObservation;
import edu.tufts.hrilab.boxbot.GameAction;

public abstract class Active extends GameAction {
    protected ParsedResponse parsedResponse;
    private static final Logger log = LoggerFactory.getLogger(Active.class);


    protected class ParsedResponse {
        CommandResult command_result;
        int step;
        boolean gameOver;
        BoxBotObservation observation;

        protected class CommandResult {
            String command;
            String result;
            String message;
            double stepCost;
        }
    }

    public Active(){ }

    public BoxBotObservation getObservation() {
        //log.info("Raw response string: {}", this.rawResponse, "\n");
        //log.info("Here is the observation {}", this.parsedResponse.observation, "\n");
        return this.parsedResponse.observation;
    }

    public boolean getSuccess() {
        return this.parsedResponse.command_result.result.equals("SUCCESS");
    }

    public double getStepCost() {
        return this.parsedResponse.command_result.stepCost;
    }

    public String getMessage() {return this.parsedResponse.command_result.message; }

    public boolean getGameOver() { return this.parsedResponse.gameOver; }

    protected void parseResponse() {
        this.parsedResponse = new Gson().fromJson(this.rawResponse, ParsedResponse.class);
    }
}
