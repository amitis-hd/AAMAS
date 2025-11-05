package edu.tufts.hrilab.boxbot.actions;

public class PutUnderMotion extends Active {
    private String objectName;
    private String destinationName;

    public PutUnderMotion(String objectName, String destinationName) {
        this.objectName = objectName;
        this.destinationName = destinationName;
        this.maxResponseWait = 5000;
    }

    @Override
    public String getCommand() {
        return String.format("{\"action\": \"PUTUNDER\", \"object\": \"%s\", \"destination\": \"%s\"}", 
            this.objectName, this.destinationName);
    }
}