package edu.tufts.hrilab.boxbot.actions;

public class Put extends Active {
    private String object_name;
    private String destination_name;

    public Put(String object_name, String destination_name) { 
        // Remove the super("MOVE") call - Active has no-arg constructor
        this.object_name = object_name;
        this.destination_name = destination_name;
        this.maxResponseWait = 5000; 
    }

    @Override
    public String getCommand() {
        return String.format("{\"action\": \"PUT\", \"object_name\": \"%s\", \"destination_name\": \"%s\"}", this.object_name, this.destination_name);
    }
}