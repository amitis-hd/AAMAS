package edu.tufts.hrilab.boxbot.actions;

public class Close extends Active {
    private String objectName;

    public Close(String objectName) { 
        this.objectName = objectName;
        this.maxResponseWait = 1000; }

    @Override
    public String getCommand() {
        return String.format("{\"action\": \"CLOSE\", \"object_name\": \"%s\"}", this.objectName);
    }
    
}

