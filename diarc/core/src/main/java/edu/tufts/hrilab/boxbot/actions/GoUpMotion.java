package edu.tufts.hrilab.boxbot.actions;

public class GoUpMotion extends Active {
    private String objectName;

    public GoUpMotion(String objectName) {
        this.objectName = objectName;
        this.maxResponseWait = 5000;
    }

    @Override
    public String getCommand() {
        return String.format("{\"action\": \"GOUP\", \"object\": \"%s\"}", 
            this.objectName);
    }
}