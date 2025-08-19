package edu.tufts.hrilab.boxbot;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.thinkingrobots.trade.TRADEService;
import ai.thinkingrobots.trade.TRADE;
import ai.thinkingrobots.trade.TRADEException;
import ai.thinkingrobots.trade.TRADEServiceConstraints;
import ai.thinkingrobots.trade.TRADEService;
import edu.tufts.hrilab.action.annotations.Observes;
import edu.tufts.hrilab.action.justification.ConditionJustification;
import edu.tufts.hrilab.action.justification.Justification;
import edu.tufts.hrilab.action.annotations.Observes;
import edu.tufts.hrilab.diarc.DiarcComponent;
import edu.tufts.hrilab.fol.Predicate;
import edu.tufts.hrilab.fol.Variable;
import edu.tufts.hrilab.fol.Term;
import edu.tufts.hrilab.fol.Factory;
import edu.tufts.hrilab.fol.Symbol;
import edu.tufts.hrilab.interfaces.BoxBotSimulatorInterface;
import edu.tufts.hrilab.util.Util;
import edu.tufts.hrilab.boxbot.actions.East;
import edu.tufts.hrilab.boxbot.actions.North;
import edu.tufts.hrilab.boxbot.actions.West;
import edu.tufts.hrilab.boxbot.actions.South;
import edu.tufts.hrilab.boxbot.actions.ToggleHold;
import edu.tufts.hrilab.boxbot.actions.Push;
import edu.tufts.hrilab.boxbot.actions.Lift;
import edu.tufts.hrilab.boxbot.actions.Insert;
import edu.tufts.hrilab.boxbot.actions.Rotate;
import edu.tufts.hrilab.boxbot.actions.Open;
import edu.tufts.hrilab.boxbot.actions.Close;
import edu.tufts.hrilab.boxbot.actions.GetObservation;
import edu.tufts.hrilab.boxbot.actions.Active;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Component facilitating interaction with a BoxBot simulation.
 */
public class BoxBotComponent extends DiarcComponent implements BoxBotSimulatorInterface {
    protected int socketPort = 9000;
    protected GamePlay game;
    private static final Logger log = LoggerFactory.getLogger(BoxBotComponent.class);

    /**
     * This number represents how far from the target position the robot can be and be
     * considered "at the target".
     */
    private static final int POSITION_TOLERANCE = 5;

    public BoxBotComponent() {
    }

    @Override
    protected void init() {
        super.init();
        this.game = new GamePlay(this.socketPort);
        Util.Sleep(500);
        this.game.perform(new GetObservation());
        if (this.game.observation == null) {
            Util.Sleep(500);
            // Try to re-establish connection or get a new observation
            this.game.perform(new GetObservation());
        }
    }

    /*************************************
     * OBSERVERS
     *************************************/


