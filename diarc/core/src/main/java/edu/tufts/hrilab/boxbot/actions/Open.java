package edu.tufts.hrilab.boxbot.actions;


public class Open extends Active {

    public Open() { this.maxResponseWait = 1000; }

    @Override
    public String getCommand() {
        return "OPEN";
    }
    
}
