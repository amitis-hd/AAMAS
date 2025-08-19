package edu.tufts.hrilab.boxbot.actions;


public class Lift extends Active {

    public Lift() { this.maxResponseWait = 1000; }

    @Override
    public String getCommand() {
        return "LIFT";
    }
    
}
