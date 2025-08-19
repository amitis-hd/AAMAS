package edu.tufts.hrilab.boxbot.actions;

public class Close extends Active {

    public Close() { this.maxResponseWait = 1000; }

    @Override
    public String getCommand() {
        return "CLOSE";
    }
    
}
