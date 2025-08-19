package edu.tufts.hrilab.boxbot.actions;


public class Push extends Active {

    public Push() { this.maxResponseWait = 1000; }

    @Override
    public String getCommand() {
        System.out.println("Push action constructor called");
        return "PUSH";
    }
    
}
