package edu.tufts.hrilab.boxbot.actions;


public class Insert extends Active {

    public Insert() { this.maxResponseWait = 1000; }

    @Override
    public String getCommand() {
        return "INSERT";
    }
    
}
