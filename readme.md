

## Simulator

To run the simulator, run `python sock_env.py`. To enable support for keyboard interaction, pass the `--keyboard_input` flag.

## DIARC

To run the DIARC config, run `./gradlew launch -Pmain=edu.tufts.hrilab.config.boxbot.BoxBotConfig`. You must make sure the simulator is running before you launch the config, or the config launch will fail.

## Web GUI

To run the DIARC component with simulator integration and the web UI to trigger actions:
1. Run the simulator socket environment from the root directory, `python sock_env.py`
2. In a separate terminal, run `./gradlew launch -Pmain=edu.tufts.hrilab.config.boxbot.BoxBotConfig --args="-g"` from the diarc/ directory
3. In a third terminal, run `./gradlew launchGui` from the diarc/ directory
