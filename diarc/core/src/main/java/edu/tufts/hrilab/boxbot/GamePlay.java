package edu.tufts.hrilab.boxbot;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.tufts.hrilab.socket.SocketConnection;
import edu.tufts.hrilab.boxbot.actions.Active;

public class GamePlay {
    SocketConnection sock;
    BoxBotObservation observation;
    private static final Logger log = LoggerFactory.getLogger(GamePlay.class);
    
    public GamePlay(int socketPort){
        try {
            this.sock = new SocketConnection(socketPort);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    
    public void perform(GameAction action) {
        this.sock.sendCommand(action.getCommand());
        String response = this.sock.waitedResponse(1000);

        action.insertResponse(response);

        this.observation = ((Active)action).getObservation();
    }

    // public boolean atChest() {
    //     this.sock.sendCommand("AT_CHEST");
    //     String response = this.sock.waitedResponse(20000);
    //     return response != null && response.contains("\"result\":\"SUCCESS\"");
    // }
}
