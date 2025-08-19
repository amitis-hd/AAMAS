# CS-150 Final Project

Repository: https://github.com/turnerhayes/cs150-final

## Installing Python prerequisites

The requirements for the simulator are listed in requirements.txt. Install them using `pip install -r requirements.txt` or your favorite package manager. If you're using anaconda, you may need to add the conda-forge channel with `conda config --append channels conda-forge`.

## Simulator

To run the simulator, run `python sock_env.py`. To enable support for keyboard interaction, pass the `--keyboard_input` flag.

## DIARC

To run the DIARC config, run `./gradlew launch -Pmain=edu.tufts.hrilab.config.boxbot.BoxBotConfig`. You must make sure the simulator is running before you launch the config, or the config launch will fail.

## Web GUI

To run the DIARC component with simulator integration and the web UI to trigger actions:
1. Run the simulator socket environment from the root directory, `python sock_env.py`
2. In a separate terminal, run `./gradlew launch -Pmain=edu.tufts.hrilab.config.boxbot.BoxBotConfig --args="-g"` from the diarc/ directory
3. In a third terminal, run `./gradlew launchGui` from the diarc/ directory
4. Go to the URL output in step 3 (probably will be http://localhost:3000/)
5. On the web UI, go to the Goal Manager tab
6. Choose the action you want to execute (e.g. `moveWest()`, `moveEast()`, etc.)
7. In the Submit Action tab, enter `self` for the `actor` argument to the action, then click the Submit button
8. The simulator that should have appeared as a result of step 1 should show the selected action being executed (if possible)

## Changed Files

- [diarc/config/src/main/java/edu/tufts/hrilab/config/boxbot/BoxBotConfig.java](diarc/config/src/main/java/edu/tufts/hrilab/config/boxbot/BoxBotConfig.java)
    - Authors:
        - Turner Hayes
- [diarc/core/src/main/java/edu/tufts/hrilab/interfaces/BoxBotSimulatorInterface.java](diarc/core/src/main/java/edu/tufts/hrilab/interfaces/BoxBotSimulatorInterface.java)
    - Authors:
        - Turner Hayes
        - Ray Xu
- [diarc/core/src/main/java/edu/tufts/hrilab/boxbot/BoxBotComponent.java](diarc/core/src/main/java/edu/tufts/hrilab/boxbot/BoxBotComponent.java)
    - Authors:
        - Turner Hayes
        - Ray Xu
- [diarc/core/src/main/java/edu/tufts/hrilab/boxbot/BoxBotObservation.java](diarc/core/src/main/java/edu/tufts/hrilab/boxbot/BoxBotObservation.java)
    - Authors:
        - Turner Hayes
        - Ray Xu
- [diarc/core/src/main/java/edu/tufts/hrilab/boxbot/GamePlay.java](diarc/core/src/main/java/edu/tufts/hrilab/boxbot/GamePlay.java)
    - Authors:
        - Turner Hayes
- [diarc/core/src/main/java/edu/tufts/hrilab/boxbot/GameAction.java](diarc/core/src/main/java/edu/tufts/hrilab/boxbot/GameAction.java)
    - Authors:
        - Turner Hayes
- [diarc/core/src/main/java/edu/tufts/hrilab/boxbot/actions/*](diarc/core/src/main/java/edu/tufts/hrilab/boxbot/actions/)
    - Authors:
        - Turner Hayes
- [diarc/core/src/main/java/edu/tufts/hrilab/action/planner/Planner.java](diarc/core/src/main/java/edu/tufts/hrilab/action/planner/Planner.java)
    - Authors:
        - Turner Hayes
- [diarc/core/src/main/resources/config/edu/tufts/hrilab/action/asl/domains/boxbot.asl](diarc/core/src/main/resources/config/edu/tufts/hrilab/action/asl/domains/boxbot.asl)
    - Authors:
        - Turner Hayes
        - Ray Xu
- [diarc/core/src/main/resources/config/edu/tufts/hrilab/belief/agents/boxbot.pl](diarc/core/src/main/resources/config/edu/tufts/hrilab/belief/agents/boxbot.pl)
    - Authors:
        - Turner Hayes
        - Ray Xu
- All files in root directory
    - Authors:
        - Turner Hayes
