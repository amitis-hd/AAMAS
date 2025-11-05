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
import edu.tufts.hrilab.boxbot.actions.Enter;
import edu.tufts.hrilab.boxbot.actions.Open;
import edu.tufts.hrilab.boxbot.actions.Close;
import edu.tufts.hrilab.boxbot.actions.GetObservation;
import edu.tufts.hrilab.boxbot.actions.Active;
import edu.tufts.hrilab.boxbot.actions.Move;
import edu.tufts.hrilab.boxbot.actions.Extend;
import edu.tufts.hrilab.boxbot.actions.Retract;
import edu.tufts.hrilab.boxbot.actions.Insertbetween;
import edu.tufts.hrilab.boxbot.actions.Put;
import edu.tufts.hrilab.boxbot.actions.Grasp;
import edu.tufts.hrilab.boxbot.actions.SweepMotion;
import edu.tufts.hrilab.boxbot.actions.PierceMotion;
import edu.tufts.hrilab.boxbot.actions.DragMotion;
import edu.tufts.hrilab.boxbot.actions.SqueezeMotion;
import edu.tufts.hrilab.boxbot.actions.GoUpMotion;
import edu.tufts.hrilab.boxbot.actions.GoDownMotion;
import edu.tufts.hrilab.boxbot.actions.PutUnderMotion;



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
    public List<HashMap<Variable, Symbol>> isHoldingItem(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        if (this.game.observation.itemInAgentsHand != null) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> isHandExtended(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        if (this.game.observation.isHandExtended) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> isHandRotated(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        if (this.game.observation.handRotation > 0) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> isUnder(Term objectTerm, Term baseTerm) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        
        String objectName = objectTerm.getName();
        String baseName = baseTerm.getName();
        
        // Check if the underRelationships map exists and contains this relationship
        if (this.game.observation.underRelationships != null && 
            this.game.observation.underRelationships.containsKey(objectName)) {
            
            String actualBase = this.game.observation.underRelationships.get(objectName);
            
            // If the object is under the specified base, return success
            if (actualBase.equals(baseName)) {
                list.add(new HashMap<>());
            }
        }
        
        return list;
    }

   
    @Override
    public List<HashMap<Variable, Symbol>> atOpenning(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        String objectName = term.get(0).toString().strip();
        log.error("openning is : {}", objectName);
       

        if (objectName.contains("-door") || objectName.contains("-window")) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> locationChanged(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        String objectName = term.get(0).toString().strip();


        if (!objectName.equals(this.game.observation.location)) {
            list.add(new HashMap<>());
        }
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> isPierced(Term targetTerm) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        
        String targetName = targetTerm.getName();
        
        // Check if the target object has been pierced
        if (targetName.equals("lemon") && this.game.observation.lemonPierced) {
            list.add(new HashMap<>());
        }
        
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> isElevated(Term targetTerm) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
 
        
        // Check if the target object has been pierced
        if (this.game.observation.isElevated) {
            list.add(new HashMap<>());
        }
        
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> isMoved(Term objectTerm) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        
        // Check if an object has been moved
        if (this.game.observation.objectMoved) {
            list.add(new HashMap<>());
        }
        
        return list;
    }

    @Override
    public List<HashMap<Variable, Symbol>> isSqueezed(Term objectTerm) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        
        String objectName = objectTerm.getName();
        
        // Check if the object has been squeezed
        if (objectName.equals("lemon") && this.game.observation.lemonSqueezed) {
            list.add(new HashMap<>());
        }
        
        return list;
    }

    // Optional: Add observer for mug being full
    @Override
    public List<HashMap<Variable, Symbol>> isFull(Term objectTerm) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        
        String objectName = objectTerm.getName();
        
        if (objectName.equals("mug") && this.game.observation.mugFull) {
            list.add(new HashMap<>());
        }
        
        return list;
    }



    @Override 
    public List<HashMap<Variable, Symbol>> isAt(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        String objectName = term.get(0).toString().strip();
        log.error("objectname is : {}", objectName);
        log.error("location is: {}", this.game.observation.location);
        
        // Check if the location string contains the object name
        if (this.game.observation.location.contains("-" + objectName)) {
            //log.info("done");
            list.add(new HashMap<>());
        }
        
        return list;
    }

    @Override 
    public List<HashMap<Variable, Symbol>> isOpen(Term term) {
        List<HashMap<Variable, Symbol>> list = new ArrayList<>();
        String objectName = term.get(0).toString().strip();
        
        //log.info("=== Checking isOpen for: {} ===", objectName);
        //log.info("Observation chestOpen: {}", this.game.observation.chestOpen);
        //log.info("Observation doorOpen: {}", this.game.observation.doorOpen);
        
        boolean isObjectOpen = false;
        
        if (objectName.equals("chest")) {
            isObjectOpen = this.game.observation.chestOpen;
        }
        else if (objectName.equals("door")) {
            isObjectOpen = this.game.observation.doorOpen;
        }
        
        //log.info("{} is open: {}", objectName, isObjectOpen);
        
        if (isObjectOpen) {
            list.add(new HashMap<>());
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
                return this.game.observation.doorPos[0]; 
            case "window":
                return this.game.observation.windowPos[0];
            case "stool":
                return this.game.observation.stoolPos[0];
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
                return this.game.observation.doorPos[1];
            case "window":
                return this.game.observation.windowPos[1];
            case "stool":
                return this.game.observation.stoolPos[1];
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
        String objectName = this.game.observation.lights;
        if (objectName.equals("off")) {
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
        //log.info("The action {} \n" , action);
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
    public Justification toggleHold(Term term) {
        log.info("inside togglehold");
        String objectName = term.getName();
        GameAction action = new ToggleHold(objectName.strip());
        log.info("about to perform it");
        game.perform(action);
        return new ConditionJustification(action.getSuccess());
    }
    
    @Override
    public Justification move(Term term) {
        String objectName = term.getName();
        //log.info("Moving to object: {}", objectName);
        GameAction action = new Move(objectName.strip());
        game.perform(action);
        return new ConditionJustification(action.getSuccess());
    }

    @Override
    public Justification open(Term term) {
        String objectName = term.getName();
        GameAction action = new Open(objectName.strip());
        //log.info("performing open to object: {}", objectName);
        game.perform(action);
        //log.info("!!! game.perform() completed, success: {}", action.getSuccess());
        return new ConditionJustification(action.getSuccess());

    }

    @Override 
    public Justification close(Term term) {
        String objectName = term.getName();
        GameAction action = new Close(objectName.strip());
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
    public Justification extend() {
        GameAction action = new Extend();
        game.perform(action);
        return new ConditionJustification(action.getSuccess());

    }

    @Override 
    public Justification retract() {
        GameAction action = new Retract();
        game.perform(action);
        return new ConditionJustification(action.getSuccess());

    }


    @Override 
    public Justification insert(Term objectTerm, Term destinationTerm) {
        String objectName = objectTerm.getName();
        String destName = destinationTerm.getName();
        log.info("inserting {} into {}", objectName, destName);
        GameAction action = new Insert(objectName.strip(), destName.strip());
        game.perform(action);
        return new ConditionJustification(action.getSuccess());
    }

    @Override 
    public Justification put(Term objectTerm, Term destinationTerm) {
        String objectName = objectTerm.getName();
        String destName = destinationTerm.getName();
        log.info("puttin {} on {}", objectName, destName);
        GameAction action = new Put(objectName.strip(), destName.strip());
        game.perform(action);
        return new ConditionJustification(action.getSuccess());
    }

    @Override
    public Justification putUnderMotion(Term objectTerm, Term destinationTerm) {
        String objectName = objectTerm.getName();
        String destName = destinationTerm.getName();
        log.info("putting {} under {}", objectName, destName);
        GameAction action = new PutUnderMotion(objectName.strip(), destName.strip());
        game.perform(action);
        return new ConditionJustification(action.getSuccess());
    }

    @Override 
    public Justification insertbetween(Term objectTerm, Term left_objTerm, Term right_objTerm) {
        String objectName = objectTerm.getName();
        String left_obj = left_objTerm.getName();
        String right_obj = right_objTerm.getName();
        log.info("inserting {} into {} and {}", objectName, left_obj, right_obj);
        GameAction action = new Insertbetween(objectName.strip(), left_obj.strip(), right_obj.strip());
        game.perform(action);
        return new ConditionJustification(action.getSuccess());
    }


    @Override 
    public Justification rotate(Term term) {
        String degree = term.getName();
        //log.info("rotating by: {}", degree);
        GameAction action = new Rotate(Integer.parseInt(degree.strip()));
        game.perform(action);
        return new ConditionJustification(action.getSuccess());

    }

    @Override 
    public Justification grasp(Term term) {
        String objectName = term.getName();
        //log.info("rotating by: {}", degree);
        GameAction action = new Grasp(objectName.strip());
        game.perform(action);
        return new ConditionJustification(action.getSuccess());

    }

    @Override
    public Justification sweepMotion(Term toolTerm, Term targetTerm, Term surfaceTerm) {
        String toolName = toolTerm.getName();
        String targetName = targetTerm.getName();
        String surfaceName = surfaceTerm.getName();
        log.info("sweeping {} on {} to move {}", toolName, surfaceName, targetName);
        GameAction action = new SweepMotion(toolName.strip(), targetName.strip(), surfaceName.strip());
        game.perform(action);
        return new ConditionJustification(action.getSuccess());
    }

    @Override
    public Justification dragMotion(Term objectTerm, Term destinationTerm) {
        String objectName = objectTerm.getName();
        String destName = destinationTerm.getName();
        log.info("dragging {} to {}", objectName, destName);
        GameAction action = new DragMotion(objectName.strip(), destName.strip());
        game.perform(action);
        return new ConditionJustification(action.getSuccess());
    }

    @Override
    public Justification pierceMotion(Term toolTerm, Term targetTerm) {
        String toolName = toolTerm.getName();
        String targetName = targetTerm.getName();
        log.info("poking {} with {}", targetName, toolName);
        GameAction action = new PierceMotion(toolName.strip(), targetName.strip());
        game.perform(action);
        return new ConditionJustification(action.getSuccess());
    }

    @Override
    public Justification squeezeMotion(Term objectTerm) {
        String objectName = objectTerm.getName();
        log.info("squeezing {}", objectName);
        GameAction action = new SqueezeMotion(objectName.strip());
        game.perform(action);
        return new ConditionJustification(action.getSuccess());
    }

    @Override
    public Justification goUpMotion(Term objectTerm) {
        String objectName = objectTerm.getName();
        log.info("climbing up onto {}", objectName);
        GameAction action = new GoUpMotion(objectName.strip());
        game.perform(action);
        return new ConditionJustification(action.getSuccess());
    }

    @Override
    public Justification goDownMotion() {
        log.info("climbing down");
        GameAction action = new GoDownMotion();
        game.perform(action);
        return new ConditionJustification(action.getSuccess());
    }


}
