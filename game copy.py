from typing import Tuple, Union, TypedDict
import math
import pygame
import os

from shapes import calculate_overlap_percentage

from direction import Direction

DIRECTION_VECTOR = {
    Direction.NORTH: (0, -1),
    Direction.SOUTH: (0, 1),
    Direction.EAST: (1, 0),
    Direction.WEST: (-1, 0)
}

# Assuming these files are in the same directory as this script.

class Robot:
    def __init__(self, size):
        self.image = pygame.image.load('robot.png').convert_alpha()
        self.image = pygame.transform.scale(self.image, size)
        self.rect = self.image.get_rect()
class Plants:
    def __init__(self, size):
        self.image = pygame.image.load('plants.png').convert_alpha()
        self.image = pygame.transform.scale(self.image, size)
        self.rect = self.image.get_rect()

class Bottle:
    def __init__(self, size):
        self.image = pygame.image.load('bottle.png').convert_alpha()
        self.image = pygame.transform.scale(self.image, size)
        self.rect = self.image.get_rect()

class Faucet:
    def __init__(self, size):
        self.image = pygame.image.load('tap.png').convert_alpha()
        self.image = pygame.transform.scale(self.image, size)
        self.rect = self.image.get_rect()

class Box:
    def __init__(self, size):
        self.image = pygame.image.load('box.png').convert_alpha()
        self.image = pygame.transform.scale(self.image, size)
        self.rect = self.image.get_rect()

class Chest:
    def __init__(self, size):
        self.image = pygame.image.load('chest_closed.png').convert_alpha()
        self.image = pygame.transform.scale(self.image, size)
        self.rect = self.image.get_rect()
        self.image_open = pygame.image.load('chest_open.png').convert_alpha()
        self.image_open = pygame.transform.scale(self.image_open, size)

    def set_open(self, is_open):
        if is_open:
            self.image = self.image_open

class Key:
    def __init__(self, size):
        self.image = pygame.image.load('key.png').convert_alpha()
        self.image = pygame.transform.scale(self.image, size)
        self.rect = self.image.get_rect()

class LightButton:
    def __init__(self, size):
        self.image = pygame.image.load('lightbutton.png').convert_alpha()
        self.image = pygame.transform.scale(self.image, size)
        self.rect = self.image.get_rect()

class Stool:
    def __init__(self, size):
        self.image = pygame.image.load('stool.png').convert_alpha()
        self.image = pygame.transform.scale(self.image, size)
        self.rect = self.image.get_rect()

class Window:
    def __init__(self, size):
        self.image = pygame.image.load('window.png').convert_alpha()
        self.image = pygame.transform.scale(self.image, size)
        self.rect = self.image.get_rect()
    
# --- SHAPE DEFINITIONS ---
class Circle:
    def __init__(self, center, radius):
        self.center = center
        self.radius = radius
        self.rect = pygame.Rect(center[0] - radius, center[1] - radius, 2 * radius, 2 * radius)

class BoxShape:
    def __init__(self, box, topLeft):
        self.rect = pygame.Rect(topLeft[0], topLeft[1], box.rect.width, box.rect.height)

class RobotShape:
    def __init__(self, robot, topLeft):
        self.rect = pygame.Rect(topLeft[0], topLeft[1], robot.rect.width, robot.rect.height)
class BottleShape:
    def __init__(self, bottle, topLeft):
        self.rect = pygame.Rect(topLeft[0], topLeft[1], bottle.rect.width, bottle.rect.height)
class PlantsShape:
    def __init__(self, plants, topLeft):
        self.rect = pygame.Rect(topLeft[0], topLeft[1], plants.rect.width, plants.rect.height)

class FaucetShape:
    def __init__(self, faucet, topLeft):
        self.rect = pygame.Rect(topLeft[0], topLeft[1], faucet.rect.width, faucet.rect.height)

class ChestShape:
    def __init__(self, chest, topLeft):
        self.rect = pygame.Rect(topLeft[0], topLeft[1], chest.rect.width, chest.rect.height)

class LightButtonShape:
    def __init__(self, button, topLeft):
        self.rect = pygame.Rect(topLeft[0], topLeft[1], button.rect.width, button.rect.height)

# --- NEW SHAPE CLASSES ADDED FOR CONSISTENCY ---
class StoolShape:
    def __init__(self, stool, topLeft):
        self.rect = pygame.Rect(topLeft[0], topLeft[1], stool.rect.width, stool.rect.height)

class WindowShape:
    def __init__(self, window, topLeft):
        self.rect = pygame.Rect(topLeft[0], topLeft[1], window.rect.width, window.rect.height)

