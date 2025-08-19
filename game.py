from typing import Tuple, Union, TypedDict
import math

from direction import Direction
from sprites import Robot, Box, Chest, Key
from shapes import Circle, calculate_overlap_percentage, BoxShape, RobotShape, ChestShape
from colors import RED, BLACK, WHITE, GRAY

import pygame

Position = Tuple[int, int]

class Observation(TypedDict):
    itemInAgentsHand: Union[str, None]
    isAgentPushing: bool
    lights: str
    robotPos: Position
    lightButtonPos: Position
    chestPos: Union[Position, None]
    boxPos: Union[Position, None]
    robotWidth: int
    robotHeight: int
    boxWidth: int
    boxHeight: int
    lightButtonWidth: int
    lightButtonHeight: int
    doorTop: int
    doorBottom: int
    wallWidth: int
    chestOpen: bool
    doorOpen: bool
    islightButtonPressed: bool

DIRECTION_VECTOR = {
    Direction.NORTH: (0, -1),
    Direction.SOUTH: (0, 1),
    Direction.EAST: (1, 0),
    Direction.WEST: (-1, 0)
}

class Game:

    def __init__(self) -> None:
        self.agent_pushing = False
        self.robot_speed = 20
        self.pickup_range = 80
        self.wall_width = 10
        self.box_width = 40
        self.box_height = math.ceil(59 * (self.box_width/54))
        self.robot_height = 40
        self.robot_width = math.ceil(206 * (self.robot_height/188))
        self.lightButton_width = 20
        self.lightButton_height = self.lightButton_width
        self._box: Union[Box, None] = None
        self._robot: Union[Robot, None] = None
        self._chest: Union[Chest, None] = None
        self.door_locked = True
        self.key_inserted = False

        
        self.holding_item = None  

        self.toggled_at_lightButton = False
        self.chest_open = False
        self.door_open = False

        self.width, self.height = 800, 600
        self.robot_pos: Position = (self.width // 4, self.height // 2)
        self.lightButton_pos = (self.width - 100, self.height - 100)
        self.box_pos = (self.width // 2, self.height // 2 - 100)
        self.chest_pos = (self.width // 2 -300, self.height // 2 + 200 )
        self._key = None  #?????????????????????????????????????????????????????
        self.key_pos = (self.chest_pos[0] + 50, self.chest_pos[1] + 10)

        self.lightButton = Circle(center=self.lightButton_pos, radius=self.lightButton_width)

        self.doorway_height = 100
        self.doorway_pos = (0, self.height // 2 - self.height // 2)

        self.running = False

        pygame.init()
        pygame.display.set_caption("BoxBot Environment")
        self.screen = pygame.display.set_mode((self.width, self.height))
        self.clock = pygame.time.Clock()
        self.font = pygame.font.Font(None, 36)

    def reset(self) -> None:
        self.robot_pos: Position = (self.width // 4, self.height // 2)
        self.box_pos = (self.width // 2, self.height // 2)
        self.chest_pos =  (self.width // 2 -300, self.height // 2 + 200 )
        self.toggled_at_lightButton = False
        self.chest_open = False
        self.door_open = False
        self.door_locked = True
        self.key_inserted = False

        self.key_pos = (self.chest_pos[0] + 20, self.chest_pos[1] - 30) 
        self.holding_item = None  

    def set_up(self) -> None:
        self.running = True

    def update(self) -> None:
        self.clock.tick(30)
        self._draw_background()
        self._draw_walls()
        self._draw_lightButton()
        self._draw_robot()
        self._draw_box()
        self._draw_key()  
        self._draw_status_text()
        self._draw_status_text()
        self._draw_chest()
        pygame.display.flip()
        
    def _draw_background(self) -> None:
        # Drawing background (change based on light)
        light_on = not (self.is_box_on_lightButton() or self.toggled_at_lightButton)
        if light_on:
            self.screen.fill(WHITE)
        else:
            self.screen.fill(GRAY)

    def _draw_walls(self) -> None:
        # Draw the walls (except for the doorway)
        pygame.draw.rect(self.screen, BLACK, (0, 0, self.width, self.wall_width))  # Top wall
        pygame.draw.rect(self.screen, BLACK, (0, self.height - self.wall_width, self.width, self.wall_width))  # Bottom wall
        pygame.draw.rect(self.screen, BLACK, (self.width - self.wall_width, 0, self.wall_width, self.height))  # Right wall
        
        # Draw the doorway
        pygame.draw.rect(self.screen, BLACK, (0, 0, self.wall_width, self.doorway_pos[1]))  # Left wall above doorway
        pygame.draw.rect(
            self.screen,
            BLACK,
            (
                0,
                self.doorway_pos[1] + self.doorway_height,
                self.wall_width,
                self.height - (self.doorway_pos[1] + self.doorway_height)
            )
        )  # Left wall below doorway
    
    def _draw_robot(self) -> None:
        # Draw the robot
        robot = self._get_robot()
        self.screen.blit(robot.image, (self.robot_pos[0], self.robot_pos[1]), robot.rect)

    def push_arm(self) -> bool:
        """Agent lowers arm to push the switch."""
        self.agent_pushing = True

        if self.at_lightButton():
            self.toggled_at_lightButton = True
            
        return True


    def lift_arm(self) -> bool:
        """Agent raises arm back up."""
        self.agent_pushing = False
        if self.at_lightButton():
            self.toggled_at_lightButton = False
        return True
    
    def insert_object(self) -> bool:
        if self.holding_item == "key" and self.at_door():
            self.key_inserted = True
            return True
        return False
    
    def rotate_object(self) -> bool:
        if self.holding_item == "key" and self.key_inserted and self.at_door():
            self.door_locked = False
            return True
        return False

            
        
    def _draw_box(self) -> None:
        if self.holding_item == "box":
            self.box_pos = (self.robot_pos[0] + self.robot_width, self.robot_pos[1])
        # Draw the box
        box = self._get_box()
        self.screen.blit(box.image, self.box_pos, box.rect)

    def _draw_chest(self) -> None:
        chest = self._get_chest()
        chest.set_open(self.chest_open)
        self.screen.blit(chest.image, self.chest_pos, chest.rect)
        
    def _draw_lightButton(self) -> None:
        # Draw the lightButton
        pygame.draw.circle(self.screen, BLACK, self.lightButton["center"], self.lightButton["radius"])

    def _draw_key(self) -> None:  
        if not self.chest_open:
            return
        if self.holding_item == "key":
            self.key_pos = (self.robot_pos[0] + self.robot_width, self.robot_pos[1])
        key = self._get_key()
        self.screen.blit(key.image, self.key_pos, key.rect)

    
    def _draw_status_text(self) -> None:
        light_on = not (self.is_box_on_lightButton() or self.toggled_at_lightButton)
        # Display status
        status_text = self.font.render(f"Lights {'ON' if light_on else 'OFF'}", True, BLACK if light_on else WHITE)
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
    
    def _get_key(self) -> Key:  
        if not self._key:
            self._key = Key((20, 40))  
        return self._key

    def _can_pickup_key(self) -> bool:  
        print(f"DEBUG: holding item: {self.holding_item}")
        print(f"DEBUG: chest open: {self.chest_open}")
        if self.holding_item or not self.chest_open:
            return False
        
        key = self._get_key()
        robot = self._get_robot()
        
        # Debug positions and sizes
        print(f"DEBUG: robot_pos: {self.robot_pos}")
        print(f"DEBUG: key_pos: {self.key_pos}")
        print(f"DEBUG: key size: {key.rect.width} x {key.rect.height}")
        print(f"DEBUG: pickup_range: {self.pickup_range}")
        
        key_center = (self.key_pos[0] + key.rect.width // 2, self.key_pos[1] + key.rect.height // 2)
        print(f"DEBUG: key_center: {key_center}")
        
        # Calculate distance between robot and key
        robot_center = (self.robot_pos[0] + self.robot_width // 2, self.robot_pos[1] + self.robot_height // 2)
        print(f"DEBUG: robot_center: {robot_center}")
        
        distance = ((robot_center[0] - key_center[0])**2 + (robot_center[1] - key_center[1])**2)**0.5
        print(f"DEBUG: distance between robot and key: {distance}")
        
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
        
        print(f"DEBUG: overlap percentage: {overlap}")
        return overlap > 0
    
    def player_move(self, action: Direction) -> None:
        (x1, y1) = DIRECTION_VECTOR[action]

        return self.move_robot([self.robot_speed * x1, self.robot_speed * y1])
        
    """
    Makes the robot pick up the box if it can.
    
    Returns:
        True if the robot picked up the box, False if it was unable to pick it
        up for any reason
    """
    def grab_item(self) -> bool:
        print(f"DEBUG: Can pickup box: {self._can_pickup_box()}")
        print(f"DEBUG: Can pickup key: {self._can_pickup_key()}")
        if self._can_pickup_box():
            self.holding_item = "box"
            return True
        elif self._can_pickup_key(): 
            self.holding_item = "key"
            return True
        return False

    def release_item(self) -> bool:
        if not self.holding_item:
            return False
        self.holding_item = None
        return True
    
    def toggle_holding_item(self) -> bool:
        print(f"DEBUG: in toggleholding")
        result = False
        if self.holding_item:
            self.release_item()
            result = True
        if self.at_lightButton():
            self.toggled_at_lightButton = not self.toggled_at_lightButton
            return True
        if result == True :
            return True
        else:
            return self.grab_item()
    
    def open_item(self) -> bool:
        if self.at_chest(): 
            self.chest_open = True
            return True 
        
        return False

    def close_item(self) -> bool: 
        if self.at_chest(): 
            self.chest_open = False
            return True 
        return False
        

        
    
    # def collide(self, pos: Position) -> bool:
    #     robot = self._get_robot()
    #     box = self._get_box()
    #     chest = self._get_chest()
        
    #     # collision with the box if not holding it
    #     if not self.holding_item == "box":
    #         if calculate_overlap_percentage(
    #             RobotShape(
    #                 robot=robot,
    #                 topLeft=pos
    #             ),
    #             BoxShape(
    #                 box=box,
    #                 topLeft=self.box_pos
    #             )
    #         ) > 20:
    #             return True

       

    #     return False
    
    def hits_wall(self, pos: Position) -> bool:
        robot_left = self.robot_pos[0]
        robot_right = self.robot_pos[0] + self.robot_width
        
        if self.holding_item == "box":
            robot_right += self.box_width
            

        if robot_left <= self.wall_width:
            # Hits west wall
            return True
        if robot_right >= (self.width - self.wall_width):
            # Hits east wall
            return True
        if pos[1] <= self.wall_width:
            # Hits north wall
            return True
        if pos[1] + self.robot_height >= (self.height - self.wall_width):
            # Hits south wall
            return True
        
        return False
    
    # moves robot
    def move_robot(self, position_change) -> bool:
        new_position = (self.robot_pos[0] + position_change[0], self.robot_pos[1] + position_change[1])

        # if self.collide(new_position) or self.hits_wall(new_position):
        #     return False

        self.robot_pos = new_position
        return True
    
    """
    Getter for the box sprite. This is used in place of directly accessing the field
    because there is a timing issue in constructing the sprite; it seems to depend on some
    state of Pygame being setup so that it can load the image file that does not seem to
    be true when the Game object is being instantiated (or when its static fields are
    instantiated).
    """
    def _get_box(self) -> Box:
        if (not self._box):
            self._box = Box((self.box_width, self.box_height))
        return self._box
    
    """
    See #_get_box() for an explanation of this.
    """
    def _get_robot(self) -> Robot:
        if (not self._robot):
            self._robot = Robot((self.robot_width, self.robot_height))
        return self._robot

    """
    Determines whether the box is currently on top of the lightButton (and not
    being held by the robot)
    
    Returns:
        True if the box is on top of the lightButton and not being held, False
        otherwise
    """
    def is_box_on_lightButton(self) -> bool:
        box = self._get_box()
        if self.holding_item == "box":
            return False
        overlap = calculate_overlap_percentage(
            self.lightButton,
            BoxShape(
                box=box,
                topLeft=self.box_pos
            )
        ) > 0
        return overlap
    
    
    def at_lightButton(self) -> bool:
        robot = self._get_robot()
        return calculate_overlap_percentage(
            RobotShape(
                robot=robot,
                topLeft=self.robot_pos
            ),
            self.lightButton
        ) > 0
    
    def at_chest(self) -> bool:
        robot = self._get_robot()
        chest = self._get_chest() # Get the chest object
        return calculate_overlap_percentage(
            RobotShape(
                robot=robot,
                topLeft=self.robot_pos
            ),
            # Wrap the chest object and its position in a ChestShape dictionary
            ChestShape(
                chest=chest,
                topLeft=self.chest_pos
            )
        ) > 0

    
    def _get_chest(self) -> Chest:
        if not self._chest:
            self._chest = Chest((100, 80))
        return self._chest
    
    

    def observation(self) -> Observation:
        lightButton_pos = (
            self.lightButton_pos[0] - math.ceil(self.lightButton_width / 2),
            self.lightButton_pos[1] - math.ceil(self.lightButton_height / 2)
        )
        obs = Observation({
            "itemInAgentsHand": self.holding_item,
            "isAgentPushing": self.agent_pushing,
            "lights": "off" if (self.is_box_on_lightButton() or self.toggled_at_lightButton) else "on",
            "chestOpen": self.chest_open,
            "doorOpen": self.door_open,
            "islightButtonPressed": (self.is_box_on_lightButton() or self.toggled_at_lightButton),
            "robotPos": self.robot_pos,
            "lightButtonPos": lightButton_pos,
            "boxPos": None if (self.holding_item == "box") else self.box_pos,
            "chestPos": self.chest_pos,
            "robotWidth": self.robot_width,
            "robotHeight": self.robot_height,
            "lightButtonWidth": self.lightButton_width,
            "lightButtonHeight": self.lightButton_height,
            "boxWidth": self.box_width,
            "boxHeight": self.box_height,
            "doorTop": self.doorway_pos[1],
            "doorBottom": self.doorway_pos[1] + self.doorway_height,
            "wallWidth": self.wall_width,
            
        })

        if self.chest_open:
            obs["keyVisible"] = True  # Dynamically add this field

        return obs
