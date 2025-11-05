package edu.tufts.hrilab.boxbot.actions;

public class PierceMotion extends Active {
    private String toolName;
    private String targetName;

    public PierceMotion(String toolName, String targetName) {
        this.toolName = toolName;
        this.targetName = targetName;
        this.maxResponseWait = 5000;
    }

    @Override
    public String getCommand() {
        return String.format("{\"action\": \"POKE\", \"tool\": \"%s\", \"target\": \"%s\"}", 
            this.toolName, this.targetName);
    }
}