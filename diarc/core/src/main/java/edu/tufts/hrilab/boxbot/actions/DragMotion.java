package edu.tufts.hrilab.boxbot.actions;

public class DragMotion extends Active {
    private String objectName;
    private String destinationName;

    public DragMotion(String objectName, String destinationName) {
        this.objectName = objectName;
        this.destinationName = destinationName;
        this.maxResponseWait = 5000;
    }

    @Override
    public String getCommand() {
        return String.format("{\"action\": \"DRAG\", \"object\": \"%s\", \"destination\": \"%s\"}", 
            this.objectName, this.destinationName);
    }
}