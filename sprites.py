import os
import pygame
import math
from typing import Union, Tuple

class _SimulationSprite(pygame.sprite.Sprite):
    def __init__(self, img_path: str, size: Union[int, Tuple[int, int]]) -> None:
        if isinstance(size, int):
            size = (size, size)
        pygame.sprite.Sprite.__init__(self)
        self.image = pygame.transform.scale(
            pygame.image.load(img_path).convert_alpha(),
            size
        )
        self.rect = self.image.get_rect()

class Robot(_SimulationSprite):
    def __init__(self, robot_size: Tuple[int, int]) -> None:
        _SimulationSprite.__init__(
            self,
            img_path=os.path.join(os.path.dirname(__file__), "./robot.png"),
            size=robot_size
        )

class Box(_SimulationSprite):
    def __init__(self, box_size: Tuple[int, int]) -> None:
        _SimulationSprite.__init__(
            self,
            img_path=os.path.join(os.path.dirname(__file__), "./box.png"),
            size=box_size
        )

class Chest(pygame.sprite.Sprite):
    def __init__(self, chest_size: Tuple[int, int]) -> None:
        pygame.sprite.Sprite.__init__(self)
        base_path = os.path.dirname(__file__)
        self.closed_img = pygame.transform.scale(
            pygame.image.load(os.path.join(base_path, "closedChest.png")).convert_alpha(),
            chest_size
        )
        self.open_img = pygame.transform.scale(
            pygame.image.load(os.path.join(base_path, "openChest.png")).convert_alpha(),
            chest_size
        )
        self.image = self.closed_img
        self.rect = self.image.get_rect()

    def set_open(self, is_open: bool) -> None:
        self.image = self.open_img if is_open else self.closed_img


class Key(_SimulationSprite):
    def __init__(self, key_size: Tuple[int, int]) -> None:
        _SimulationSprite.__init__(
            self,
            img_path=os.path.join(os.path.dirname(__file__), "key.png"),
            size=key_size
        )