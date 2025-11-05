package edu.tufts.hrilab.boxbot.actions;

public class GoDownMotion extends Active {

    public GoDownMotion() {
        this.maxResponseWait = 5000;
    }

    @Override
    public String getCommand() {
        return "{\"action\": \"GODOWN\"}";
    }
}