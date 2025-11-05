from enum import IntEnum


class PlayerAction(IntEnum):
    NORTH = 1,
    SOUTH = 2,
    EAST = 3,
    WEST = 4,
    TOGGLE_HOLD = 5,
    GET_OBSERVATION = 6,
    RESET = 7,
    OPEN = 8,
    CLOSE = 9,
    PUSH = 10,
    LIFT = 11, 
    INSERT = 12, 
    ROTATE = 13, 
    ENTER = 14, 
    MOVE = 15,
    EXTEND = 16,
    RETRACT = 17, 
    INSERTBETWEEN = 18, 
    GRASP = 19, 
    PUT = 20, 
    SWEEP = 21, 
    DRAG = 22, 
    POKE = 23, 
    SQUEEZE = 24, 
    GOUP = 25, 
    GODOWN = 26. 
    PUTUNDER = 27
    

  


PlayerActionTable = {
    "NORTH" : PlayerAction.NORTH,
    "SOUTH" : PlayerAction.SOUTH,
    "EAST" : PlayerAction.EAST,
    "WEST" : PlayerAction.WEST,
    "TOGGLE_HOLD": PlayerAction.TOGGLE_HOLD,
    "GET_OBSERVATION": PlayerAction.GET_OBSERVATION,
    "RESET": PlayerAction.RESET,
    "OPEN": PlayerAction.OPEN,
    "CLOSE": PlayerAction.CLOSE,
    "PUSH" : PlayerAction.PUSH, 
    "LIFT" : PlayerAction.LIFT,
    "INSERT" : PlayerAction.INSERT, 
    "ROTATE" : PlayerAction.ROTATE,
    "ENTER" : PlayerAction.ENTER,
    "MOVE" : PlayerAction.MOVE,
    "EXTEND" : PlayerAction.EXTEND, 
    "RETRACT" : PlayerAction.RETRACT, 
    "INSERTBETWEEN" : PlayerAction.INSERTBETWEEN,
    "GRASP" : PlayerAction.GRASP, 
    "PUT" : PlayerAction.PUT, 
    "SWEEP" : PlayerAction.SWEEP, 
    "POKE" : PlayerAction.POKE, 
    "DRAG" : PlayerAction.DRAG, 
    "SQUEEZE" : PlayerAction.SQUEEZE,
    "GOUP" : PlayerAction.GOUP, 
    "GODOWN" : PlayerAction.GODOWN,
    "PUTUNDER" : PlayerAction.PUTUNDER


  
}