    @Override
    public List<HashMap<Variable, Symbol>> northOfBox(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();

        if (this.game.observation.robotPos[1] <= this.game.observation.boxPos[1]) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> southOfBox(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();

        if (this.game.observation.robotPos[1] + this.game.observation.robotHeight >= this.game.observation.boxPos[1] + this.game.observation.boxHeight) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> eastOfBox(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();

        if (this.game.observation.robotPos[0] + this.game.observation.robotWidth >= this.game.observation.boxPos[0] + this.game.observation.boxHeight) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> westOfBox(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();

        if (this.game.observation.robotPos[0] + this.game.observation.robotWidth <= this.game.observation.boxPos[0]) {
            list.add(new HashMap<>());
        }
        return list;
    }




     @Override
    public List<HashMap<Variable, Symbol>> northOfChest(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();

        if (this.game.observation.robotPos[1] <= this.game.observation.chestPos[1]) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> southOfChest(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();

        if (this.game.observation.robotPos[1] + this.game.observation.robotHeight >= this.game.observation.chestPos[1] + this.game.observation.boxHeight) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> eastOfChest(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();

        if (this.game.observation.robotPos[0] + this.game.observation.robotWidth >= this.game.observation.chestPos[0] + this.game.observation.boxHeight) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> westOfChest(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();

        if (this.game.observation.robotPos[0] + this.game.observation.robotWidth <= this.game.observation.chestPos[0]) {
            list.add(new HashMap<>());
        }
        return list;
    }













    @Override
    public List<HashMap<Variable, Symbol>> northOflightButton(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();

        if (this.game.observation.robotPos[1] <= this.game.observation.lightButtonPos[1]) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> southOflightButton(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();

        if (this.game.observation.robotPos[1] >= this.game.observation.lightButtonPos[1] + this.game.observation.lightButtonHeight) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> eastOflightButton(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();

        if (this.game.observation.robotPos[0] >= this.game.observation.lightButtonPos[0] + this.game.observation.lightButtonWidth) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> westOflightButton(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();

        if (this.game.observation.robotPos[0] + this.game.observation.robotWidth <= this.game.observation.lightButtonPos[0]) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override 
    public List<HashMap<Variable, Symbol>> isAtlightButton(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        if (this.game.observation.isAtlightButton) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> isHoldingItem(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        if (this.game.observation.itemInAgentsHand != null) {
            list.add(new HashMap<>());
        }
        return list;
    }


    @Override 
    public List<HashMap<Variable, Symbol>> isAtChest(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        
        
        // Robot bounds
        int robotX = this.game.observation.robotPos[0];
        int robotY = this.game.observation.robotPos[1];
        int robotWidth = this.game.observation.robotWidth;
        int robotHeight = this.game.observation.robotHeight;
        int robotRight = robotX + robotWidth;
        int robotBottom = robotY + robotHeight;
        
        // Chest bounds
        int chestX = this.game.observation.chestPos[0];
        int chestY = this.game.observation.chestPos[1];
        int chestWidth = 100;  // From game code: Chest((100, 80))
        int chestHeight = 80;  // From game code: Chest((100, 80))
        int chestRight = chestX + chestWidth;
        int chestBottom = chestY + chestHeight;
        
        // Check for overlap (rectangles overlap if they don't NOT overlap)
        boolean overlapping = !(robotRight <= chestX ||    // Robot is completely to the left of chest
                            robotX >= chestRight ||     // Robot is completely to the right of chest
                            robotBottom <= chestY ||    // Robot is completely above chest
                            robotY >= chestBottom);     // Robot is completely below chest
        
        if (overlapping) {
            list.add(new HashMap<>());
        }
        
        return list;
    }

    @Override 
    public List<HashMap<Variable, Symbol>> isAt(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        
        // Extract the object name from the term
         // Extract the object name from the term

        String objectName = term.get(0).toString();

        
        
        
        // Robot bounds (common for all checks)
        int robotX = this.game.observation.robotPos[0];
        int robotY = this.game.observation.robotPos[1];
        int robotWidth = this.game.observation.robotWidth;
        int robotHeight = this.game.observation.robotHeight;
        int robotRight = robotX + robotWidth;
        int robotBottom = robotY + robotHeight;
        
        boolean overlapping = false;
        
        switch (objectName) {
            case "chest":
                
                // Chest bounds
                int chestX = this.game.observation.chestPos[0];
                int chestY = this.game.observation.chestPos[1];
                int chestWidth = 100;  // From game code
                int chestHeight = 80;  // From game code
                int chestRight = chestX + chestWidth;
                int chestBottom = chestY + chestHeight;
                System.out.println("DEBUG: Chest bounds: [" + chestX + "," + chestY + "] size: " + chestWidth + "x" + chestHeight);
                
                overlapping = !(robotRight <= chestX ||
                            robotX >= chestRight ||
                            robotBottom <= chestY ||
                            robotY >= chestBottom);
                System.out.println("DEBUG: Overlapping = " + overlapping);

                break;
                
            case "box":
                // Only check if box is not being held
                if (this.game.observation.boxPos != null) {
                    int boxX = this.game.observation.boxPos[0];
                    int boxY = this.game.observation.boxPos[1];
                    int boxWidth = this.game.observation.boxWidth;
                    int boxHeight = this.game.observation.boxHeight;
                    int boxRight = boxX + boxWidth;
                    int boxBottom = boxY + boxHeight;
                    
                    overlapping = !(robotRight <= boxX ||
                                robotX >= boxRight ||
                                robotBottom <= boxY ||
                                robotY >= boxBottom);
                }
                break;
                
            case "lightButton":
                // lightButton bounds (it's a circle, but we can treat as square)
                int lightButtonX = this.game.observation.lightButtonPos[0];
                int lightButtonY = this.game.observation.lightButtonPos[1];
                int lightButtonWidth = this.game.observation.lightButtonWidth;
                int lightButtonHeight = this.game.observation.lightButtonHeight;
                int lightButtonRight = lightButtonX + lightButtonWidth;
                int lightButtonBottom = lightButtonY + lightButtonHeight;
                
                overlapping = !(robotRight <= lightButtonX ||
                            robotX >= lightButtonRight ||
                            robotBottom <= lightButtonY ||
                            robotY >= lightButtonBottom);
                break;
                
            case "door":
                // Door bounds
                int doorX = 0;  // Door is at x=0
                int doorY = this.game.observation.doorTop;
                int doorWidth = this.game.observation.wallWidth;
                int doorHeight = this.game.observation.doorBottom - this.game.observation.doorTop;
                int doorRight = doorX + doorWidth;
                int doorBottom = doorY + doorHeight;
                
                overlapping = !(robotRight <= doorX ||
                            robotX >= doorRight ||
                            robotBottom <= doorY ||
                            robotY >= doorBottom);
                break;
                
            default:
                // Unknown object, return false
                overlapping = false;
                break;
        }
        
        if (overlapping) {
            list.add(new HashMap<>());
        }
        
        return list;
    }

    @Override 
    public List<HashMap<Variable, Symbol>> isOpen(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        String objectName = term.get(0).toString();

        if (objectName == "chest") {
            if (this.game.observation.chestOpen) {
                list.add(new HashMap<>());
            }
        }

        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> northOf(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        String objectName = term.get(0).toString();
        
        int robotY = this.game.observation.robotPos[1];
        int objectY = getObjectY(objectName);
        
        if (robotY < objectY) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> southOf(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        String objectName = term.get(0).toString();
        
        int robotY = this.game.observation.robotPos[1];
        int objectY = getObjectY(objectName);
        
        if (robotY > objectY) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> eastOf(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        String objectName = term.get(0).toString();
        
        int robotX = this.game.observation.robotPos[0];
        int objectX = getObjectX(objectName);
        
        if (robotX > objectX) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> westOf(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        String objectName = term.get(0).toString();
        
        int robotX = this.game.observation.robotPos[0];
        int objectX = getObjectX(objectName);
        
        if (robotX < objectX) {
            list.add(new HashMap<>());
        }
        return list;
    }

    // Helper methods
    private int getObjectX(String objectName) {
        switch (objectName) {
            case "chest":
                return this.game.observation.chestPos[0];
            case "box":
                return this.game.observation.boxPos != null ? this.game.observation.boxPos[0] : -1;
            case "lightButton":
                return this.game.observation.lightButtonPos[0];
            case "door":
                return 0; // Door is at x=0
            default:
                return -1;
        }
    }

    private int getObjectY(String objectName) {
        switch (objectName) {
            case "chest":
                return this.game.observation.chestPos[1];
            case "box":
                return this.game.observation.boxPos != null ? this.game.observation.boxPos[1] : -1;
            case "lightButton":
                return this.game.observation.lightButtonPos[1];
            case "door":
                return this.game.observation.doorTop;
            default:
                return -1;
        }
    }


    @Override 
    public List<HashMap<Variable, Symbol>> isAgentPushing(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        if (this.game.observation.isAgentPushing) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> isInPickupRange(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        if (this.game.observation.isInPickupRange) {
            list.add(new HashMap<>());
        }
        return list;
    } 

    @Override
    public List<HashMap<Variable, Symbol>> lightsOff(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        if (this.game.observation.lights == "off") {
            list.add(new HashMap<>());
        }
        return list;
    }
    
    @Override
    public List<HashMap<Variable, Symbol>> northOfDoorCenter(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        
        if (
            this.game.observation.robotPos[1] <= this.game.observation.doorTop +
                Math.floor((this.game.observation.doorBottom - this.game.observation.doorTop)/2)
        ) {
            list.add(new HashMap<>());
        }
        return list;
    }
    
    @Override
    public List<HashMap<Variable, Symbol>> southOfDoorCenter(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        
        if (
            this.game.observation.robotPos[1] >= this.game.observation.doorTop +
                Math.floor((this.game.observation.doorBottom - this.game.observation.doorTop)/2)
        ) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> eastOfDoor(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();

        if (this.game.observation.robotPos[0] > 0) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> isAtDoor(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();

        if (
            this.game.observation.robotPos[0] == this.game.observation.wallWidth &&
            this.game.observation.robotPos[1] <= this.game.observation.doorBottom &&
            this.game.observation.robotPos[1] >= this.game.observation.doorTop
        ) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> canMoveWest(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        if (
            this.game.observation.robotPos[0] > this.game.observation.wallWidth
        ) {
            list.add(new HashMap<>());
        }
        return list;
    }

    /*************************************
     * UTILITY FUNCTIONS
     *************************************/

    private boolean isApproximatelyAt(int robotPos, int targetPos) {
        if (robotPos < targetPos - POSITION_TOLERANCE) {
            return false;
        }

        if (robotPos > targetPos + POSITION_TOLERANCE) {
            return false;
        }

        return true;
    }

    /*************************************
     * ACTION IMPLEMENTATIONS
     *************************************/

    @Override
    public Justification getObservation() {
        GameAction action = new GetObservation();
        game.perform(action);
        log.info("The action {} \n" , action);
        return new ConditionJustification(action.getSuccess());
    }

    @TRADEService
    public String getObservationJson() {
        GameAction action = new GetObservation();
        game.perform(action);
        if (action instanceof Active) {
            return action.rawResponse;          // return raw JSON string
        } else {
            log.warn("Action is not an instance of Active, cannot get rawResponse.");
            return "{}";
        }
    }

    @Override
    public Justification moveWest() {
        GameAction action = new West();
        game.perform(action);
        return new ConditionJustification(action.getSuccess());
    }
    
    @Override
    public Justification moveEast() {
        GameAction action = new East();
        game.perform(action);
        return new ConditionJustification(action.getSuccess());
    }
    
    @Override
    public Justification moveNorth() {
        GameAction action = new North();
        game.perform(action);
        return new ConditionJustification(action.getSuccess());
    }
    
    @Override
    public Justification moveSouth() {
        GameAction action = new South();
        game.perform(action);
        return new ConditionJustification(action.getSuccess());
    }

    
    @Override
    public Justification toggleHold() {
        GameAction action = new ToggleHold();
        game.perform(action);
        return new ConditionJustification(action.getSuccess());
    }

    @Override
    public Justification open() {
        GameAction action = new Open();
        game.perform(action);
        return new ConditionJustification(action.getSuccess());

    }

    @Override 
    public Justification close() {
        GameAction action = new Close();
        game.perform(action);
        return new ConditionJustification(action.getSuccess());

    }

    @Override
    public Justification push() {
        GameAction action = new Push();
        game.perform(action);
        return new ConditionJustification(action.getSuccess());

    }

    @Override 
    public Justification lift() {
        GameAction action = new Lift();
        game.perform(action);
        return new ConditionJustification(action.getSuccess());

    }

    @Override 
    public Justification insert() {
        GameAction action = new Insert();
        game.perform(action);
        return new ConditionJustification(action.getSuccess());

    }

    @Override 
    public Justification rotate() {
        GameAction action = new Rotate();
        game.perform(action);
        return new ConditionJustification(action.getSuccess());

    }


}
