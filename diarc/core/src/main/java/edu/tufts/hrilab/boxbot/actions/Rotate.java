package edu.tufts.hrilab.boxbot.actions;


public class Rotate extends Active {
    private int degree;

    public Rotate(int degree) { 
        this.degree = degree;
        
        this.maxResponseWait = 1000; }

    @Override
    public String getCommand() {
        return String.format("{\"action\": \"ROTATE\", \"degree\": \"%d\"}", this.degree);
    }
    
}
