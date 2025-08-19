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
    ROTATE = 13
  


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
  
}
