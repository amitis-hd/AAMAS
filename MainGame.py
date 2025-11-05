from typing import Tuple, Union, TypedDict, Optional
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
from typing import Tuple, Optional, List, Dict
import pygame
import math
import os

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

# ============= Object Classes =============

class Object:
    def __init__(self, name: str, size: Tuple[int, int], can_open: bool = False, 
                 movable: bool = False, initial_position: Position = (0, 0)):
        self.name = name
        self.size = size
        self.can_open = can_open
        self.movable = movable
        self._is_open = False
        self._temporarily_immovable = False
        self.rotation = 0  # Add rotation property
        
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
        if self.rotation != 0:
            rotated_image = pygame.transform.rotate(self.image, self.rotation)
            rotated_rect = rotated_image.get_rect(center=self.rect.center)
            surface.blit(rotated_image, rotated_rect)
        else:
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
    
    def open(self):
        if not self.can_open:
            raise ValueError(f"{self.name} cannot be opened")
        if not self._is_open:
            self._is_open = True
            self._load_image()
    
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
        self._held_object_offset: Optional[Tuple[int, int]] = None
    
    def draw(self, surface: pygame.Surface):
        if self._hand_rotation != 0:
            rotated_image = pygame.transform.rotate(self.image, self._hand_rotation)
            rotated_rect = rotated_image.get_rect(center=self.rect.center)
            surface.blit(rotated_image, rotated_rect)
        else:
            surface.blit(self.image, self.rect)
        
        if self._held_object is not None:
            self._held_object.draw(surface)
    
    @property
    def position(self) -> Position:
        return self.robot_pos
    
    @position.setter
    def position(self, pos: Position):
        old_pos = self.robot_pos
        self.robot_pos = pos
        self.rect.topleft = pos
        
        if self._held_object is not None and self._held_object_offset is not None:
            self._held_object.position = (pos[0] + self._held_object_offset[0], 
                                         pos[1] + self._held_object_offset[1])
    
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
        
        self.position = new_position
        
        return True
    
    def extend_hand(self):
        self._hand_state = Robot.HandState.EXTENDED
    
    def retract_hand(self):
        self._hand_state = Robot.HandState.NEUTRAL
    
    def rotate_hand(self, degrees: int):
        self._hand_rotation = (self._hand_rotation + degrees) % 360
        # Also rotate held object if any
        if self._held_object is not None:
            self._held_object.rotation = (self._held_object.rotation + degrees) % 360
    
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
        if self._held_object is not None:
            return False
        if not self.can_pick_up(obj):
            return False
        
        self._held_object = obj
        center_offset_x = (self.width - obj.width) // 2
        center_offset_y = -obj.height - 5
        
        self._held_object_offset = (center_offset_x, center_offset_y)
        
        self._held_object.position = (self.robot_pos[0] + center_offset_x,
                                    self.robot_pos[1] + center_offset_y)
        return True
    
    def release_object(self) -> Optional[Object]:
        released = self._held_object
        self._held_object = None
        self._held_object_offset = None
        return released
    
    def get_held_object(self) -> Optional[Object]:
        return self._held_object


# ============= Game Class =============

