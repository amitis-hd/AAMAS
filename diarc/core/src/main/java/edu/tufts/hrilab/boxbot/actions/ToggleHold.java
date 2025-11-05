/*
 * Copyright © Thinking Robots, Inc., Tufts University, and others 2024.
 */

package edu.tufts.hrilab.boxbot.actions;

public class ToggleHold extends Active {
    private String objectName;

    public ToggleHold(String objectName) { 
        this.objectName = objectName;
        this.maxResponseWait = 200000; }

    @Override
    public String getCommand() {
        System.out.println("togglehold action constructor called");
        return String.format("{\"action\": \"TOGGLE_HOLD\", \"object_name\": \"%s\"}", this.objectName);
    }
}
