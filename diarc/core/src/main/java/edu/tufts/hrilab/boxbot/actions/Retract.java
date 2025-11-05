package edu.tufts.hrilab.boxbot.actions;


public class Retract extends Active {

    public Retract() { this.maxResponseWait = 1000; }

    @Override
    public String getCommand() {
        return "RETRACT";
    }
    
}
