import edu.tufts.hrilab.fol.Symbol;
import edu.tufts.hrilab.fol.Predicate;
import edu.tufts.hrilab.fol.Variable;

() = pickup["?actor lowers its arms, grabs the item, raises its arms"]() {
    conditions: {

        pre: ~isHoldingItem();
    }
    effects: {
        success: isHoldingItem();

    }

    act: toggleHold;
}

() = putdown["?actor lowers its arms, releases the box, raises its arms"]() {
    conditions: {
        pre: isHoldingItem();
  
    }
    effects: {
        success: ~isHoldingItem();
   
    }

    act: toggleHold;
}

() = pusharm["?actor lowers its arm and holds the object in its reach down. This action must be followed by liftarm before the agent can move"]() {
    conditions: {
        pre: ~isAgentPushing();

    }
    effects: {
        success: isAgentPushing();

    }

    act: push;
}

() = liftarm["?actor raises its arm back up, allowing it to move"]() {
    conditions: {
        pre: isAgentPushing();

    }
    effects: {
        success: ~isAgentPushing();

    }

    act: lift;
}

() = open["?actor opens ?object"](Symbol ?object:physobj) {
    conditions: {
        pre: equals(?object, chest);
        pre: ~isholdingItem();
        pre: ~isAgentPushing();
        pre : isAt(?object);
        pre: ~isOpen(?object);
    }

    effects: {
        success: isOpen(?object);
    }

    act: open;
}

() = close["?actor closes ?object"](Symbol ?object:physobj) {
    conditions: {
        pre: equals(?object, chest);
        pre: ~ isholdingItem();
        pre: ~isAgentPushing();
        pre : isAt(?object);
        pre infer: isOpen(?object);
    }

    effects: {
        success: ~isOpen(?object);
    }

    act: close;
}

() = insertandrotate["?actor inserts the object in its hand into a hole in front of it and rotates it"]() {
    conditions: {
        pre: isholdingItem();
        pre: ~isAgentPushing();
    }

    act: insert;
    act: rotate;
}




() = moveTo["?actor moves to the specified object"](Symbol ?object:physobj) {
    edu.tufts.hrilab.fol.Predicate !query;
    edu.tufts.hrilab.fol.Predicate !northQuery;
    edu.tufts.hrilab.fol.Predicate !southQuery;
    edu.tufts.hrilab.fol.Predicate !westQuery;
    edu.tufts.hrilab.fol.Predicate !eastQuery;

    conditions : {
        pre : ~isAgentPushing();
    }
    effects : {
        success: isAt(?object);
    }

    op: log(info, "moving to object");

    !query = op:invokeStaticMethod("edu.tufts.hrilab.fol.Factory", "createPredicate", "isAt(?object)");
    !northQuery = op:invokeStaticMethod("edu.tufts.hrilab.fol.Factory", "createPredicate", "northOf(?object)");
    !southQuery = op:invokeStaticMethod("edu.tufts.hrilab.fol.Factory", "createPredicate", "southOf(?object)");
    !westQuery = op:invokeStaticMethod("edu.tufts.hrilab.fol.Factory", "createPredicate", "westOf(?object)");
    !eastQuery = op:invokeStaticMethod("edu.tufts.hrilab.fol.Factory", "createPredicate", "eastOf(?object)");
    
    while(~obs:!query) {
        if(obs:!westQuery) {
            act:moveEast();
        }
        elseif(obs:!northQuery) {
            act:moveSouth();
        }
        elseif(obs:!eastQuery) {
            act:moveWest();
        }
        elseif(obs:!southQuery) {
            act:moveNorth();
        }
    }
}