class DoorShape:
    def __init__(self, pos, width, height):
        self.rect = pygame.Rect(pos[0], pos[1], width, height)

# --- COLORS and TYPES ---
RED = (255, 0, 0)
BLACK = (0, 0, 0)
WHITE = (255, 255, 255)
GRAY = (128, 128, 128)
GREEN = (0, 255, 0)
LIGHT_BLUE = (173, 216, 230)

Position = Tuple[int, int]

class Observation(TypedDict):
    itemInAgentsHand: Union[str, None]
    isHandExtended: bool
    isHandRotated: bool
    isAgentPushing: bool

    lights: str
    door: str
    chest: str
    robotPos: Union[Position, None]
    plantsPos: Union[Position, None]
    bottlePos: Union[Position, None]
    faucetPos: Union[Position, None]
    lightButtonPos: Position
    chestPos: Union[Position, None]
    boxPos: Union[Position, None]
    robotWidth: int
    robotHeight: int
    plantsWidth: int
    plantsHeight: int
    bottleWidth: int
    bottleHeight: int
    faucetWidth: int
    faucetHeight: int
    boxWidth: int
    boxHeight: int
    lightButtonWidth: int
    lightButtonHeight: int
    doorPos: Union[Position, None]
    doorWidth: int
    doorHeight: int
    wallWidth: int
    chestOpen: bool
    doorOpen: bool
    islightButtonPressed: bool
    windowPos: Union[Position, None]
    windowHeight: int
    windowWidth: int
    stoolPos: Union[Position, None]
    stoolHeight: int
    stoolWidth: int
    location: str

