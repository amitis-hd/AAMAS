# Author: Daniel Kasenberg (adapted from Gyan Tatiya's Minecraft socket)
import argparse
import json
import selectors
import socket
import types

from player_action import PlayerAction, PlayerActionTable
from env import SimulatorEnv

import pygame

ACTION_COMMANDS = ['NORTH', 'SOUTH', 'WEST', 'EAST', 'TOGGLE_HOLD', 'GET_OBSERVATION', 'RESET', 'OPEN', 'CLOSE', 'PUSH', 'LIFT', 'INSERT', 'ROTATE']

def serialize_data(data):
    if isinstance(data, set):
        return list(data)
    elif isinstance(data, dict):
        return {k: serialize_data(v) for k, v in data.items()}
    elif isinstance(data, list):
        return [serialize_data(item) for item in data]
    else:
        return data

class BoxBotEventHandler:
    def __init__(self, env, keyboard_input=False):
        self.env = env
        self.keyboard_input = keyboard_input
        env.reset()
        self.running = True
     
    def handle_events(self):
        self.handle_exploratory_events()
        self.env.render()

    def handle_exploratory_events(self):
        for event in pygame.event.get():
            if event.type == pygame.QUIT or (event.type == pygame.KEYDOWN and event.key == pygame.K_ESCAPE):
                self.env.unwrapped.game.running = False
            elif self.keyboard_input:
                if event.type == pygame.KEYDOWN:
                    if event.key == pygame.K_SPACE:
                        self.env.step(PlayerAction.TOGGLE_HOLD)
                        self.env.step(PlayerAction.OPEN)
                        self.env.step(PlayerAction.CLOSE)
                        self.env.step(PlayerAction.PUSH)
                        self.env.step(PlayerAction.LIFT)
                        self.env.step(PlayerAction.INSERT)
                        self.env.step(PlayerAction.ROTATE)

                    elif event.key == pygame.K_r:
                        self.env.step(PlayerAction.RESET)

        if self.keyboard_input:
            keys = pygame.key.get_pressed()
            if keys[pygame.K_UP]:
                self.env.step(PlayerAction.NORTH)
            elif keys[pygame.K_DOWN]:
                self.env.step(PlayerAction.SOUTH)

            elif keys[pygame.K_LEFT]:
                self.env.step(PlayerAction.WEST)

            elif keys[pygame.K_RIGHT]:
                self.env.step(PlayerAction.EAST)

        self.running = self.env.unwrapped.game.running


def get_action_json(action, env_, obs, reward, done, info_=None):
    if not isinstance(info_, dict):
        result = True
        message = ''
    else:
        result, message = info_['result'], info_.get('message')

    result = 'SUCCESS' if result else 'FAIL'

    action_json = {'command_result': {'command': action, 'result': result, 'message': message,},
                   'observation': obs,
                   'step': env_.unwrapped.step_count,
                   'gameOver': done,
                   'violations': ''}
    return action_json


def accept_wrapper(sock):
    conn, addr = sock.accept()  # Should be ready to read
    print('accepted connection from', addr)
    conn.setblocking(False)
    data = types.SimpleNamespace(addr=addr, inb=b'', outb=b'')
    events = selectors.EVENT_READ | selectors.EVENT_WRITE
    sel.register(conn, events, data=data)


if __name__ == "__main__":

    parser = argparse.ArgumentParser()

    parser.add_argument(
        '--port',
        type=int,
        help="Which port to bind",
        default=9000
    )

    parser.add_argument(
        '--keyboard_input',
        action='store_true'
    )

    args = parser.parse_args()

    # np.random.seed(0)

    # Make the env
    env = SimulatorEnv()

    handler = BoxBotEventHandler(env, keyboard_input=args.keyboard_input)

    sel = selectors.DefaultSelector()
    # Connect to agent
    HOST = '127.0.0.1'
    PORT = args.port
    sock_agent = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock_agent.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock_agent.bind((HOST, PORT))
    sock_agent.listen()
    print('Listening on', (HOST, PORT))
    sock_agent.setblocking(False)

    sel.register(sock_agent, selectors.EVENT_READ, data=None)
    env.reset()
    env.render()
    done = False

    while env.unwrapped.game.running:
        events = sel.select(timeout=0)
        should_perform_action = False
        curr_action = None
        e = []
        handler.handle_events()
        env.render()
        for key, mask in events:
            if key.data is None:
                accept_wrapper(key.fileobj)
            else:
                sock = key.fileobj
                data = key.data
                if mask & selectors.EVENT_READ:
                    recv_data = sock.recv(4096)  # Should be ready to read
                    if recv_data:
                        data.inb += recv_data
                        if len(recv_data) < 4096:
                            #  We've hit the end of the input stream; now we process the input
                            command = data.inb.decode().strip()
                            data.inb = b''
                            e.append((key, mask, command))
                            if command in ACTION_COMMANDS:
                                curr_action = PlayerActionTable[command]
                                should_perform_action = True
                            else:
                                info = {'result': False, 'step_cost': 0.0, 'message': 'Invalid Command'}
                                print("obs:", obs)
                                print("obs serialized:", serialize_data(obs))
                                print("obs json string:", json.dumps(serialize_data(obs)))
                                json_to_send = get_action_json(command, env, None, 0., False, info)
                                data.outb = str.encode(json.dumps(json_to_send) + "\n")
                    else:
                        print('closing connection to', data.addr)
                        sel.unregister(sock)
                        sock.close()
                if mask & selectors.EVENT_WRITE:
                    if data.outb:
                        sent = sock.send(data.outb)  # Should be ready to write
                        data.outb = data.outb[sent:]
        if should_perform_action and curr_action is not None:
            print("Taking action: ", PlayerAction(curr_action).name)
            obs, reward, done, info, violations = env.step(curr_action)
            print("info: %s" % info)
            for key, mask, command in e:
                json_to_send = get_action_json(command, env, obs, reward, done, info)
                
                data = key.data
                #data.outb = str.encode(json.dumps(json_to_send) + "\n")

                # Serialize the data to ensure it's JSON-serializable
                json_to_send_serialized = serialize_data(json_to_send)   
                json_encoded = str.encode(json.dumps(json_to_send_serialized) + "\n")             
                print("sending JSON: ", json_encoded)
                data.outb = json_encoded
            env.render()
    sock_agent.close()
