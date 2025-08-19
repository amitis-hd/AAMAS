package edu.tufts.hrilab.interfaces;

import ai.thinkingrobots.trade.TRADEService;

import edu.tufts.hrilab.action.annotations.Action;
import edu.tufts.hrilab.action.annotations.Observes;
import edu.tufts.hrilab.action.justification.Justification;

import edu.tufts.hrilab.fol.Predicate;
import edu.tufts.hrilab.fol.Variable;
import edu.tufts.hrilab.fol.Term;
import edu.tufts.hrilab.fol.Factory;
import edu.tufts.hrilab.fol.Symbol;

import java.util.HashMap;
import java.util.List;

public interface BoxBotSimulatorInterface {
    /*************************************
     * OBSERVERS
     *************************************/

    /**
     * Checks whether the robot is currently north of the box.
     * 
     * @param term observer term (unused)
     * @return Empty list if not north of the box, non-empty list if it is.
     */
    @TRADEService
    @Observes({ "northOf(?object)" })
    public List<HashMap<Variable, Symbol>> northOf(Term objectTerm);

    @TRADEService
    @Observes({ "southOf(?object)" })
    public List<HashMap<Variable, Symbol>> southOf(Term objectTerm);

    @TRADEService
    @Observes({ "eastOf(?object)" })
    public List<HashMap<Variable, Symbol>> eastOf(Term objectTerm);

    @TRADEService
    @Observes({ "westOf(?object)" })
    public List<HashMap<Variable, Symbol>> westOf(Term objectTerm);

      /**
     * Checks whether the robot is currently north of the box.
     * 
     * @param term observer term (unused)
     * @return Empty list if not north of the box, non-empty list if it is.
     */
    @TRADEService
    @Observes({ "northOfBox()" })
    public List<HashMap<Variable, Symbol>> northOfBox(Term term);

    /**
     * Checks whether the robot is currently south of the box.
     * 
     * @param term observer term (unused)
     * @return Empty list if not south of the box, non-empty list if it is.
     */
    @TRADEService
    @Observes({ "southOfBox()" })
    public List<HashMap<Variable, Symbol>> southOfBox(Term term);

    /**
     * Checks whether the robot is currently east of the box.
     * 
     * @param term observer term (unused)
     * @return Empty list if not east of the box, non-empty list if it is.
     */
    @TRADEService
    @Observes({ "eastOfBox()" })
    public List<HashMap<Variable, Symbol>> eastOfBox(Term term);

    /**
     * Checks whether the robot is currently west of the box.
     * 
     * @param term observer term (unused)
     * @return Empty list if not west of the box, non-empty list if it is.
     */
    @TRADEService
    @Observes({ "westOfBox()" })
    public List<HashMap<Variable, Symbol>> westOfBox(Term term);


    /**
     * Checks whether the robot is currently north of the Chest.
     * 
     * @param term observer term (unused)
     * @return Empty list if not north of the box, non-empty list if it is.
     */
    @TRADEService
    @Observes({ "northOfChest()" })
    public List<HashMap<Variable, Symbol>> northOfChest(Term term);

    /**
     * Checks whether the robot is currently south of the chest.
     * 
     * @param term observer term (unused)
     * @return Empty list if not south of the box, non-empty list if it is.
     */
    @TRADEService
    @Observes({ "southOfChest()" })
    public List<HashMap<Variable, Symbol>> southOfChest(Term term);

    /**
     * Checks whether the robot is currently east of the chest.
     * 
     * @param term observer term (unused)
     * @return Empty list if not east of the box, non-empty list if it is.
     */
    @TRADEService
    @Observes({ "eastOfChest()" })
    public List<HashMap<Variable, Symbol>> eastOfChest(Term term);

    /**
     * Checks whether the robot is currently west of the chest.
     * 
     * @param term observer term (unused)
     * @return Empty list if not west of the box, non-empty list if it is.
     */
    @TRADEService
    @Observes({ "westOfChest()" })
    public List<HashMap<Variable, Symbol>> westOfChest(Term term);

    /**
     * Checks whether the robot is currently north of the switch.
     * 
     * @param term observer term (unused)
     * @return Empty list if not north of the switch, non-empty list if it is.
     */
    @TRADEService
    @Observes({ "northOflightButton()" })
    public List<HashMap<Variable, Symbol>> northOflightButton(Term term);

    /**
     * Checks whether the robot is currently south of the switch.
     * 
     * @param term observer term (unused)
     * @return Empty list if not south of the switch, non-empty list if it is.
     */
    @TRADEService
    @Observes({ "southOflightButton()" })
    public List<HashMap<Variable, Symbol>> southOflightButton(Term term);

    /**
     * Checks whether the robot is currently east of the switch.
     * 
     * @param term observer term (unused)
     * @return Empty list if not east of the switch, non-empty list if it is.
     */
    @TRADEService
    @Observes({ "eastOflightButton()" })
    public List<HashMap<Variable, Symbol>> eastOflightButton(Term term);

    /**
     * Checks whether the robot is currently west of the switch.
     * 
     * @param term observer term (unused)
     * @return Empty list if not south of the west, non-empty list if it is.
     */
    @TRADEService
    @Observes({ "westOflightButton()" })
    public List<HashMap<Variable, Symbol>> westOflightButton(Term term);

    /**
     * Checks whether the robot is currently at the switch.
     * 
     * @param term observer term (unused)
     * @return Empty list if not at the switch, non-empty list if it is.
     */
    @TRADEService 
    @Observes({ "isAtlightButton()" })
    public List<HashMap<Variable, Symbol>> isAtlightButton(Term term);