class Game:

    def __init__(self) -> None:
        self.agent_pushing = False
        self.is_box_on_lightButton = False
        self.robot_speed = 20
        self.pickup_range = 80
        self.wall_width = 10
        self.box_width = 40
        self.box_height = math.ceil(59 * (self.box_width/54))
        self.robot_height = 40
        self.robot_width = math.ceil(206 * (self.robot_height/188))
        self.lightButton_width = 40
        self.lightButton_height = 40
        self._box: Union[Box, None] = None
        self._robot: Union[Robot, None] = None
        self._chest: Union[Chest, None] = None
        self._plants: Union[Plants, None] = None
        self._bottle: Union[Bottle, None] = None
        self._faucet: Union[Faucet, None] = None
        self.door_locked = False
        self.key_inserted = False
        self.plants_height = 30
        self.plants_width = 20
        self.bottle_height = 30
        self.bottle_width = 20
        self.faucet_height = 20
        self.faucet_width = 20

        self.location = "Room1"

        self.holding_item = None
        self.hand_extended = False
        self.hand_rotated = False
        self.lights_on = True
        self.chest_open = False
        self.door_open = False
        self.bottle_open = False
        self.bottle_full = True

        self.width, self.height = 1600, 600
        self.dividing_wall_x = self.width // 2
        
        self.robot_pos: Position = (self.width // 4, self.height // 2)
        self.lightButton_pos = (self.dividing_wall_x - 100, self.height - 100)
        self.box_pos = (self.dividing_wall_x // 2 + 200, self.height // 2 - 100)
        self.chest_pos = (self.dividing_wall_x // 2 - 200, self.height // 2 + 200 )
        self._key = None
        self.key_pos = (self.chest_pos[0] + 50, self.chest_pos[1] + 10)
        self.plants_pos = (self.dividing_wall_x // 2 - 350, self.height // 2 + 150 )
        self.bottle_pos = (self.dividing_wall_x // 2 - 350, self.height // 2 -100 )
        self.faucet_pos = (self.dividing_wall_x // 2 - 100, self.height // 2 - 200 )

        self.doorway_height = 100
        self.doorway_pos = (self.dividing_wall_x, self.height // 2 - self.doorway_height // 2)

        self._window: Union[Window, None] = None
        self.window_width = 150
        self.window_height = 100
        self.window_pos = (self.width - 400, 150)

        self._stool: Union[Stool, None] = None
        self.stool_width = 60
        self.stool_height = 80
        self.stool_pos = (self.width - 250, self.height - 150)

        self._lightButton: Union[LightButton, None] = None

        self.running = False

        # --- NEW: Belief file attributes ---
        self.belieffilePath = "/mnt/c/Users/hamid/Desktop/myboxbot/diarc/core/src/main/resources/config/edu/tufts/hrilab/belief/agents/boxbot.pl"
        self.discovered_objects = set() # Tracks objects discovered in this session

        pygame.init()
        pygame.display.set_caption("BoxBot Environment")
        self.screen = pygame.display.set_mode((self.width, self.height))
        self.clock = pygame.time.Clock()
        self.font = pygame.font.Font(None, 36)

    def _add_belief_to_pl_file(self, object_name: str, prolog_text: str):
        """
        Checks if a belief exists in the .pl file and appends it if not.
        This prevents creating duplicate entries across multiple game sessions.
        """
        # 1. Check if we've already processed this object in the current game run
        if object_name in self.discovered_objects:
            return

        try:
            # 2. Check if the file already contains the core belief to avoid duplicates
            file_content = ""
            if os.path.exists(self.belieffilePath):
                with open(self.belieffilePath, 'r') as f:
                    file_content = f.read()
            
            if f"object({object_name}, object)." in file_content:
                self.discovered_objects.add(object_name) # Mark as discovered for this session and stop
                return

            # 3. If not found, append the new beliefs to the file
            with open(self.belieffilePath, 'a') as f:
                f.write(f"\n% --- Beliefs for {object_name} discovered during gameplay ---\n")
                f.write(prolog_text)
                print(f"✅ Added beliefs for '{object_name}' to {self.belieffilePath}")
            
            # 4. Mark the object as discovered for the current session
            self.discovered_objects.add(object_name)

        except FileNotFoundError:
            print(f"❌ Error: Belief file not found at {self.belieffilePath}")
        except Exception as e:
            print(f"❌ An error occurred while writing to the belief file: {e}")

    def reset(self) -> None:
        self.robot_pos: Position = (self.width // 4, self.height // 2)
        self.box_pos = (self.dividing_wall_x // 2, self.height // 2)
        self.chest_pos = (self.dividing_wall_x // 2 - 200, self.height // 2 + 200 )
        self.lights_on = True
        self.chest_open = False
        self.door_open = False
        self.door_locked = True
        self.key_inserted = False
        self.stool_pos = (self.width - 250, self.height - 150)
        self.key_pos = (self.chest_pos[0] + 20, self.chest_pos[1] - 30)
        self.holding_item = None
        self.discovered_objects.clear() # Reset for the new game

    def set_up(self) -> None:
        self.running = True

    def update(self) -> None:
        self.clock.tick(30)
        self._draw_background()
        self._draw_walls_and_features()
        self._draw_lightButton()
        self._draw_robot()
        self._draw_bottle()
        self._draw_plants()
        self._draw_faucet()
        self._draw_box()
        self._draw_stool()
        self._draw_key()
        self._draw_status_text()
        self._draw_chest()
        self._draw_window()
        pygame.display.flip()

    def _draw_background(self) -> None:
        if self.lights_on:
            self.screen.fill(WHITE)
        else:
            self.screen.fill(GRAY)

    def _draw_walls_and_features(self) -> None:
        # Drawing boundary walls
        pygame.draw.rect(self.screen, BLACK, (0, 0, self.width, self.wall_width))
        pygame.draw.rect(self.screen, BLACK, (0, self.height - self.wall_width, self.width, self.wall_width))
        pygame.draw.rect(self.screen, BLACK, (0, 0, self.wall_width, self.height))
        pygame.draw.rect(self.screen, BLACK, (self.width - self.wall_width, 0, self.wall_width, self.height))

        # Drawing dividing wall
        pygame.draw.rect(self.screen, BLACK, (self.dividing_wall_x, 0, self.wall_width, self.doorway_pos[1]))
        pygame.draw.rect(
            self.screen,
            BLACK,
            (
                self.dividing_wall_x,
                self.doorway_pos[1] + self.doorway_height,
                self.wall_width,
                self.height - (self.doorway_pos[1] + self.doorway_height)
            )
        )

        # Drawing door
        door_color = GREEN if self.door_open else RED
        pygame.draw.rect(self.screen, door_color, (self.doorway_pos[0], self.doorway_pos[1], self.wall_width, self.doorway_height))

    def _draw_robot(self) -> None:
        robot = self._get_robot()
        self.screen.blit(robot.image, (self.robot_pos[0], self.robot_pos[1]))
    def _draw_plants(self) -> None:
        plants = self._get_plants()
        self.screen.blit(plants.image, (self.plants_pos[0], self.plants_pos[1]))
    def _draw_bottle(self) -> None:
        bottle = self._get_bottle()
        self.screen.blit(bottle.image, (self.bottle_pos[0], self.bottle_pos[1]))
    def _draw_faucet(self) -> None:
        faucet = self._get_faucet()
        self.screen.blit(faucet.image, (self.faucet_pos[0], self.faucet_pos[1]))

    def _draw_box(self) -> None:
        if self.holding_item == "box":
            self.box_pos = (self.robot_pos[0] + self.robot_width, self.robot_pos[1])
        box = self._get_box()
        self.screen.blit(box.image, self.box_pos)

    def _draw_stool(self) -> None:
        """Draw stool; if being carried, keep it alongside the robot."""
        if self.holding_item == "stool":
            self.stool_pos = (self.robot_pos[0] + self.robot_width, self.robot_pos[1])
        stool = self._get_stool()
        # Only draw stool once the door is open (stool is in the far room).
        if self.door_open or self.holding_item == "stool":
            self.screen.blit(stool.image, self.stool_pos)

    def _draw_window(self) -> None:
        """Draw window only when the door is open (far room visible)."""
        if not self.door_open:
            return
        window_sprite = self._get_window()
        self.screen.blit(window_sprite.image, self.window_pos)

    def push_arm(self) -> bool:
        """Agent lowers arm to push the switch."""
        self.agent_pushing = True
        if self.at_lightButton():
            self.lights_on = not self.lights_on
        return True

    def lift_arm(self) -> bool:
        """Agent raises arm back up."""
        self.agent_pushing = False
        if self.at_lightButton():
            self.lights_on = not self.lights_on
        return True

    def insert_object(self) -> bool:
        self.hand_extended = True
        print("\n\n insert \n\n")
        if self.holding_item == "key" and self.at_door():
            print("\n\n key \n\n")
            self.key_inserted = True
            return True
        return False

    def rotate_object(self) -> bool:
        self.hand_rotated = not self.hand_rotated
        print("\n\n rotate \n\n")
        if self.holding_item == "key" and self.key_inserted and self.at_door():
            self.door_locked = not self.door_locked
            print("\n\n unlock/lock \n\n")
            return True
        if self.holding_item == "mouthWash" and self.bottle_full:
            self.bottle_full = False
            return True
        return False
    
    def enter(self) -> bool:
        if(self.location == "Room1-door") :
            self.location = "Room2-door"
            return True
        elif(self.location == "Room2-door"):
            self.location = "Room1-door"
            return True
        elif(self.location == "Room2-window"):
                self.location = "Outdoors"

                return True
        return False
        

    def _draw_chest(self) -> None:
        chest = self._get_chest()
        chest.set_open(self.chest_open)
        self.screen.blit(chest.image, self.chest_pos)

    def _draw_lightButton(self) -> None:
        lightButton = self._get_lightButton()
        self.screen.blit(lightButton.image, self.lightButton_pos)

    def _draw_key(self) -> None:
        if not self.chest_open:
            return
        if self.holding_item == "key":
            self.key_pos = (self.robot_pos[0] + self.robot_width, self.robot_pos[1])
        key = self._get_key()
        self.screen.blit(key.image, self.key_pos)

    def _draw_status_text(self) -> None:
        status_text = self.font.render(f"Lights {'ON' if self.lights_on else 'OFF'}", True, BLACK if self.lights_on else WHITE)
        self.screen.blit(status_text, (10, 10))

    def _can_pickup_box(self) -> bool:
        if self.holding_item:
            return False
        box = self._get_box()
        robot = self._get_robot()
        overlap_pickup = calculate_overlap_percentage(
            Circle(
                center=(
                    self.box_pos[0] + box.rect.width // 2,
                    self.box_pos[1] + box.rect.height // 2
                ),
                radius=self.pickup_range
            ),
            RobotShape(
                robot=robot,
                topLeft=self.robot_pos
            )
        )
        return overlap_pickup > 0

    def _can_pickup_stool(self) -> bool:
        """Pickup test for stool (available in far room once door is open)."""
        if self.holding_item:
            return False
        if not (self.door_open):  # Stool is behind the door until opened
            return False
        stool = self._get_stool()
        robot = self._get_robot()
        overlap_pickup = calculate_overlap_percentage(
            Circle(
                center=(
                    self.stool_pos[0] + stool.rect.width // 2,
                    self.stool_pos[1] + stool.rect.height // 2
                ),
                radius=self.pickup_range
            ),
            RobotShape(
                robot=robot,
                topLeft=self.robot_pos
            )
        )
        return overlap_pickup > 0

    def _get_key(self) -> Key:
        if not self._key:
            self._key = Key((20, 40))
        return self._key
    
    def _get_lightButton(self) -> LightButton:
        if not self._lightButton:
            self._lightButton = LightButton((self.lightButton_width, self.lightButton_height))
        return self._lightButton

    def _can_pickup_key(self) -> bool:
        if self.holding_item or not self.chest_open:
            return False
        key = self._get_key()
        robot = self._get_robot()
        key_center = (self.key_pos[0] + key.rect.width // 2, self.key_pos[1] + key.rect.height // 2)
        overlap = calculate_overlap_percentage(
            Circle(
                center=key_center,
                radius=self.pickup_range
            ),
            RobotShape(
                robot=robot,
                topLeft=self.robot_pos
            )
        )
        return overlap > 0

    def player_move(self, action: Direction) -> None:
        (x1, y1) = DIRECTION_VECTOR[action]
        return self.move_robot([self.robot_speed * x1, self.robot_speed * y1])

    def grab_item(self) -> bool:
        if self._can_pickup_box():
            self.holding_item = "box"
            return True
        elif self._can_pickup_key():
            self.holding_item = "key"
            return True
        elif self._can_pickup_stool():
            self.holding_item = "stool"
            return True
        elif self.at_bottle():
            self.holding_item = "mouthWash"
            return True
        return False

    def release_item(self) -> bool:
        if not self.holding_item:
            return False
        if self.holding_item == "box" and self.at_lightButton():
            self.is_box_on_lightButton = True
            self.lights_on = False
        # For stool: just drop it where it is (no special effect)
        self.holding_item = None
        return True

    def toggle_holding_item(self) -> bool:
        if self.holding_item:
            return self.release_item()
        else:
            return self.grab_item()

    def open_item(self) -> bool:
        print("\n\n in open item \n\n")
        if self.at_chest():
            print("\n\n setting chest to open \n\n")
            self.chest_open = True
            return True
        if self.at_door() and not self.door_locked:
            self.door_open = True
            return True
        if self.at_bottle():
            self.bottle_open = True
            return True
        return False

    def close_item(self) -> bool:
        if self.at_chest():
            self.chest_open = False
            return True
        if self.at_door():
            self.door_open = False
            return True
        if self.at_bottle():
            self.bottle_open = False
            return True
        return False

    def _collides(self, pos: Position) -> bool:
        robot = self._get_robot()
        robot_shape = RobotShape(robot, pos)
        
        # Extend robot's collision box if holding a box or stool
        if self.holding_item == "box":
            robot_shape.rect.width += self._get_box().rect.width
        if self.holding_item == "stool":
            robot_shape.rect.width += self._get_stool().rect.width

        # Boundary Walls
        if robot_shape.rect.left <= self.wall_width or \
           robot_shape.rect.right >= self.width - self.wall_width or \
           robot_shape.rect.top <= self.wall_width or \
           robot_shape.rect.bottom >= self.height - self.wall_width:
            return True

        # Dividing Wall
        wall_above_door = pygame.Rect(self.dividing_wall_x, 0, self.wall_width, self.doorway_pos[1])
        wall_below_door = pygame.Rect(
            self.dividing_wall_x,
            self.doorway_pos[1] + self.doorway_height,
            self.wall_width,
            self.height
        )
        if robot_shape.rect.colliderect(wall_above_door) or robot_shape.rect.colliderect(wall_below_door):
            return True

        # Closed Door
        if not self.door_open:
            door_shape = DoorShape(self.doorway_pos, self.wall_width, self.doorway_height)
            if robot_shape.rect.colliderect(door_shape.rect):
                return True

        # Allow approach to interactables
        return False

    def move_robot(self, position_change) -> bool:
        new_position = (self.robot_pos[0] + position_change[0], self.robot_pos[1] + position_change[1])
        # if self._collides(new_position):
        #     return False
        self.robot_pos = new_position
        return True

    def _get_box(self) -> Box:
        if (not self._box):
            self._box = Box((self.box_width, self.box_height))
        return self._box

    def _get_robot(self) -> Robot:
        if (not self._robot):
            self._robot = Robot((self.robot_width, self.robot_height))
        return self._robot
    def _get_plants(self) -> Plants:
        if (not self._plants):
            self._plants = Plants((self.plants_width, self.plants_height))
        return self._plants
    
    def _get_bottle(self) -> Bottle:
        if (not self._bottle):
            self._bottle = Bottle((self.bottle_width, self.bottle_height))
        return self._bottle
    
    def _get_faucet(self) -> Faucet:
        if (not self._faucet):
            self._faucet = Faucet((self.faucet_width, self.faucet_height))
        return self._faucet

    def _get_chest(self) -> Chest:
        if not self._chest:
            self._chest = Chest((100, 80))
        return self._chest

    def _get_window(self) -> Window:
        if not self._window:
            self._window = Window((self.window_width, self.window_height))
        return self._window
    
    def _get_stool(self) -> Stool:
        if not self._stool:
            self._stool = Stool((self.stool_width, self.stool_height))
        return self._stool

    # --- REFACTORED: Using shape classes for consistency ---
    def is_box_on_lightButton(self) -> bool:
        if self.holding_item == "box":
            return False
        
        box = self._get_box()
        lightButton = self._get_lightButton()
        
        overlap = calculate_overlap_percentage(
            LightButtonShape(
                button=lightButton,
                topLeft=self.lightButton_pos
            ),
            BoxShape(
                box=box,
                topLeft=self.box_pos
            )
        )
        return overlap > 0

    def at_lightButton(self) -> bool:
        robot = self._get_robot()
        lightButton = self._get_lightButton()

        robot_x, robot_y = self.robot_pos
        lightButton_x, lightButton_y = self.lightButton_pos
        robot_width, robot_height = robot.rect.width, robot.rect.height
        lightButton_width, lightButton_height = lightButton.rect.width, lightButton.rect.height

        robot_center_x = robot_x + robot_width / 2
        robot_center_y = robot_y + robot_height / 2
        lightButton_center_x = lightButton_x + lightButton_width / 2
        lightButton_center_y = lightButton_y + lightButton_height / 2

        distance = ((robot_center_x - lightButton_center_x) ** 2 + (robot_center_y - lightButton_center_y) ** 2) ** 0.5
        PROXIMITY_THRESHOLD = 100
        return distance <= PROXIMITY_THRESHOLD
    
    def at_door(self) -> bool:
        robot = self._get_robot()
        door_shape = DoorShape(self.doorway_pos, self.wall_width, self.doorway_height)

        robot_x, robot_y = self.robot_pos
        door_x, door_y = self.doorway_pos
        robot_width, robot_height = robot.rect.width, robot.rect.height
        door_width, door_height = door_shape.rect.width, door_shape.rect.height

        robot_center_x = robot_x + robot_width / 2
        robot_center_y = robot_y + robot_height / 2
        door_center_x = door_x + door_width / 2
        door_center_y = door_y + door_height / 2

        distance = ((robot_center_x - door_center_x) ** 2 + (robot_center_y - door_center_y) ** 2) ** 0.5
        PROXIMITY_THRESHOLD = 100
        return distance <= PROXIMITY_THRESHOLD
    
    
    
    def at_chest(self) -> bool:
        print("\n\n in at_chest \n\n")
        robot = self._get_robot()
        chest = self._get_chest()

        robot_x, robot_y = self.robot_pos
        chest_x, chest_y = self.chest_pos
        
        robot_width, robot_height = robot.rect.width, robot.rect.height
        chest_width, chest_height = chest.rect.width, chest.rect.height

        robot_center_x = robot_x + robot_width / 2
        robot_center_y = robot_y + robot_height / 2
        chest_center_x = chest_x + chest_width / 2
        chest_center_y = chest_y + chest_height / 2

        distance = ((robot_center_x - chest_center_x) ** 2 + (robot_center_y - chest_center_y) ** 2) ** 0.5
        PROXIMITY_THRESHOLD = 100 

        overlap_percentage = calculate_overlap_percentage(
            RobotShape(robot=robot, topLeft=self.robot_pos),
            ChestShape(chest=chest, topLeft=self.chest_pos)
        )

        print("\n\n this is overlap percentage: {} \n\n", overlap_percentage)
        return overlap_percentage > 0 or distance <= PROXIMITY_THRESHOLD
    
    def at_plants(self) -> bool:
        print("\n\n in at_plants \n\n")
        robot = self._get_robot()
        plants = self._get_plants()

        robot_x, robot_y = self.robot_pos
        plants_x, plants_y = self.plants_pos
        
        robot_width, robot_height = robot.rect.width, robot.rect.height
        plants_width, plants_height = plants.rect.width, plants.rect.height

        robot_center_x = robot_x + robot_width / 2
        robot_center_y = robot_y + robot_height / 2
        plants_center_x = plants_x + plants_width / 2
        plants_center_y = plants_y + plants_height / 2

        distance = ((robot_center_x - plants_center_x) ** 2 + (robot_center_y - plants_center_y) ** 2) ** 0.5
        PROXIMITY_THRESHOLD = 100 

        overlap_percentage = calculate_overlap_percentage(
            RobotShape(robot=robot, topLeft=self.robot_pos),
            PlantsShape(plants=plants, topLeft=self.plants_pos)
        )

        print("\n\n this is overlap percentage: {} \n\n", overlap_percentage)
        return overlap_percentage > 0 or distance <= PROXIMITY_THRESHOLD
    
    def at_bottle(self) -> bool:
        print("\n\n in at_bottle \n\n")
        robot = self._get_robot()
        bottle = self._get_bottle()

        robot_x, robot_y = self.robot_pos
        bottle_x, bottle_y = self.bottle_pos
        
        robot_width, robot_height = robot.rect.width, robot.rect.height
        bottle_width, bottle_height = bottle.rect.width, bottle.rect.height

        robot_center_x = robot_x + robot_width / 2
        robot_center_y = robot_y + robot_height / 2
        bottle_center_x = bottle_x + bottle_width / 2
        bottle_center_y = bottle_y + bottle_height / 2

        distance = ((robot_center_x - bottle_center_x) ** 2 + (robot_center_y - bottle_center_y) ** 2) ** 0.5
        PROXIMITY_THRESHOLD = 100 

        overlap_percentage = calculate_overlap_percentage(
            RobotShape(robot=robot, topLeft=self.robot_pos),
            BottleShape(Bottle=bottle, topLeft=self.bottle_pos)
        )

        print("\n\n this is overlap percentage: {} \n\n", overlap_percentage)
        return overlap_percentage > 0 or distance <= PROXIMITY_THRESHOLD
    
    def at_faucet(self) -> bool:
        print("\n\n in at_faucet \n\n")
        robot = self._get_robot()
        faucet = self._get_faucet()

        robot_x, robot_y = self.robot_pos
        faucet_x, faucet_y = self.faucet_pos
        
        robot_width, robot_height = robot.rect.width, robot.rect.height
        faucet_width, faucet_height = faucet.rect.width, faucet.rect.height

        robot_center_x = robot_x + robot_width / 2
        robot_center_y = robot_y + robot_height / 2
        faucet_center_x = faucet_x + faucet_width / 2
        faucet_center_y = faucet_y + faucet_height / 2

        distance = ((robot_center_x - faucet_center_x) ** 2 + (robot_center_y - faucet_center_y) ** 2) ** 0.5
        PROXIMITY_THRESHOLD = 100 

        overlap_percentage = calculate_overlap_percentage(
            RobotShape(robot=robot, topLeft=self.robot_pos),
            FaucetShape(faucet=faucet, topLeft=self.faucet_pos)
        )

        print("\n\n this is overlap percentage: {} \n\n", overlap_percentage)
        return overlap_percentage > 0 or distance <= PROXIMITY_THRESHOLD

    # --- NEW: reachability for stool and window ---
    def at_stool(self) -> bool:
        """Close-enough check to interact with the stool (in far room)."""
        if not (self.door_open ):
            return False
        robot = self._get_robot()
        stool = self._get_stool()

        robot_x, robot_y = self.robot_pos
        stool_x, stool_y = self.stool_pos
        robot_w, robot_h = robot.rect.width, robot.rect.height
        stool_w, stool_h = stool.rect.width, stool.rect.height

        robot_cx = robot_x + robot_w / 2
        robot_cy = robot_y + robot_h / 2
        stool_cx = stool_x + stool_w / 2
        stool_cy = stool_y + stool_h / 2

        distance = ((robot_cx - stool_cx) ** 2 + (robot_cy - stool_cy) ** 2) ** 0.5
        PROXIMITY_THRESHOLD = 100
        return distance <= PROXIMITY_THRESHOLD
    
    def at_box(self) -> bool:
        print("\n\n in at_chest \n\n")
        robot = self._get_robot()
        box = self._get_box()

        robot_x, robot_y = self.robot_pos
        box_x, box_y = self.box_pos
        
        robot_width, robot_height = robot.rect.width, robot.rect.height
        box_width, box_height = box.rect.width, box.rect.height

        robot_center_x = robot_x + robot_width / 2
        robot_center_y = robot_y + robot_height / 2
        box_center_x = box_x + box_width / 2
        box_center_y = box_y + box_height / 2

        distance = ((robot_center_x - box_center_x) ** 2 + (robot_center_y - box_center_y) ** 2) ** 0.5
        PROXIMITY_THRESHOLD = 100 

        overlap_percentage = calculate_overlap_percentage(
            RobotShape(robot=robot, topLeft=self.robot_pos),
            BoxShape(box = box, topLeft=self.box_pos)
        )

        print("\n\n this is overlap percentage: {} \n\n", overlap_percentage)
        return overlap_percentage > 0 or distance <= PROXIMITY_THRESHOLD

    def at_window(self) -> bool:
        """Close-enough check to interact with/look at the window (in far room)."""
        if not self.door_open:
            return False
        robot = self._get_robot()
        window = self._get_window()

        robot_x, robot_y = self.robot_pos
        window_x, window_y = self.window_pos
        robot_w, robot_h = robot.rect.width, robot.rect.height
        window_w, window_h = window.rect.width, window.rect.height

        robot_cx = robot_x + robot_w / 2
        robot_cy = robot_y + robot_h / 2
        window_cx = window_x + window_w / 2
        window_cy = window_y + window_h / 2

        distance = ((robot_cx - window_cx) ** 2 + (robot_cy - window_cy) ** 2) ** 0.5
        PROXIMITY_THRESHOLD = 120
        return distance <= PROXIMITY_THRESHOLD

    def observation(self) -> Observation:
        lightButton_pos_centered = (
            self.lightButton_pos[0] + self.lightButton_width // 2,
            self.lightButton_pos[1] + self.lightButton_height // 2
        )
        obs = Observation({
            "itemInAgentsHand": self.holding_item,
            "isHandExtended": self.hand_extended,
            "isHandRotated": self.hand_rotated,
            "isAgentPushing": self.agent_pushing,
            "lights": "on" if self.lights_on else "off",
            "chest": "open" if self.chest_open else "closed",
            "door": "open" if self.door_open else "closed",
            "mouthWash": ("open" if self.bottle_open else "closed") + (" full" if self.bottle_full else " empty"),
            "islightButtonPressed": self.is_box_on_lightButton or (self.agent_pushing and self.at_lightButton()),
            "robotPos": self.robot_pos,
            "plantsPos": self.plants_pos,
            "faucetPos": self.faucet_pos,
            "mouthwashPos": self.bottle_pos,
            "lightButtonPos": lightButton_pos_centered,
            "boxPos": None if (self.holding_item == "box") else self.box_pos,
            "chestPos": self.chest_pos,
            "robotWidth": self.robot_width,
            "robotHeight": self.robot_height,
            "plantsWidth": self.plants_width,
            "plantsHeight": self.plants_height,
            "mouthWashWidth": self.bottle_width,
            "mouthWashHeight": self.bottle_height,
            "faucetWidth": self.faucet_width,
            "faucetHeight": self.faucet_height,
            "lightButtonWidth": self.lightButton_width,
            "lightButtonHeight": self.lightButton_height,
            "boxWidth": self.box_width,
            "boxHeight": self.box_height,
            "doorPos": self.doorway_pos,
            "doorWidth": self.wall_width,
            "doorHeight": self.doorway_height,
            "wallWidth": self.wall_width,
            "doorOpen": self.door_open,
            "chestOpen": self.chest_open,
            "location": self.location ,
        })


        # The key is discovered when the chest is open.
        if self.chest_open:
            obs["keyVisible"] = True
            key_facts = (
                "object(key, object).\n"
                "subtype(key, physical).\n"
                "property(key, has_weight, true).\n"
                "property(key, weight_class, light).\n"
                "property(key, can_be_carried_by, agent).\n"
            )
            self._add_belief_to_pl_file("key", key_facts)

        # The window and stool are discovered when the door is open.
        if self.door_open:
            obs["windowPos"] = self.window_pos
            obs["windowHeight"] = self.window_height
            obs["windowWidth"] = self.window_width
            obs["stoolPos"] = self.stool_pos
            obs["stoolHeight"] = self.stool_height
            obs["stoolWidth"] = self.stool_width

            window_facts = (
                "object(window, object).\n"
                "subtype(window, physical).\n"
            )
            self._add_belief_to_pl_file("window", window_facts)

            stool_facts = (
                "object(stool, object).\n"
                "subtype(stool, physical).\n"
                "property(stool, has_weight, true).\n"
                "property(stool, weight_class, medium).\n"
                "property(stool, can_be_carried_by, agent).\n"
            )
            self._add_belief_to_pl_file("stool", stool_facts)
        
        # --- END MODIFIED SECTION ---

        if self.at_door():
            obs["location"] = obs["location"]+ "-door"
        if self.at_chest():
            obs["location"] = obs["location"]+ "-chest"
        if self.at_box():
            obs["location"] = obs["location"] + "-box"
        if self.at_lightButton():
            obs["location"] = obs["location"] + "-lightButton"
        if self.at_window():
            obs["location"] = obs["location"] + "-window"
        if self.at_stool():
            obs["location"] = obs["location"] + "-stool"

        return obs