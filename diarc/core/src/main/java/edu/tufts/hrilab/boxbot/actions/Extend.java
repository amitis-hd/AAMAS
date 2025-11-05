package edu.tufts.hrilab.boxbot.actions;


public class Extend extends Active {

    public Extend() { this.maxResponseWait = 1000; }

    @Override
    public String getCommand() {
        return "EXTEND";
    }
    
}
