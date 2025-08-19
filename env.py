from direction import Direction
import gymnasium as gym
from player_action import PlayerAction, PlayerActionTable
from game import Game, Observation

MOVEMENT_ACTIONS = [PlayerAction.NORTH, PlayerAction.SOUTH, PlayerAction.EAST, PlayerAction.WEST]


class SimulatorEnv(gym.Env):

    def __init__(self):

        super(SimulatorEnv, self).__init__()

        self.unwrapped.step_count = 0
        self.unwrapped.num_players = 1
        self.unwrapped.game = None

    def step(self, action: PlayerAction):
        done = False
        
        game: Game = self.unwrapped.game
        
        success = True

        if action in MOVEMENT_ACTIONS:
            if action == PlayerAction.NORTH:
                direction = Direction.NORTH
            elif action == PlayerAction.SOUTH:
                direction = Direction.SOUTH
            elif action == PlayerAction.EAST:
                direction = Direction.EAST
            else:
                direction = Direction.WEST
            success = game.player_move(direction)
        elif action == PlayerAction.TOGGLE_HOLD:
            success = game.toggle_holding_item()
        elif action == PlayerAction.OPEN:
            success = game.open_item()
        elif action == PlayerAction.PUSH:
            success = game.push_arm()
        elif action == PlayerAction.LIFT:
            success = game.lift_arm()
        elif action == PlayerAction.CLOSE:
            success = game.close_item()
        elif action == PlayerAction.INSERT:
            success = game.insert_object()
        elif action == PlayerAction.ROTATE:
            success = game.rotate_object()
        # elif action == PlayerAction.AT_CHEST:
        #     success = game.at_chest()
        elif action == PlayerAction.RESET:
            game.reset()
        elif action == PlayerAction.GET_OBSERVATION:
            # This is a no-op; we just want to get the observation
            pass
        observation = game.observation()
        if action != PlayerAction.GET_OBSERVATION:
            self.unwrapped.step_count += 1
        if not game.running:
            done = True
        return observation, 0., done, {
            'result': success,
        }, None

    def reset(self) -> Observation:
        game = Game()
        self.unwrapped.game = game
        game.set_up()
        self.unwrapped.step_count = 0
        return game.observation()

    def render(self):
        game: Game = self.unwrapped.game
        game.update()


if __name__ == "__main__":
    env = SimulatorEnv()
    env.reset()

    for i in range(100):
        env.step(PlayerAction.EAST)
        env.render()
