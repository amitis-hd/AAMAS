from typing import Tuple, Union, TypedDict, Optional, List, Dict
from shapes import calculate_overlap_percentage
from direction import Direction
import math
import pygame
import os

DIRECTION_VECTOR = {
    Direction.NORTH: (0, -1),
    Direction.SOUTH: (0, 1),
    Direction.EAST: (1, 0),
    Direction.WEST: (-1, 0)
}

# --- COLORS and TYPES ---
RED = (255, 0, 0)
BLACK = (0, 0, 0)
WHITE = (255, 255, 255)
GRAY = (128, 128, 128)
GREEN = (0, 255, 0)
LIGHT_BLUE = (173, 216, 230)

Position = Tuple[int, int]

class Observation(TypedDict, total=False):
    """
    Observation representing what the agent can currently perceive.
    Fields are added dynamically based on visibility.
    """
    # Agent state (always visible)
    itemInAgentsHand: Union[str, None]
    isHandExtended: bool
    handRotation: int
    isAgentPushing: bool
    
    # Environment state (always visible in current room)
    lights: str
    location: str
    
    # Robot info (always visible)
    robotPos: Position
    robotWidth: int
    robotHeight: int
    
    # Objects visible in Room1
    plantsPos: Optional[Position]
    plantsWidth: Optional[int]
    plantsHeight: Optional[int]
    
    bottlePos: Optional[Position]
    bottleWidth: Optional[int]
    bottleHeight: Optional[int]
    bottle: Optional[str]
    bottleFull: Optional[bool]
    bottleLiquidColor: Optional[str]
    
    faucetPos: Optional[Position]
    faucetWidth: Optional[int]
    faucetHeight: Optional[int]
    
    lightButtonPos: Optional[Position]
    lightButtonWidth: Optional[int]
    lightButtonHeight: Optional[int]
    islightButtonPressed: Optional[bool]
    
    chestPos: Optional[Position]
    chestWidth: Optional[int]
    chestHeight: Optional[int]
    chest: Optional[str]
    chestOpen: Optional[bool]
    
    boxPos: Optional[Position]
    boxWidth: Optional[int]
    boxHeight: Optional[int]
    
    # Door (visible from both rooms when near)
    doorPos: Optional[Position]
    doorWidth: Optional[int]
    doorHeight: Optional[int]
    door: Optional[str]
    doorOpen: Optional[bool]
    wallWidth: Optional[int]
    
    # Key (only visible when chest is open)
    keyPos: Optional[Position]
    keyWidth: Optional[int]
    keyHeight: Optional[int]
    keyVisible: Optional[bool]
    
    # Objects visible in Room2 (only when door is open)
    windowPos: Optional[Position]
    windowWidth: Optional[int]
    windowHeight: Optional[int]
    
    stoolPos: Optional[Position]
    stoolWidth: Optional[int]
    stoolHeight: Optional[int]

     # Bookshelf scenario objects
    bookcasePos: Optional[Position]
    bookcaseWidth: Optional[int]
    bookcaseHeight: Optional[int]
    
    book1Pos: Optional[Position]
    book1Width: Optional[int]
    book1Height: Optional[int]
    
    book2Pos: Optional[Position]
    book2Width: Optional[int]
    book2Height: Optional[int]
    
    broomPos: Optional[Position]
    broomWidth: Optional[int]
    broomHeight: Optional[int]
    
    mousepadPos: Optional[Position]
    mousepadWidth: Optional[int]
    mousepadHeight: Optional[int]
    
    penPos: Optional[Position]
    penWidth: Optional[int]
    penHeight: Optional[int]
    
    rollingpinPos: Optional[Position]
    rollingpinWidth: Optional[int]
    rollingpinHeight: Optional[int]
    
    spatulaPos: Optional[Position]
    spatulaWidth: Optional[int]
    spatulaHeight: Optional[int]
    spatulaRotation: Optional[int]
    spatulaInserted: Optional[bool]
    
    notePos: Optional[Position]
    noteWidth: Optional[int]
    noteHeight: Optional[int]
    noteVisible: Optional[bool]

    blanketPos: Optional[Position]
    blanketWidth: Optional[int]
    blanketHeight: Optional[int]
    blanketRotation: Optional[int]
    blanketInserted: Optional[bool]
    
    forkPos: Optional[Position]
    forkWidth: Optional[int]
    forkHeight: Optional[int]
    
    suctioncupPos: Optional[Position]
    suctioncupWidth: Optional[int]
    suctioncupHeight: Optional[int]
    
    magnetPos: Optional[Position]
    magnetWidth: Optional[int]
    magnetHeight: Optional[int]
    
    bungeecordsPos: Optional[Position]
    bungeecordsWidth: Optional[int]
    bungeecordsHeight: Optional[int]
    
    paperclipPos: Optional[Position]
    paperclipWidth: Optional[int]
    paperclipHeight: Optional[int]
    
    drawerPos: Optional[Position]
    drawerWidth: Optional[int]
    drawerHeight: Optional[int]
    drawer: Optional[str]
    drawerOpen: Optional[bool]

    lemonPos: Optional[Position]
    lemonWidth: Optional[int]
    lemonHeight: Optional[int]
    
    mugPos: Optional[Position]
    mugWidth: Optional[int]
    mugHeight: Optional[int]
    mug: Optional[str]
    mugOpen: Optional[bool]
    mugFull: Optional[bool]
    mugLiquidColor: Optional[str]

    gardenhosePos: Optional[Position]
    gardenhoseWidth: Optional[int]
    gardenhoseHeight: Optional[int]
    
    sodabottlePos: Optional[Position]
    sodabottleWidth: Optional[int]
    sodabottleHeight: Optional[int]
    sodabottle: Optional[str]
    sodabottleFull: Optional[bool]
    sodabottleLiquidColor: Optional[str]
    
    ducttapePos: Optional[Position]
    ducttapeWidth: Optional[int]
    ducttapeHeight: Optional[int]
    
    hangingplantsPos: Optional[Position]
    hangingplantsWidth: Optional[int]
    hangingplantsHeight: Optional[int]

    ladderPos: Optional[Position]
    ladderWidth: Optional[int]
    ladderHeight: Optional[int]
    isElevated: Optional[bool]

    underRelationships: Optional[Dict[str, str]]


