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

    @TRADEService 
    @Observes({ "isHoldingItem()" })
    public List<HashMap<Variable, Symbol>> isHoldingItem(Term term);

    @TRADEService 
    @Observes({ "isHandExtended()" })
    public List<HashMap<Variable, Symbol>> isHandExtended(Term term);

    @TRADEService 
    @Observes({ "isHandRotated()" })
    public List<HashMap<Variable, Symbol>> isHandRotated(Term term);
    @TRADEService 
    @Observes({ "atOpenning()" })
    public List<HashMap<Variable, Symbol>> atOpenning(Term term);
    @TRADEService 
    @Observes({ "locationChanged()" })
    public List<HashMap<Variable, Symbol>> locationChanged(Term term);

    @TRADEService 
    @Observes({ "isPierced()" })
    List<HashMap<Variable, Symbol>> isPierced(Term targetTerm);
    @TRADEService 
    @Observes({ "isMoved()" })
    List<HashMap<Variable, Symbol>> isMoved(Term objectTerm);
    @TRADEService 
    @Observes({ "isSqueezed()" })
    List<HashMap<Variable, Symbol>> isSqueezed(Term objectTerm);
    @TRADEService 
    @Observes({ "isFull()" })
    public List<HashMap<Variable, Symbol>> isFull(Term objectTerm);
    // @TRADEService 
    // @Observes({ "getlocation()" })
    // public List<HashMap<Variable, Symbol>> getlocation(Term term);

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

    @TRADEService
    @Observes({ "isElevated()" })
    public List<HashMap<Variable, Symbol>> isElevated(Term targetTerm);

    @TRADEService
    @Observes({ "isUnder(?object, ?destination)" })
    public List<HashMap<Variable, Symbol>> isUnder(Term objectTerm, Term baseTerm);

    
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
    Justification toggleHold(Term term);

    /**
     * Action to request the robot to open an object.
     * 
     * @return justification describing the result of the request.
     */
    @TRADEService
    @Action
    Justification open(Term term);

    @TRADEService
    @Action
    Justification move(Term term);

    /**
     * Action to request the robot to close an object.
     * 
     * @return justification describing the result of the request.
     */
    @TRADEService
    @Action
    Justification close(Term term);

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

    @TRADEService
    @Action
    Justification extend();

    @TRADEService
    @Action
    Justification retract();

    /**
     * Action to request the robot to Lift a hand.
     * 
     * @return justification describing the result of the request.
     */
    @TRADEService
    @Action
    Justification insert(Term objectTerm, Term destinationTerm);

    @TRADEService
    @Action
    Justification insertbetween(Term objectTerm, Term left_objTerm, Term right_objTerm);

    /**
     * Action to request the robot to Lift a hand.
     * 
     * @return justification describing the result of the request.
     */
    @TRADEService
    @Action
    Justification rotate(Term term);

    @TRADEService
    @Action
    Justification put(Term objectTerm, Term destinationTerm);

    @TRADEService
    @Action
    Justification grasp(Term term);

    @TRADEService
    @Action
    Justification sweepMotion(Term toolTerm, Term targetTerm, Term surfaceTerm);

    @TRADEService
    @Action
    Justification dragMotion(Term objectTerm, Term destinationTerm);

    @TRADEService
    @Action
    public Justification pierceMotion(Term toolTerm, Term targetTerm);

    @TRADEService
    @Action
    public Justification squeezeMotion(Term objectTerm);

    @TRADEService
    @Action
    public Justification goDownMotion();

    @TRADEService
    @Action
    public Justification goUpMotion(Term objectTerm);

    @TRADEService
    @Action
    Justification putUnderMotion(Term objectTerm, Term destinationTerm);

    
}
