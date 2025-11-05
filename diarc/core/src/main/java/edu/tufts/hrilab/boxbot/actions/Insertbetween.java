package edu.tufts.hrilab.boxbot.actions;


public class Insertbetween extends Active {
    private String objectName;
    private String leftObj;
    private String rightObj;

    public Insertbetween(String objectName, String leftObj, String rightObj) { 
        this.objectName = objectName;
        this.leftObj = leftObj;
        this.rightObj = rightObj;
        this.maxResponseWait = 1000; }

    @Override
    public String getCommand() {
        return String.format("{\"action\": \"INSERTBETWEEN\", \"object_name\": \"%s\" , \"left_obj\": \"%s\" , \"right_obj\": \"%s\"}", this.objectName, this.leftObj, this.rightObj);
    }
    
}
