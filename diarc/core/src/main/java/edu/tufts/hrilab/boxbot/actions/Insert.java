package edu.tufts.hrilab.boxbot.actions;


public class Insert extends Active {
    private String objectName;
    private String destName;

    public Insert(String objectName, String destName) { 
        this.objectName = objectName;
        this.destName = destName;
        this.maxResponseWait = 1000; }

    @Override
    public String getCommand() {
        return String.format("{\"action\": \"INSERT\", \"object_name\": \"%s\" , \"destination_name\": \"%s\"}", this.objectName, this.destName);
    }
    
}
