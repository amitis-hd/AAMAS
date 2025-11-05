package edu.tufts.hrilab.boxbot.actions;

public class Move extends Active {
    private String objectName;

    public Move(String objectName) { 
        // Remove the super("MOVE") call - Active has no-arg constructor
        this.objectName = objectName;
        this.maxResponseWait = 5000; 
    }

    @Override
    public String getCommand() {
        return String.format("{\"action\": \"MOVE\", \"object_name\": \"%s\"}", this.objectName);
    }
}