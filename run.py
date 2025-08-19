import pygame

# Initialize pygame
pygame.init()

pygame.display.set_caption('Room Simulation')

# Import Game *after* pygame init
from game import Game
game = Game()

game.start()

# Quit pygame
pygame.quit()