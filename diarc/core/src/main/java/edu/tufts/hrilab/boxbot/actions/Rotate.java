package edu.tufts.hrilab.boxbot.actions;


public class Rotate extends Active {

    public Rotate() { this.maxResponseWait = 1000; }

    @Override
    public String getCommand() {
        return "ROTATE";
    }
    
}
