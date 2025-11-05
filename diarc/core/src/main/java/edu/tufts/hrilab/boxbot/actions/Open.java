package edu.tufts.hrilab.boxbot.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Open extends Active {
    private static final Logger log = LoggerFactory.getLogger(Open.class);
    private String objectName;

    public Open(String objectName) {
        //log.info("!!! Open constructor called with objectName: {}", objectName);
        this.objectName = objectName;
        this.maxResponseWait = 1000;
        //log.info("!!! Open constructor completed");
    }

    @Override
    public String getCommand() {
        String command = String.format("{\"action\": \"OPEN\", \"object_name\": \"%s\"}", this.objectName);
        //log.info("!!! Open.getCommand() called, returning: {}", command);
        return command;
    }
}