# ============= Object Classes =============

class Object:
    def __init__(self, name: str, size: Tuple[int, int], can_open: bool = False, 
                 movable: bool = False, carryable: bool = True, initial_position: Position = (0, 0)):
        self.name = name
        self.size = size
        self.can_open = can_open
        self.movable = movable
        self.carryable = carryable
        self._is_open = False
        self._temporarily_immovable = False
        
        self._load_image()
        self.rect.topleft = initial_position
    
    def _load_image(self):
        if self.can_open:
            image_path = f'{self.name}_open.png' if self._is_open else f'{self.name}_closed.png'
        else:
            image_path = f'{self.name}.png'
        
        self.image = pygame.image.load(image_path).convert_alpha()
        self.image = pygame.transform.scale(self.image, self.size)
        
        if hasattr(self, 'rect'):
            old_pos = self.rect.topleft
            self.rect = self.image.get_rect()
            self.rect.topleft = old_pos
        else:
            self.rect = self.image.get_rect()
    
    def draw(self, surface: pygame.Surface):
        surface.blit(self.image, self.rect)
    
    @property
    def width(self) -> int:
        return self.rect.width
    
    @property
    def height(self) -> int:
        return self.rect.height
    
    @property
    def position(self) -> Position:
        return (self.rect.x, self.rect.y)
    
    @position.setter
    def position(self, pos: Position):
        self.rect.topleft = pos
    
    @property
    def get_name(self) -> str:
        return self.name
    
    def open(self) -> bool:
        if not self.can_open:
            return False
        if not self._is_open:
            self._is_open = True
            self._load_image()
        return True
    
    def close(self):
        if not self.can_open:
            raise ValueError(f"{self.name} cannot be closed")
        if self._is_open:
            self._is_open = False
            self._load_image()
    
    @property
    def is_open(self) -> bool:
        return self._is_open
    
    @property
    def is_movable(self) -> bool:
        return self.movable and not self._temporarily_immovable
    
    def set_temporarily_immovable(self, immovable: bool):
        self._temporarily_immovable = immovable

class Container(Object):
    """Container class that can be full/empty and open/closed. Empties when tilted while open."""
    
    def __init__(self, name: str, size: Tuple[int, int], initial_position: Position = (0, 0),
                 movable: bool = True, carryable: bool = True, starts_full: bool = True,
                 liquid_color: Optional[str] = None):
        # Call parent Object constructor
        self._is_full = starts_full
        self._rotation = 0
        self.liquid_color = liquid_color  # Color of liquid when full (e.g., "blue", "brown", "clear")
        super().__init__(name, size, can_open=True, movable=movable, 
                        carryable=carryable, initial_position=initial_position)

    
    def _load_image(self):
        """Override parent's _load_image to handle full/empty states."""
        # Determine image based on full/empty state (ignore open/closed for image)
        if self.is_full:
            image_path = f'{self.name}_full.png'
        else:
            image_path = f'{self.name}_empty.png'
        
        self.image = pygame.image.load(image_path).convert_alpha()
        self.image = pygame.transform.scale(self.image, self.size)
        
        if hasattr(self, 'rect'):
            old_pos = self.rect.topleft
            self.rect = self.image.get_rect()
            self.rect.topleft = old_pos
        else:
            self.rect = self.image.get_rect()
    
    @property
    def is_full(self) -> bool:
        return self._is_full
    
    @property
    def rotation(self) -> int:
        return self._rotation
    
    def set_rotation(self, degrees: int):
        """Set rotation/tilt. If tilted while open, container empties."""
        self._rotation = degrees % 360
        
        # If open and tilted significantly (more than 30 degrees), empty the container
        if self._is_open and abs(self._rotation) > 30 and abs(self._rotation) < 330:
            self.empty()
    
    def fill(self, liquid_color: Optional[str] = None):
        """Fill the container with optional liquid color specification."""
        if not self._is_full:
            self._is_full = True
            if liquid_color:
                self.liquid_color = liquid_color
            self._load_image()
    
    def empty(self):
        """Empty the container."""
        if self._is_full:
            self._is_full = False
            self._load_image()
    
    def open(self) -> bool:
        """Override parent's open to check if tilted - empties if tilted."""
        if not self._is_open:
            self._is_open = True
            # Check if already tilted, if so, empty immediately
            if abs(self._rotation) > 30 and abs(self._rotation) < 330:
                self.empty()
            # Don't reload image since we don't have open/closed versions
        return True
    
    def close(self) -> bool:
        """Override parent's close to prevent closing if tilted."""
        if abs(self._rotation) > 30 and abs(self._rotation) < 330:
            return False  # Can't close while tilted
        
        if self._is_open:
            self._is_open = False
        return True
    
class Button(Object):
    """Button class that can be pushed and detects objects on top of it."""
    
    def __init__(self, name: str, size: Tuple[int, int], initial_position: Position = (0, 0)):
        super().__init__(name, size, can_open=False, movable=False, initial_position=initial_position)
        self._is_pressed = False
    
    @property
    def is_pressed(self) -> bool:
        return self._is_pressed
    
    def press(self):
        """Mark button as pressed."""
        self._is_pressed = True
    
    def release(self):
        """Mark button as released."""
        self._is_pressed = False
    
    def check_if_pressed(self, objects_list: List[Object], robot_pushing: bool, robot_near: bool) -> bool:
        """
        Check if button should be pressed based on objects on top or robot pushing.
        
        Args:
            objects_list: List of objects that could be on the button
            robot_pushing: Whether robot is in pushing state
            robot_near: Whether robot is near the button
        
        Returns:
            True if button should be pressed, False otherwise
        """
        # Check if robot is pushing while near
        if robot_pushing and robot_near:
            self._is_pressed = True
            return True
        
        # Check if any object is on top of the button
        button_rect = pygame.Rect(self.position[0], self.position[1], self.width, self.height)
        
        for obj in objects_list:
            if not obj.movable:  # Only movable objects can press button
                continue
            
            obj_rect = pygame.Rect(obj.position[0], obj.position[1], obj.width, obj.height)
            
            # Check if object overlaps with button
            if button_rect.colliderect(obj_rect):
                # Calculate overlap area to determine if significantly on button
                overlap_x = min(button_rect.right, obj_rect.right) - max(button_rect.left, obj_rect.left)
                overlap_y = min(button_rect.bottom, obj_rect.bottom) - max(button_rect.top, obj_rect.top)
                overlap_area = overlap_x * overlap_y
                
                # If overlap is significant (> 30% of button area), consider it pressed
                if overlap_area > (self.width * self.height * 0.3):
                    self._is_pressed = True
                    return True
        
        # Nothing pressing the button
        self._is_pressed = False
        return False
    