class Game:
    def __init__(self) -> None:

        # World dimensions
        self.width, self.height = 1600, 600
        self.wall_width = 10

        # Pygame
        self.running = False
        pygame.init()
        pygame.display.set_caption("Bookshelf Retrieval Environment")
        self.screen = pygame.display.set_mode((self.width, self.height))
        self.clock = pygame.time.Clock()
        self.font = pygame.font.Font(None, 36)

        # Robot
        robot_height = 40
        robot_width = math.ceil(206 * (robot_height/188))
        self.robot = Robot(
            size=(robot_width, robot_height),
            initial_position=(200, self.height // 2),
            speed=20,
            pickup_range=180
        )
        
        # Bookshelf and books
        self.bookcase = Object('bookcase', (300, 400), movable=False,
                              initial_position=(1300, 100))
        
        # Two books on the shelf with a narrow gap between them
        self.book1 = Object('book1', (80, 100), movable=False,
                           initial_position=(1340, 200))
        
        self.book2 = Object('book2', (80, 100), movable=False,
                           initial_position=(1360, 200))
        
        # Note (hidden initially, deep in the bookshelf between books)
        self.note = Object('note', (15, 30), movable=True,
                          initial_position=(1350, 250))
        self.note_visible = False  # Starts invisible
        self.note_reachable = False  # Becomes reachable after spatula manipulation
        
        # Tools
        self.broom = Object('broom', (30, 150), movable=True,
                           initial_position=(200, 400))
        
        self.mousepad = Object('mousepad', (30, 50), movable=True,
                              initial_position=(400, 450))
        
        self.pen = Object('pen', (25, 80), movable=True,
                         initial_position=(600, 470))
        
        self.rollingpin = Object('rollingpin', (40, 150), movable=True,
                                initial_position=(800, 400))
        
        # Spatula - 50 pixels wide (too wide for the gap initially)
        self.spatula = Object('spatula', (40, 80), movable=True,
                             initial_position=(1000, 450))
        
        # Spatula state tracking
        self.spatula_inserted = False
        self.spatula_rotated_after_insert = False
        
        # Belief tracking
        self.belieffilePath = "/mnt/c/Users/hamid/Desktop/myboxbot/diarc/core/src/main/resources/config/edu/tufts/hrilab/belief/agents/boxbot.pl"
        self.discovered_objects: set = set()

        self.obj_map = {
            'bookcase': self.bookcase,
            'book1': self.book1,
            'book2': self.book2,
            'note': self.note,
            'broom': self.broom,
            'mousepad': self.mousepad,
            'pen': self.pen,
            'rollingpin': self.rollingpin,
            'spatula': self.spatula
        }
        
    def get_current_room(self) -> str:
        """Single room environment."""
        return "Room"
    
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
    
    def move_robot_to(self, obj: Object, interaction_distance: int = 40) -> bool:
        """Move robot to object."""
        if self.is_near(obj, threshold=interaction_distance + 10):
            return True

        robot_center_offset_x = self.robot.width / 2
        robot_center_offset_y = self.robot.height / 2
        
        potential_targets = {
            'left': (obj.rect.centerx - obj.width / 2 - robot_center_offset_x - interaction_distance, obj.rect.centery),
            'right': (obj.rect.centerx + obj.width / 2 + robot_center_offset_x + interaction_distance, obj.rect.centery),
            'top': (obj.rect.centerx, obj.rect.centery - obj.height / 2 - robot_center_offset_y - interaction_distance),
            'bottom': (obj.rect.centerx, obj.rect.centery + obj.height / 2 + robot_center_offset_y + interaction_distance)
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
            current_pos = self.robot.position
            
            distance_to_target = math.hypot(current_pos[0] - target_x, current_pos[1] - target_y)
            if distance_to_target < step_size:
                if not self._collides((target_x, target_y)):
                    self.robot.position = (target_x, target_y)
                return True

            angle = math.atan2(target_y - current_pos[1], target_x - current_pos[0])
            dx = int(step_size * math.cos(angle))
            dy = int(step_size * math.sin(angle))
            
            next_pos = (current_pos[0] + dx, current_pos[1] + dy)
            if dx != 0 and dy != 0 and not self._collides(next_pos):
                self.robot.position = next_pos
                continue
                
            next_pos_h = (current_pos[0] + dx, current_pos[1])
            if dx != 0 and not self._collides(next_pos_h):
                self.robot.position = next_pos_h
                continue

            next_pos_v = (current_pos[0], current_pos[1] + dy)
            if dy != 0 and not self._collides(next_pos_v):
                self.robot.position = next_pos_v
                continue

            return False

        return False
    
    def get_object(self, name: str) -> Optional[Object]:
        """Get an object by name."""
        return self.obj_map.get(name)

    def _collides(self, pos: Position) -> bool:
        """Check if robot at given position would collide with walls or obstacles."""
        test_rect = pygame.Rect(pos[0], pos[1], self.robot.width, self.robot.height)
        
        # Boundary walls
        if (test_rect.left <= self.wall_width or 
            test_rect.right >= self.width - self.wall_width or
            test_rect.top <= self.wall_width or
            test_rect.bottom >= self.height - self.wall_width):
            return True
        
        # Immovable objects
        for obj in [self.bookcase, self.book1, self.book2]:
            if obj == self.robot.get_held_object():
                continue
            obj_rect = pygame.Rect(obj.position[0], obj.position[1], obj.width, obj.height)
            if test_rect.colliderect(obj_rect):
                return True
        
        return False
    
    def get_visible_objects(self) -> Dict[str, Object]:
        """Return dictionary of currently visible objects."""
        visible = {
            'bookcase': self.bookcase,
            'book1': self.book1,
            'book2': self.book2,
            'broom': self.broom,
            'mousepad': self.mousepad,
            'pen': self.pen,
            'rollingpin': self.rollingpin,
            'spatula': self.spatula
        }
        
        # Note is only visible after spatula has been rotated while inserted
        if self.note_visible:
            visible['note'] = self.note
        
        return visible
    
    def observation(self) -> Observation:
        """Build observation of what agent can currently see."""
        obs = {
            # Agent state
            "itemInAgentsHand": self.robot.held_object_name,
            "isHandExtended": self.robot.hand_state == Robot.HandState.EXTENDED,
            "handRotation": self.robot.hand_rotation,
            "isAgentPushing": self.robot.is_pushing,
            
            # Environment
            "location": self._build_location_string(),
            
            # Robot
            "robotPos": self.robot.position,
            "robotWidth": self.robot.width,
            "robotHeight": self.robot.height,
        }
        
        # Add visible objects
        visible = self.get_visible_objects()
        
        for name, obj in visible.items():
            obs[f"{name}Pos"] = obj.position
            obs[f"{name}Width"] = obj.width
            obs[f"{name}Height"] = obj.height
        
        # Special spatula state
        obs["spatulaRotation"] = self.spatula.rotation
        obs["spatulaInserted"] = self.spatula_inserted
        
        # Note visibility
        if 'note' in visible:
            obs["noteVisible"] = True
            self._discover_object("note")
        else:
            obs["noteVisible"] = False
        
        return Observation(obs)
    
    def _build_location_string(self) -> str:
        """Build location string with proximity markers."""
        base = self.get_current_room()
        markers = []
        
        # Always check these objects if near (matching the old game.py pattern)
        if self.is_near(self.bookcase): markers.append("bookcase")
        if self.is_near(self.book1): markers.append("book1")
        if self.is_near(self.book2): markers.append("book2")
        if self.is_near(self.broom): markers.append("broom")
        if self.is_near(self.mousepad): markers.append("mousepad")
        if self.is_near(self.pen): markers.append("pen")
        if self.is_near(self.rollingpin): markers.append("rollingpin")
        if self.is_near(self.spatula): markers.append("spatula")
        
        # Add note to location if it's visible AND agent is near the note
        if self.note_visible and self.is_near(self.note):
            markers.append("note")
        
        return base + ("-" + "-".join(markers) if markers else "")
    
    def _discover_object(self, obj_name: str):
        """Add object to belief file when discovered."""
        if obj_name in self.discovered_objects:
            return
        
        belief_map = {
            "note": "object(note, object).\nsubtype(note, physical).\nproperty(note, has_weight, true).\nproperty(note, weight_class, light).\nproperty(note, can_be_carried_by, agent).\n"
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
        pygame.display.flip()
    
    def _render(self):
        """Render the game world."""
        self.screen.fill((255, 255, 255))
        
        # Walls
        pygame.draw.rect(self.screen, (0, 0, 0), (0, 0, self.width, self.wall_width))
        pygame.draw.rect(self.screen, (0, 0, 0), (0, self.height - self.wall_width, self.width, self.wall_width))
        pygame.draw.rect(self.screen, (0, 0, 0), (0, 0, self.wall_width, self.height))
        pygame.draw.rect(self.screen, (0, 0, 0), (self.width - self.wall_width, 0, self.wall_width, self.height))
        
        # Draw visible objects (but not held object)
        visible = self.get_visible_objects()
        for obj in visible.values():
            if self.robot.get_held_object() != obj:
                obj.draw(self.screen)
        
        # Draw robot (which will draw held object on top)
        self.robot.draw(self.screen)
        
        # Status text
        status = f"Spatula Inserted: {self.spatula_inserted} | Note Visible: {self.note_visible}"
        text = self.font.render(status, True, (0, 0, 0))
        self.screen.blit(text, (10, 10))

    def set_up(self) -> None:
        self.running = True
    
    def reset(self):
        """Reset the game state."""
        self.__init__()