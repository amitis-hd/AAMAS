package edu.tufts.hrilab.boxbot.actions;

public class SqueezeMotion extends Active {
    private String objectName;

    public SqueezeMotion(String objectName) {
        this.objectName = objectName;
        this.maxResponseWait = 5000;
    }

    @Override
    public String getCommand() {
        return String.format("{\"action\": \"SQUEEZE\", \"object_name\": \"%s\"}", 
            this.objectName);
    }
}