    @TRADEService 
    @Observes({ "isHoldingItem()" })
    public List<HashMap<Variable, Symbol>> isHoldingItem(Term term);

    /**
     * Checks whether the robot is currently at the switch.
     * 
     * @param term observer term (unused)
     * @return Empty list if not at the switch, non-empty list if it is.
     */
    @TRADEService 
    @Observes({ "isAtChest()" })
    public List<HashMap<Variable, Symbol>> isAtChest(Term term);

        /**
     * Checks whether the robot is currently at the switch.
     * 
     * @param term observer term (unused)
     * @return Empty list if not at the switch, non-empty list if it is.
     */
    @TRADEService
    @Observes({ "isAt(?object)" })
    public List<HashMap<Variable, Symbol>> isAt(Term term);

    @TRADEService
    @Observes({"isOpen(?object)"})
    public List<HashMap<Variable, Symbol>> isOpen(Term term);

    @TRADEService 
    @Observes({ "isAgentPushing()" })
    public List<HashMap<Variable, Symbol>> isAgentPushing(Term term);

    /**
     * Checks whether the robot is currently in range of where it can pick up
     * the box.
     * 
     * @param term observer term (unused)
     * @return Empty list if can't pick up the box, non-empty list if it can.
     */
    @TRADEService
    @Observes({ "isInPickupRange()" })
    public List<HashMap<Variable, Symbol>> isInPickupRange(Term term); 


    /**
     * Checks whether the light is off
     * 
     * @param term observer term (unused)
     * @return Empty list if can't pick up the box, non-empty list if it can.
     */
    @TRADEService
    @Observes({ "lightsOff()" })
    public List<HashMap<Variable, Symbol>> lightsOff(Term term);

    /**
     * Checks whether the robot is currently north of the center of the door.
     * 
     * @param term observer term (unused)
     * @return Empty list if not north of the center of the door, non-empty
     * list if it is.
     */
    @TRADEService
    @Observes({ "northOfDoorCenter()" })
    public List<HashMap<Variable, Symbol>> northOfDoorCenter(Term term);

    /**
     * Checks whether the robot is currently south of the center of the door.
     * 
     * @param term observer term (unused)
     * @return Empty list if not south of the center of the door, non-empty
     * list if it is.
     */
    @TRADEService
    @Observes({ "southOfDoorCenter()" })
    public List<HashMap<Variable, Symbol>> southOfDoorCenter(Term term);

   /**
     * Checks whether the robot is currently east of the door.
     * 
     * @param term observer term (unused)
     * @return Empty list if not east of the door, non-empty list if it is.
     */
    @TRADEService
    @Observes({ "eastOfDoor()" })
    public List<HashMap<Variable, Symbol>> eastOfDoor(Term term);

    /**
     * Checks whether the robot is currently at the door.
     * 
     * @param term observer term (unused)
     * @return Empty list if not at the door, non-empty list if it is.
     */
    @TRADEService
    @Observes({ "isAtDoor()" })
    public List<HashMap<Variable, Symbol>> isAtDoor(Term term);

    /**
     * Checks whether the robot is capable of moving west.
     * 
     * @param term observer term (unused)
     * @return Empty list if robot can't move west, non-empty list if it can.
     */
    @TRADEService
    @Observes({ "canMoveWest()" })
    public List<HashMap<Variable, Symbol>> canMoveWest(Term term);

    /*************************************
     * ACTIONS
     *************************************/

    /**
     * Action to request the current state of the simulation.
     * 
     * @return justification describing the result of the request.
     */
    @TRADEService
    @Action
    Justification getObservation();
    
    /**
     * Action to request to move the robot west.
     * 
     * @return justification describing the result of the request.
     */
    @TRADEService
    @Action
    Justification moveWest();
    
    /**
     * Action to request to move the robot east.
     * 
     * @return justification describing the result of the request.
     */
    @TRADEService
    @Action
    Justification moveEast();
    
    /**
     * Action to request to move the robot north.
     * 
     * @return justification describing the result of the request.
     */
    @TRADEService
    @Action
    Justification moveNorth();
    
    /**
     * Action to request to move the robot south.
     * 
     * @return justification describing the result of the request.
     */
    @TRADEService
    @Action
    Justification moveSouth();
    
    /**
     * Action to request the robot to release or pick up an object.
     * 
     * @return justification describing the result of the request.
     */
    @TRADEService
    @Action
    Justification toggleHold();

    /**
     * Action to request the robot to open an object.
     * 
     * @return justification describing the result of the request.
     */
    @TRADEService
    @Action
    Justification open();

    /**
     * Action to request the robot to close an object.
     * 
     * @return justification describing the result of the request.
     */
    @TRADEService
    @Action
    Justification close();

        /**
     * Action to request the robot to Push a hand.
     * 
     * @return justification describing the result of the request.
     */
    @TRADEService
    @Action
    Justification push();

    /**
     * Action to request the robot to Lift a hand.
     * 
     * @return justification describing the result of the request.
     */
    @TRADEService
    @Action
    Justification lift();

    /**
     * Action to request the robot to Lift a hand.
     * 
     * @return justification describing the result of the request.
     */
    @TRADEService
    @Action
    Justification insert();

    /**
     * Action to request the robot to Lift a hand.
     * 
     * @return justification describing the result of the request.
     */
    @TRADEService
    @Action
    Justification rotate();

    
}
