# Author: Gyan Tatiya
# Email: Gyan.Tatiya@tufts.edu

import json
import socket

from utils import recv_socket_data


if __name__ == "__main__":
    # Make the env
    action_commands = ['NORTH', 'SOUTH', 'EAST', 'WEST', 'TOGGLE_HOLD', 'GET_OBSERVATION', 'OPEN', 'CLOSE', 'PUSH', 'LIFT', 'INSERT', 'ROTATE']

    print("action_commands: ", action_commands)

    # Connect to Box Bot simulation
    HOST = '127.0.0.1'
    PORT = 9000
    sock_game = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock_game.connect((HOST, PORT))

    while True:
        # assume this is the only agent in the game
        action = "EAST"

        print("Sending action: ", action)
        sock_game.send(str.encode(action))  # send action to env

        output = recv_socket_data(sock_game)  # get observation from env
        output = json.loads(output)

        print("Observations: ", output["observation"])
        print("Violations", output["violations"])
        
        if output["observation"]["isInPickupRange"]:
            # assume this is the only agent in the game
            action = ""

            print("Sending action: ", action)
            sock_game.send(str.encode(action))  # send action to env

            output = recv_socket_data(sock_game)  # get observation from env
            output = json.loads(output)

            print("Observations: ", output["observation"])
            print("Violations", output["violations"])
            