class Door(Object):
    """Door class that can be locked/unlocked and opened/closed."""
    
    def __init__(self, name: str, size: Tuple[int, int], initial_position: Position = (0, 0), 
                 locked: bool = True, key_name: Optional[str] = None):
        super().__init__(name, size, can_open=True, movable=False, initial_position=initial_position)
        self._locked = locked
        self._key_inserted = False
        self._key_rotation = 0  # Track rotation of inserted key
        self.key_name = key_name  # Name of key that unlocks this door
        self._inserted_object: Optional[Object] = None
    
    @property
    def is_locked(self) -> bool:
        return self._locked
    
    @property
    def key_inserted(self) -> bool:
        return self._key_inserted
    
    @property
    def key_rotation(self) -> int:
        return self._key_rotation
    
    def insert_key(self, key: Object) -> bool:
        print("inside insert key")
        """Insert a key into the door lock."""
        if self._key_inserted:
            return False  # Already has a key
        if key.get_name != self.key_name:
            return False  # Wrong key
        
        self._key_inserted = True
        self._inserted_object = key
        self._key_rotation = 0
        return True
    
    def rotate_key(self, degrees: int) -> bool:
        """Rotate the inserted key."""
        if not self._key_inserted:
            return False
        
        self._key_rotation = (self._key_rotation + degrees) % 360
        print("in key rotation")
        
        # Unlock if rotated 90 degrees or more
        if self._key_rotation >= 90:
            print("unlocked")
            print("locked bool is ", self._locked)
            self._locked = not self._locked
            return True
        
        return True
    
    def open(self) -> bool:
        """Override open to check if locked."""
        if self._locked:
            return False
        if not self._is_open:
            self._is_open = True
            self._load_image()
        return True
    
    def lock(self):
        """Lock the door."""
        self._locked = True
    
    def unlock(self):
        """Unlock the door."""
        self._locked = False
    
    def remove_key(self) -> Optional[Object]:
        """Remove the inserted key."""
        if not self._key_inserted:
            return None
        
        removed = self._inserted_object
        self._key_inserted = False
        self._inserted_object = None
        self._key_rotation = 0
        return removed


