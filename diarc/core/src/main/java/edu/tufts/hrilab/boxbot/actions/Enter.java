package edu.tufts.hrilab.boxbot.actions;


public class Enter extends Active {

    public Enter() { this.maxResponseWait = 1000; }

    @Override
    public String getCommand() {
        return "ENTER";
    }
    
}

