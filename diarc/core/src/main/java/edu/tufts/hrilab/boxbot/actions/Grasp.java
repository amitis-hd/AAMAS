package edu.tufts.hrilab.boxbot.actions;

public class Grasp extends Active {
    private String objectName;

    public Grasp(String objectName) { 
        // Remove the super("MOVE") call - Active has no-arg constructor
        this.objectName = objectName;
        this.maxResponseWait = 5000; 
    }

    @Override
    public String getCommand() {
        return String.format("{\"action\": \"GRASP\", \"object_name\": \"%s\"}", this.objectName);
    }
}