class Robot:
    class HandState:
        NEUTRAL = "neutral"
        EXTENDED = "extended"
    
    def __init__(self, size: Tuple[int, int], initial_position: Position = (0, 0), 
                 speed: int = 20, pickup_range: int = 80):
        self.size = size
        self.robot_pos = initial_position
        self.speed = speed
        self.pickup_range = pickup_range
        
        self.image = pygame.image.load('robot.png').convert_alpha()
        self.image = pygame.transform.scale(self.image, size)
        self.rect = self.image.get_rect()
        self.rect.topleft = initial_position
        
        self._hand_state = Robot.HandState.NEUTRAL
        self._hand_rotation = 0
        self._is_pushing = False
        self._held_object: Optional[Object] = None
        self._held_object_offset: Optional[Tuple[int, int]] = None  # Offset from robot position
        self._grasped_object: Optional[Object] = None  # Object being carried (can move with robot)
        self._elevated = False
    
    def draw(self, surface: pygame.Surface):
        if self._hand_rotation != 0:
            rotated_image = pygame.transform.rotate(self.image, self._hand_rotation)
            rotated_rect = rotated_image.get_rect(center=self.rect.center)
            surface.blit(rotated_image, rotated_rect)
        else:
            surface.blit(self.image, self.rect)
        
        # Draw held object on top of robot
        if self._held_object is not None:
            self._held_object.draw(surface)

    def squeeze(self):
        """Squeeze then release the hand - useful for squeezing objects like lemons."""
        # Simulate squeezing by briefly closing then opening the hand state
        # This is a momentary action that applies pressure
        pass  # The actual squeezing logic would be handled by the game

    @property
    def is_elevated(self) -> bool:
        return self._elevated
    
    def elevate(self) -> bool:
        """Get on top of ladder or stool."""
        self._elevated = True
        return True
    
    def descend(self) -> bool:
        """Get down from ladder or stool."""
        if not self._elevated:
            return False
        self._elevated = False
        return True
    
    def squeeze_held_object(self) -> bool:
        """Squeeze the currently held object."""
        if self._held_object is None:
            return False
        return True
    
    @property
    def position(self) -> Position:
        return self.robot_pos
    
    @position.setter
    def position(self, pos: Position):
        old_pos = self.robot_pos
        self.robot_pos = pos
        self.rect.topleft = pos
        
        # Calculate delta for moving grasped objects
        delta_x = pos[0] - old_pos[0]
        delta_y = pos[1] - old_pos[1]
        
        # Update held object position if carrying one
        if self._held_object is not None and self._held_object_offset is not None:
            self._held_object.position = (pos[0] + self._held_object_offset[0], 
                                         pos[1] + self._held_object_offset[1])
        
        # Update grasped object position if grasping one
        if self._grasped_object is not None:
            self._grasped_object.position = (
                self._grasped_object.position[0] + delta_x,
                self._grasped_object.position[1] + delta_y
            )
            
            # Return delta for Game to move stacked objects
            self._last_delta = (delta_x, delta_y)
    
    @property
    def width(self) -> int:
        return self.rect.width
    
    @property
    def height(self) -> int:
        return self.rect.height
    
    @property
    def hand_state(self) -> str:
        return self._hand_state
    
    @property
    def hand_rotation(self) -> int:
        return self._hand_rotation
    
    @property
    def is_pushing(self) -> bool:
        return self._is_pushing
    
    @property
    def held_object_name(self) -> Optional[str]:
        if self._held_object is None:
            return None
        return self._held_object.get_name
    
    @property
    def is_holding_object(self) -> bool:
        return self._held_object is not None
    
    def can_move(self) -> bool:
        if self._is_pushing:
            return False
        return True
    
    def move(self, position_change: Tuple[int, int]) -> bool:
        if not self.can_move():
            return False
        
        new_position = (self.robot_pos[0] + position_change[0], 
                       self.robot_pos[1] + position_change[1])
        
        self.position = new_position  # This will automatically update held/grasped objects
        
        return True
    
    def extend_hand(self):
        self._hand_state = Robot.HandState.EXTENDED
    
    def retract_hand(self):
        self._hand_state = Robot.HandState.NEUTRAL
    
    def rotate_hand(self, degrees: int):
        self._hand_rotation = (self._hand_rotation + degrees) % 360
    
    def set_hand_rotation(self, degrees: int):
        self._hand_rotation = degrees % 360
    
    def reset_hand_rotation(self):
        self._hand_rotation = 0
    
    def start_pushing(self):
        self._is_pushing = True
    
    def stop_pushing(self):
        self._is_pushing = False
    
    def can_pick_up(self, obj: Object) -> bool:
        if not obj.movable:
            return False
        
        object_center_x = obj.position[0] + obj.width // 2
        object_center_y = obj.position[1] + obj.height // 2
        robot_center_x = self.robot_pos[0] + self.width // 2
        robot_center_y = self.robot_pos[1] + self.height // 2
        
        distance = ((robot_center_x - object_center_x) ** 2 + 
                   (robot_center_y - object_center_y) ** 2) ** 0.5
        
        return distance <= self.pickup_range
    
    def pick_up_object(self, obj: Object) -> bool:
        print("in pickup")
        if self._held_object is not None:
            return False
        if not self.can_pick_up(obj):
            print("cant pickup")
            return False
        
        self._held_object = obj
        # Position object centered on robot (in its "hand")
        # Place it slightly above and centered horizontally
        center_offset_x = (self.width - obj.width) // 2
        center_offset_y = -obj.height - 5  # Above robot with small gap
        
        self._held_object_offset = (center_offset_x, center_offset_y)
        
        # Immediately update the object's position
        self._held_object.position = (self.robot_pos[0] + center_offset_x,
                                    self.robot_pos[1] + center_offset_y)
        return True
    
    def release_object(self) -> Optional[Object]:
        released = self._held_object
        self._held_object = None
        self._held_object_offset = None
        return released
    
    def grasp_object(self, obj: Object) -> bool:
        """Grasp an object - if carryable, it moves with the robot."""
        if not obj.carryable:
            return False
        if self._grasped_object is not None:
            return False
        
        self._grasped_object = obj
        return True
    
    def release_grasp(self) -> Optional[Object]:
        """Release grasped object."""
        released = self._grasped_object
        self._grasped_object = None
        return released
    
    def hold_object(self, obj: Object) -> bool:
        self._held_object = obj
        center_offset_x = (self.width - obj.width) // 2
        center_offset_y = -obj.height - 5  # Above robot with small gap
        
        self._held_object_offset = (center_offset_x, center_offset_y)
        
        # Immediately update the object's position
        self._held_object.position = (self.robot_pos[0] + center_offset_x,
                                    self.robot_pos[1] + center_offset_y)
        return True
    
    def get_held_object(self) -> Optional[Object]:
        return self._held_object
    
    def get_grasped_object(self) -> Optional[Object]:
        return self._grasped_object


# ============= Game Class =============

