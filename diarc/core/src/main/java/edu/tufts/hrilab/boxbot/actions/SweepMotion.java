package edu.tufts.hrilab.boxbot.actions;

public class SweepMotion extends Active {
    private String toolName;
    private String targetName;
    private String surfaceName;

    public SweepMotion(String toolName, String targetName, String surfaceName) {
        this.toolName = toolName;
        this.targetName = targetName;
        this.surfaceName = surfaceName;
        this.maxResponseWait = 5000;
    }

    @Override
    public String getCommand() {
        return String.format("{\"action\": \"SWEEP\", \"tool\": \"%s\", \"target\": \"%s\", \"surface\": \"%s\"}", 
            this.toolName, this.targetName, this.surfaceName);
    }
}