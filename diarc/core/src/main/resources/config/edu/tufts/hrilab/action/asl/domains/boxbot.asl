import edu.tufts.hrilab.fol.Symbol;
import edu.tufts.hrilab.fol.Predicate;
import edu.tufts.hrilab.fol.Variable;

() = pickup["?actor lowers its arms, grabs the object ?object, then raises its arms (?object must be chosen from a value of type object in the pl file)"](Symbol ?object:physobj) {
    conditions: {
       
    }

    effects: {
        success: isHoldingItem();

    }

    act: toggleHold(?object);
}

() = putdown["?actor lowers its arms, releases the object ?object, raises its arms (?object must be chosen from a value of type object in the pl file) - use for putting things down then it doesnt matter where or just on the floor"](Symbol ?object:physobj) {
    conditions: {
        pre: isHoldingItem();
        pre : isAt(?object);
  
    }
    effects: {
        success: ~isHoldingItem();
   
    }

    act: toggleHold(?object);
}

() = putOn["?actor lowers its arms, releases the object ?object in its hands directly on top of the object ?destination, then raises arms - use for putting things down when it metters where you put them"](Symbol ?object:physobj, Symbol ?destination:physobj) {
    conditions: {
        pre: isHoldingItem();
        pre: isAt(?object);
        pre: isAt(?destination);
    }

    effects: {
        success: ~isHoldingItem();
        success: isOn(?object, ?destination);
    }

    act: put(?object, ?destination);
}


() = pushDown["?actor lowers its arm and holds the object in its reach down. This action must be followed by liftarm before the agent can move"]() {
    conditions: {
        pre: ~isAgentPushing();
        pre: ~isHoldingItem();

    }
    effects: {
        success: isAgentPushing();

    }

    act: push;
}

() = liftarm["?actor raises its arm up"]() {
    conditions: {
        pre: isAgentPushing();

    }
    effects: {
        success: ~isAgentPushing();

    }

    act: lift;
}

() = openObject["?actor opens ?object (?object must be chosen from a value of type object in the pl file)"](Symbol ?object:physobj) {
    conditions: {
        
        pre: ~isAgentPushing();
        pre : isAt(?object);
        pre: ~isOpen(?object);
    }

    effects: {
        success: isOpen(?object);
    }

    act:open(?object);
}

() = closeObject["?actor closes ?object (?object must be chosen from a value of type object in the pl file)"](Symbol ?object:physobj) {
    conditions: {
        
        pre: ~isAgentPushing();
        pre : isAt(?object);
        pre : isOpen(?object);
    }

    effects: {
        success: ~isOpen(?object);
    }

    act: close(?object);
}


() = rotateHand["?actor rotates its wrist 90 degrees clockwise"](Symbol ?degree:degree) {
    conditions: {
        
    }

    effects: {
        success: isHandRotated();
    }

    act:rotate(?degree);
}

() = extendHand["?actor extends its hand forward from a nutral position - this action does not insert. it only extends the hand"]() {

    conditions: {
        
    }

    effects: {
        success: isHandExtended();
    }

    act:extend;

}

() = retractHand["?actor retracts its extended hand back into a nutral position"]() {
    conditions: {
        
    }

    effects: {
        success: ~isHandExtended();
    }

    act:retract;
}

() = putIn["?actor puts the ?object in its hand into the hole shaped cavity ?destination"](Symbol ?object:physobj, Symbol ?destination:physobj) {

    conditions: {
        pre: isHoldingItem();
        
    }

    effects: {
        
    }

    act:insert(?object, ?destination);
}

() = putBetween["?actor puts the ?object in its hands in the cavity between objects ?leftObj and ?rightObj - this action does not make space, it only inserts into a pre existing cavity"](Symbol ?object:physobj, Symbol ?leftObj:physobj, Symbol ?rightObj:physobj) {

    conditions: {
        pre: isHoldingItem();
        pre: isAt(?leftObj);
        pre: isAt(?rightObj);
    }

    effects: {

    }

    act: insertbetween(?object, ?leftObj, ?rightObj);

}


() = grab["?actor closes its fist, grabbing the ?object in its hands"](Symbol ?object:physobj) {
    conditions: {
        pre: isAt(?object);
    }

    effects: {
        success: isHoldingItem();
    }

    act: grasp(?object);
}

() = goThrough["?actor goes through an opening (MUST be marked as opening in the pl file) and changes its ?location (?location field passed in MUST be the AGENT's CURRENT LOCATION )"](Symbol ?location:location) {


    conditions: {
        pre: atOpenning(?location);
    }

    effects: {
        success: locationChanged(?location);
    }

    act: enter;
}




() = moveTo["?actor moves to the specified ?object"](Symbol ?object:physobj) {
    conditions : {
        pre : ~isAgentPushing();
    }
    effects : {
        success: isAt(?object);
    }

    op: log(info, "moving to object");
    
    act:move(?object);
}

() = sweep["?actor moves ?tool in sweeping motion across ?surface to push ?target"](
    Symbol ?tool:physobj, 
    Symbol ?target:physobj, 
    Symbol ?surface:physobj
) {
    conditions: {
        pre: isHoldingItem();
        pre: isAt(?target);
    }
    effects: {
        success: isMoved(?target);
    }
    act: sweepMotion(?tool, ?target, ?surface);
    
}

() = poke["?actor uses pointed ?tool to pierce or puncture ?target and then takes ?tool out of ?target"](
    Symbol ?tool:physobj, 
    Symbol ?target:physobj
) {
    conditions: {
        pre: isHoldingItem();
        pre: isAt(?target);
    }
    effects: {
        success: isPierced(?target);
    }
    act: pierceMotion(?tool, ?target);
    
}

() = drag["?actor drags ?object along surface to ?destination"](
    Symbol ?object:physobj, 
    Symbol ?direction:direction
) {
    conditions: {
        pre: isAt(?object);
        pre: ~isAgentPushing();
    }
    effects: {
        success: isMoved(?object);
        success: isAt(?destination);
    }
    act: dragMotion(?object, ?destination);
}

() = squeeze["?actor squeezes the ?object in its hand"](Symbol ?object:physobj) {
    conditions: {
        pre: isHoldingItem();
        pre: isAt(?object);
    }

    effects: {
        success: isSqueezed(?object);
    }

    act: squeezeMotion(?object);
}

() = goUp["?actor climbs up onto ?object (must be a climbable object)"](Symbol ?object:physobj) {
    conditions: {
        pre: ~isElevated();
        pre: isAt(?object);
    }
    
    effects: {
        success: isElevated();
    }
    
    act: goUpMotion(?object);
}

() = goDown["?actor climbs down from elevated position"]() {
    conditions: {
        pre: isElevated();
    }
    
    effects: {
        success: ~isElevated();
    }
    
    act: goDownMotion();
}

() = putUnder["?actor places ?object underneath ?destination"](Symbol ?object:physobj, Symbol ?destination:physobj) {
    conditions: {
        pre: isHoldingItem();
        pre: isAt(?object);
        pre: isAt(?destination);
    }

    effects: {
        success: ~isHoldingItem();
        success: isUnder(?object, ?destination);
    }

    act: putUnderMotion(?object, ?destination);
}