class Game:
    def __init__(self) -> None:

        # World dimensions
        self.width, self.height = 1600, 600
        self.wall_width = 10
        self.dividing_wall_x = self.width // 2
        

        # Pygame
        self.running = False
        pygame.init()
        pygame.display.set_caption("BoxBot Environment")
        self.screen = pygame.display.set_mode((self.width, self.height))
        self.clock = pygame.time.Clock()
        self.font = pygame.font.Font(None, 36)

        
        # Environment state
        self.lights_on = True
        self.key_inserted = False
        
        # Robot
        robot_height = 40
        robot_width = math.ceil(206 * (robot_height/188))
        self.robot = Robot(
            size=(robot_width, robot_height),
            initial_position=(self.width // 4, self.height // 2),
            speed=20,
            pickup_range=180
        )
        
        # Room 1 objects
        self.box = Object('box', (40, math.ceil(59 * (40/54))), movable=True, 
                         initial_position=(self.dividing_wall_x // 2 + 200, self.height // 2 - 100))
        
        self.chest = Object('chest', (100, 80), can_open=True, movable=True, carryable=False,
                           initial_position=(self.dividing_wall_x // 2 - 200, self.height // 2 + 200))
        
        self.plants = Object('plants', (20, 30), movable=False, carryable=False,
                            initial_position=(self.dividing_wall_x // 2 - 350, self.height // 2 + 150))
        
        self.bottle = Container('bottle', (20, 30), 
                               initial_position=(self.dividing_wall_x // 2 - 350, self.height // 2 - 100),
                               movable=True,
                               carryable=True,
                               starts_full=True,
                               liquid_color="blue")
        
        self.faucet = Object('faucet', (20, 20), movable=False, carryable=False,
                            initial_position=(self.dividing_wall_x // 2 - 100, self.height // 2 - 200))
        
        self.lightButton = Button('lightButton', (40, 40), 
                                  initial_position=(self.dividing_wall_x - 100, self.height - 100))
        self.is_box_on_button = False
        
        self.key = Object('key', (20, 40), movable=True, carryable=True,
                         initial_position=(self.chest.position[0] + 50, self.chest.position[1] - 30))
        
        # Door
        self.doorway_height = 100
        self.door = Door('door', (self.wall_width, self.doorway_height),
                        initial_position=(self.dividing_wall_x, self.height // 2 - 50),
                        locked=True,
                        key_name='key')

        self.door_locked = self.door.is_locked
        
        # Room 2 objects
        self.window = Object('window', (150, 100), movable=False,
                            initial_position=(self.width - 400, 150))
        
        self.stool = Object('stool', (60, 80), movable=True,
                           initial_position=(self.width - 250, self.height - 150))
        
        # Bookshelf scenario
        self.bookcase = Object('bookcase', (100, 150), movable=False,
                              initial_position=(700, 50))
        
        self.book1 = Object('book1', (20, 25), movable=False,
                           initial_position=(720, 100))
        
        self.book2 = Object('book2', (20, 25), movable=False,
                           initial_position=(722, 100))
        
        self.note = Object('note', (15, 30), movable=True,
                          initial_position=(721, 100))
        self.note_visible = False
        self.note_reachable = False
        
        self.broom = Object('broom', (30, 100), movable=True,
                           initial_position=(20, 100))
        
        self.mousepad = Object('mousepad', (30, 50), movable=True,
                              initial_position=(400, 450))
        
        self.pen = Object('pen', (25, 80), movable=True,
                         initial_position=(600, 470))
        
        self.rollingpin = Object('rollingpin', (40, 150), movable=True,
                                initial_position=(700, 400))
        
        self.spatula = Object('spatula', (30, 50), movable=True,
                             initial_position=(50, 300))
        
        self.blanket = Object('blanket', (50, 50), movable=True, carryable=True, 
                             initial_position=(self.dividing_wall_x // 2, self.height // 2 - 200))
        
        # New items
        self.fork = Object('fork', (15, 60), movable=True, carryable=True,
                          initial_position=(200, 400))
        
        self.suctioncup = Object('suctioncup', (30, 30), movable=True, carryable=True,
                                initial_position=(300, 350))
        
        self.magnet = Object('magnet', (35, 25), movable=True, carryable=True,
                            initial_position=(150, 250))
        
        self.bungeecords = Object('bungeecords', (60, 10), movable=True, carryable=True,
                              initial_position=(500, 500))
        
        self.paperclip = Object('paperclip', (20, 15), movable=True, carryable=True,
                               initial_position=(100, 450))
        
        self.drawer = Object('drawer', (80, 40), can_open=True, movable=False, carryable=False,
                            initial_position=(70, 100))
        
        # Lemon
        self.lemon = Object('lemon', (25, 35), movable=True, carryable=True,
                           initial_position=(250, 300))
        
        # Mug (Container)
        self.mug = Container('mug', (40, 50), 
                            initial_position=(500, 250),
                            movable=True, 
                            carryable=True,
                            starts_full=False,
                            liquid_color=None)
        
        # Garden hose
        self.gardenhose = Object('gardenhose', (80, 30), movable=True, carryable=True,
                                initial_position=(100, 200))
        
        # Large soda bottle (Container - starts empty)
        self.sodabottle = Container('sodabottle', (30, 60), 
                                   initial_position=(450, 400),
                                   movable=True,
                                   carryable=True,
                                   starts_full=False,
                                   liquid_color=None)
        
        # Duct tape
        self.ducttape = Object('ducttape', (35, 35), movable=True, carryable=True,
                              initial_position=(550, 350))
        
        # Ladder
        self.ladder = Object('ladder', (50, 120), movable=True, carryable=True,
                            initial_position=(150, 400))
        
        self.hangingplants = Object('hangingplants', (40, 50), movable=False, carryable=False,
                                    initial_position=(200, 50))
        
        # Spatula state tracking
        self.spatula_inserted = False
        self.spatula_rotated_after_insert = False
        
        # Belief tracking
        self.belieffilePath = "/mnt/c/Users/hamid/Desktop/myboxbot/diarc/core/src/main/resources/config/edu/tufts/hrilab/belief/agents/boxbot.pl"
        self.discovered_objects: set = set()

        self.under_relationships = {}

        self.obj_map = {
            'box': self.box,
            'chest': self.chest,
            'door': self.door,
            'lightButton': self.lightButton,
            'plants': self.plants,
            'bottle': self.bottle,
            'faucet': self.faucet,
            'window': self.window,
            'stool': self.stool,
            'key': self.key,
            'bookcase': self.bookcase,
            'book1': self.book1,
            'book2': self.book2,
            'note': self.note,
            'broom': self.broom,
            'mousepad': self.mousepad,
            'pen': self.pen,
            'rollingpin': self.rollingpin,
            'spatula': self.spatula, 
            'blanket': self.blanket,
            'fork': self.fork,
            'suctioncup': self.suctioncup,
            'magnet': self.magnet,
            'bungeecords': self.bungeecords,
            'paperclip': self.paperclip,
            'drawer': self.drawer, 
            'lemon': self.lemon,
            'mug': self.mug, 
            'gardenhose': self.gardenhose,
            'sodabottle': self.sodabottle,
            'ducttape': self.ducttape,
            'ladder': self.ladder,
            'hangingplants': self.hangingplants
        }
    
    def get_current_room(self) -> str:
        """Determine which room the robot is in."""
        if self.robot.position[0] < self.dividing_wall_x:
            return "Room1"
        return "Room2"
    
    def distance_to(self, obj: Object) -> float:
        """Calculate distance from robot to object."""
        robot_center_x = self.robot.position[0] + self.robot.width // 2
        robot_center_y = self.robot.position[1] + self.robot.height // 2
        obj_center_x = obj.position[0] + obj.width // 2
        obj_center_y = obj.position[1] + obj.height // 2
        
        return ((robot_center_x - obj_center_x) ** 2 + 
                (robot_center_y - obj_center_y) ** 2) ** 0.5
    
    def is_near(self, obj: Object, threshold: float = 100) -> bool:
        """Check if robot is near an object."""
        return self.distance_to(obj) <= threshold
    
    def get_objects_on_top_of(self, base_obj: Object) -> List[Object]:
        """Find all objects positioned on top of the base object."""
        objects_on_top = []
        base_rect = pygame.Rect(base_obj.position[0], base_obj.position[1], 
                                base_obj.width, base_obj.height)
        
        # Check all potentially stackable objects
        for obj in [self.box, self.bottle, self.key, self.stool, self.broom, 
                    self.mousepad, self.pen, self.rollingpin, self.spatula, 
                    self.blanket, self.chest, self.fork, self.suctioncup, 
                    self.magnet, self.bungeecords, self.paperclip, self.lemon,
                    self.gardenhose, self.sodabottle, self.ducttape, self.ladder]:
            if obj == base_obj:
                continue
            if not obj.carryable and not obj.movable:
                continue
                
            obj_rect = pygame.Rect(obj.position[0], obj.position[1], 
                                  obj.width, obj.height)
            
            # Check if object is on top (overlaps and is above)
            if base_rect.colliderect(obj_rect):
                # Check if object's bottom is within reasonable range of base's area
                overlap_x = min(base_rect.right, obj_rect.right) - max(base_rect.left, obj_rect.left)
                overlap_y = min(base_rect.bottom, obj_rect.bottom) - max(base_rect.top, obj_rect.top)
                
                # Object is on top if there's horizontal overlap and it's not below the base
                if overlap_x > obj.width * 0.3 and obj_rect.centery < base_rect.bottom:
                    objects_on_top.append(obj)
        
        return objects_on_top
    
    def move_object_with_stack(self, obj: Object, delta_x: int, delta_y: int):
        """Move an object and all objects stacked on top of it."""
        # Move the base object
        obj.position = (obj.position[0] + delta_x, obj.position[1] + delta_y)
        
        # Find and move objects on top recursively
        objects_on_top = self.get_objects_on_top_of(obj)
        for top_obj in objects_on_top:
            if top_obj.carryable or top_obj.movable:
                self.move_object_with_stack(top_obj, delta_x, delta_y)
    
    def move_robot_to(self, obj: Object, interaction_distance: int = 20) -> bool:
        """
        Moves the robot to a position where it can interact with the given object.
        """
        if self.is_near(obj, threshold=interaction_distance):
            return True

        robot_center_offset_x = self.robot.width / 2
        robot_center_offset_y = self.robot.height / 2
        
        potential_targets = {
            'left': (obj.rect.centerx - obj.width / 2 - robot_center_offset_x - interaction_distance, obj.rect.centery),
            'right': (obj.rect.centerx + obj.width / 2 + robot_center_offset_x + interaction_distance, obj.rect.centery),
            'top': (obj.rect.centerx, obj.rect.centery - obj.height / 2 - robot_center_offset_y - interaction_distance),
            'bottom': (obj.rect.centerx, obj.rect.centery +obj.height / 2 + robot_center_offset_y + interaction_distance)
        }

        valid_targets = []
        for direction, center_pos in potential_targets.items():
            top_left_pos = (int(center_pos[0] - robot_center_offset_x), int(center_pos[1] - robot_center_offset_y))
            
            if not self._collides(top_left_pos):
                valid_targets.append(center_pos)

        if not valid_targets:
            return False

        robot_center_pos = self.robot.rect.center
        valid_targets.sort(key=lambda pos: math.hypot(pos[0] - robot_center_pos[0], pos[1] - robot_center_pos[1]))
        
        best_target_center = valid_targets[0]
        target_x = int(best_target_center[0] - robot_center_offset_x)
        target_y = int(best_target_center[1] - robot_center_offset_y)

        max_steps = 200
        step_size = 10
        
        for _ in range(max_steps):
            old_pos = self.robot.position
            current_pos = self.robot.position
            
            distance_to_target = math.hypot(current_pos[0] - target_x, current_pos[1] - target_y)
            if distance_to_target < step_size:
                if not self._collides((target_x, target_y)):
                    self.robot.position = (target_x, target_y)
                    # Move grasped object stack
                    grasped = self.robot.get_grasped_object()
                    if grasped:
                        delta_x = target_x - old_pos[0]
                        delta_y = target_y - old_pos[1]
                        self.move_object_with_stack(grasped, delta_x, delta_y)
                return True

            angle = math.atan2(target_y - current_pos[1], target_x - current_pos[0])
            dx = int(step_size * math.cos(angle))
            dy = int(step_size * math.sin(angle))
            
            next_pos = (current_pos[0] + dx, current_pos[1] + dy)
            if dx != 0 and dy != 0 and not self._collides(next_pos):
                self.robot.position = next_pos
                # Move grasped object stack
                grasped = self.robot.get_grasped_object()
                if grasped:
                    self.move_object_with_stack(grasped, dx, dy)
                continue
                
            next_pos_h = (current_pos[0] + dx, current_pos[1])
            if dx != 0 and not self._collides(next_pos_h):
                self.robot.position = next_pos_h
                grasped = self.robot.get_grasped_object()
                if grasped:
                    self.move_object_with_stack(grasped, dx, 0)
                continue

            next_pos_v = (current_pos[0], current_pos[1] + dy)
            if dy != 0 and not self._collides(next_pos_v):
                self.robot.position = next_pos_v
                grasped = self.robot.get_grasped_object()
                if grasped:
                    self.move_object_with_stack(grasped, 0, dy)
                continue

            return False

        return False
    
    def get_object(self, name: str) -> Optional[Object]:
        """Get an object by name."""
        return self.obj_map.get(name)

    def _collides(self, pos: Position) -> bool:
        """Check if robot at given position would collide with walls or obstacles."""
        test_rect = pygame.Rect(pos[0], pos[1], self.robot.width, self.robot.height)
        
        if (test_rect.left <= self.wall_width or 
            test_rect.right >= self.width - self.wall_width or
            test_rect.top <= self.wall_width or
            test_rect.bottom >= self.height - self.wall_width):
            return True
        
        wall_above = pygame.Rect(self.dividing_wall_x, 0, self.wall_width, self.door.position[1])
        if test_rect.colliderect(wall_above):
            return True
        
        wall_below = pygame.Rect(
            self.dividing_wall_x,
            self.door.position[1] + self.doorway_height,
            self.wall_width,
            self.height - (self.door.position[1] + self.doorway_height)
        )
        if test_rect.colliderect(wall_below):
            return True
        
        if not self.door.is_open:
            door_rect = pygame.Rect(self.door.position[0], self.door.position[1], 
                                    self.wall_width, self.doorway_height)
            if test_rect.colliderect(door_rect):
                return True
        
        for obj in [self.chest, self.plants, self.faucet, self.lightButton, self.window, 
                    self.bookcase, self.book1, self.book2]:
            if obj == self.robot.get_held_object() or obj == self.robot.get_grasped_object():
                continue
            obj_rect = pygame.Rect(obj.position[0], obj.position[1], obj.width, obj.height)
            if test_rect.colliderect(obj_rect):
                return True
        
        return False
    
    def _update_lights(self):
        """Update lights based on button state."""
        movable_objects = [self.box, self.bottle, self.key, self.stool]
        
        is_pressed = self.lightButton.check_if_pressed(
            movable_objects,
            self.robot.is_pushing,
            self.is_near(self.lightButton)
        )
        
        self.lights_on = not is_pressed
    
    def get_visible_objects(self) -> Dict[str, Object]:
        """Return dictionary of currently visible objects."""
        visible = {}
        current_room = self.get_current_room()
        
        if current_room == "Room1":
            visible['plants'] = self.plants
            visible['bottle'] = self.bottle
            visible['faucet'] = self.faucet
            visible['lightButton'] = self.lightButton
            visible['chest'] = self.chest
            visible['box'] = self.box
            visible['bookcase'] = self.bookcase
            visible['book1'] = self.book1
            visible['book2'] = self.book2
            visible['spatula'] = self.spatula
            visible['rollingpin'] = self.rollingpin
            visible['broom'] = self.broom
            visible['mousepad'] = self.mousepad
            visible['pen'] = self.pen
            visible['blanket'] = self.blanket
            visible['fork'] = self.fork
            visible['suctioncup'] = self.suctioncup
            visible['magnet'] = self.magnet
            visible['bungeecords'] = self.bungeecords
            visible['paperclip'] = self.paperclip
            visible['drawer'] = self.drawer
            visible['lemon'] = self.lemon
            visible['mug'] = self.mug
            visible['gardenhose'] = self.gardenhose
            visible['sodabottle'] = self.sodabottle
            visible['ducttape'] = self.ducttape
            visible['ladder'] = self.ladder
            visible['hangingplants'] = self.hangingplants
            
            if self.chest.is_open:
                visible['key'] = self.key

            
        
        if current_room == "Room2" or (current_room == "Room1" and self.door.is_open):
            visible['window'] = self.window
            visible['stool'] = self.stool
                
        if self.is_near(self.door, threshold=200):
            visible['door'] = self.door
        
        return visible
    
    def observation(self) -> Observation:
        """Build observation of what agent can currently see."""
        obs = {
            "itemInAgentsHand": self.robot.held_object_name,
            "isHandExtended": self.robot.hand_state == Robot.HandState.EXTENDED,
            "handRotation": self.robot.hand_rotation,
            "isAgentPushing": self.robot.is_pushing,
            "lights": "on" if self.lights_on else "off",
            "location": self._build_location_string(),
            "robotPos": self.robot.position,
            "robotWidth": self.robot.width,
            "robotHeight": self.robot.height,
            "isElevated": self.robot.is_elevated,
            "underRelationships": self.under_relationships.copy() if hasattr(self, 'under_relationships') else {}
        }
        
        visible = self.get_visible_objects()
        
        for name, obj in visible.items():
            obs[f"{name}Pos"] = obj.position
            obs[f"{name}Width"] = obj.width
            obs[f"{name}Height"] = obj.height
            
            if obj.can_open:
                obs[f"{name}Open"] = obj.is_open
                obs[name] = "open" if obj.is_open else "closed"

            # Handle Container objects with liquid color
            if isinstance(obj, Container):
                obs[f"{name}Full"] = obj.is_full
                
                # Build state string with liquid color
                state_parts = []
                state_parts.append("open" if obj.is_open else "closed")
                
                if obj.is_full:
                    if obj.liquid_color:
                        state_parts.append(f"full_{obj.liquid_color}")
                    else:
                        state_parts.append("full")
                else:
                    state_parts.append("empty")
                
                obs[name] = " ".join(state_parts)
                
                # Add liquid color as separate field if known
                if obj.is_full and obj.liquid_color:
                    obs[f"{name}LiquidColor"] = obj.liquid_color
        
        if 'lightButton' in visible:
            obs["islightButtonPressed"] = self.is_box_on_button or \
                                         (self.robot.is_pushing and self.is_near(self.lightButton))
        
        if 'key' in visible:
            obs["keyVisible"] = True
            self._discover_object("key")
        
        if 'window' in visible:
            self._discover_object("window")
        
        if 'stool' in visible:
            self._discover_object("stool")

        # Only add lemonPierced if it has been poked
        if hasattr(self, 'lemon_poked') and self.lemon_poked:
            obs["lemonPierced"] = True
        
        # Only add lemonSqueezed if it has been squeezed
        if hasattr(self, 'lemon_squeezed') and self.lemon_squeezed:
            obs["lemonSqueezed"] = True
        
        # Only add objectMoved if something was moved
        if hasattr(self, 'object_moved') and self.object_moved:
            obs["objectMoved"] = True
        
        obs["wallWidth"] = self.wall_width
        
        return Observation(obs)
    
    def is_object_under(self, obj: Object, base_obj: Object, threshold: int = 10) -> bool:
        """Check if obj is positioned under base_obj."""
        # Check if horizontally aligned (at least partially)
        obj_center_x = obj.position[0] + obj.width // 2
        base_left = base_obj.position[0]
        base_right = base_obj.position[0] + base_obj.width
        
        if not (base_left <= obj_center_x <= base_right):
            return False
        
        # Check if vertically positioned below (with some threshold)
        obj_top = obj.position[1]
        base_bottom = base_obj.position[1] + base_obj.height
        
        # Object is under if its top is near or below the base's bottom
        if abs(obj_top - base_bottom) <= threshold or obj_top > base_bottom:
            return True
        
        return False
    
    def _build_location_string(self) -> str:
        """Build location string with proximity markers."""
        base = self.get_current_room() + "floor"
        markers = []
        
        if self.is_near(self.door): markers.append("door")
        if self.is_near(self.door): markers.append("keyhole")
        if self.is_near(self.chest): markers.append("chest")
        if self.is_near(self.box): markers.append("box")
        if self.is_near(self.lightButton): markers.append("lightButton")
        if self.is_near(self.window): markers.append("window")
        if self.is_near(self.stool): markers.append("stool")
        
        
        if self.chest.is_open and self.is_near(self.key):
            markers.append("key")
        
        if self.is_near(self.bottle): markers.append("bottle")
        if self.is_near(self.faucet): markers.append("faucet")
        if self.is_near(self.plants): markers.append("plants")
        if self.is_near(self.bookcase): markers.append("bookcase")
        if self.is_near(self.book1): markers.append("book1")
        if self.is_near(self.book2): markers.append("book2")
        if self.is_near(self.broom): markers.append("broom")
        if self.is_near(self.mousepad): markers.append("mousepad")
        if self.is_near(self.pen): markers.append("pen")
        if self.is_near(self.rollingpin): markers.append("rollingpin")
        if self.is_near(self.spatula): markers.append("spatula")
        if self.is_near(self.blanket): markers.append("blanket")
        if self.is_near(self.fork): markers.append("fork")
        if self.is_near(self.suctioncup): markers.append("suctioncup")
        if self.is_near(self.magnet): markers.append("magnet")
        if self.is_near(self.bungeecords): markers.append("bungeecords")
        if self.is_near(self.paperclip): markers.append("paperclip")
        if self.is_near(self.drawer): markers.append("drawer")
        if self.is_near(self.lemon): markers.append("lemon")
        if self.is_near(self.mug): markers.append("mug")
        if self.is_near(self.gardenhose): markers.append("gardenhose")
        if self.is_near(self.sodabottle): markers.append("sodabottle")
        if self.is_near(self.ducttape): markers.append("ducttape")
        if self.is_near(self.ladder): markers.append("ladder")

        if self.is_near(self.hangingplants):
            if self.robot.is_elevated:
                markers.append("hangingplants")
            else:
                markers.append("belowhangingplants")
        
        return base + ("-" + "-".join(markers) if markers else "")
    
    def _discover_object(self, obj_name: str):
        """Add object to belief file when discovered."""
        if obj_name in self.discovered_objects:
            return
        
        belief_map = {
            "key": "object(key, object).\nsubtype(key, physical).\nproperty(key, has_weight, true).\nproperty(key, weight_class, light).\nproperty(key, can_be_carried_by, agent).\n",
            "window": "object(window, object).\nsubtype(window, physical).\n",
            "stool": "object(stool, object).\nsubtype(stool, physical).\nproperty(stool, has_weight, true).\nproperty(stool, weight_class, medium).\nproperty(stool, can_be_carried_by, agent).\n"
        }
        
        if obj_name not in belief_map:
            return
        
        try:
            if os.path.exists(self.belieffilePath):
                with open(self.belieffilePath, 'r') as f:
                    if f"object({obj_name}, object)." in f.read():
                        self.discovered_objects.add(obj_name)
                        return
            
            with open(self.belieffilePath, 'a') as f:
                f.write(f"\n% --- Beliefs for {obj_name} ---\n")
                f.write(belief_map[obj_name])
            
            self.discovered_objects.add(obj_name)
        except Exception as e:
            print(f"Error writing beliefs: {e}")
    
    def update(self):
        """Update game state and render."""
        self.clock.tick(30)
        self._render()
        self._update_lights()
        pygame.display.flip()
    
    def _render(self):
        """Render the game world."""
        self.screen.fill((255, 255, 255) if self.lights_on else (100, 100, 100))
        
        pygame.draw.rect(self.screen, (0, 0, 0), (0, 0, self.width, self.wall_width))
        pygame.draw.rect(self.screen, (0, 0, 0), (0, self.height - self.wall_width, self.width, self.wall_width))
        pygame.draw.rect(self.screen, (0, 0, 0), (0, 0, self.wall_width, self.height))
        pygame.draw.rect(self.screen, (0, 0, 0), (self.width - self.wall_width, 0, self.wall_width, self.height))
        
        door_y = self.door.position[1]
        pygame.draw.rect(self.screen, (0, 0, 0), (self.dividing_wall_x, 0, self.wall_width, door_y))
        pygame.draw.rect(self.screen, (0, 0, 0), (self.dividing_wall_x, door_y + self.doorway_height, 
                                                  self.wall_width, self.height - door_y - self.doorway_height))
        
        door_color = (0, 255, 0) if self.door.is_open else (255, 0, 0)
        pygame.draw.rect(self.screen, door_color, (*self.door.position, self.wall_width, self.doorway_height))
        
        visible = self.get_visible_objects()
        for obj in visible.values():
            if self.robot.get_held_object() != obj:
                obj.draw(self.screen)
        
        self.robot.draw(self.screen)
        
        status = f"Lights {'ON' if self.lights_on else 'OFF'}"
        text = self.font.render(status, True, (0, 0, 0) if self.lights_on else (255, 255, 255))
        self.screen.blit(text, (10, 10))

    def set_up(self) -> None:
        self.running = True