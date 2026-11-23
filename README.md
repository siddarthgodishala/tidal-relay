# Tidal Relay

Siddarth Godishala · ISYS 34603 Android final project

Keep the harbor in contact during a night watch. A lighthouse beam sweeps across the compass, and each radio channel has a different reception window. Tap **Send signal** or the chart when the beam crosses the teal arc. You get one shot per channel. Catch at least six of eight signals to clear the watch.

A shot within seven degrees of the center earns 150 points. A shot anywhere else in the reception window earns 100 points. A miss earns zero. The sweep gets faster and the windows narrow over the watch. The maximum score is 1,200. The station keeps the best cleared standard watch on the device.

## Run the game

Open the repository root in Android Studio. Use the bundled JDK, allow Gradle sync, and select an emulator or Android device running API 26 or newer. The project uses Android SDK 35, Android Gradle Plugin 9.4.0, and Gradle 9.6.0. From the project folder, `sh gradlew assembleDebug` builds the app on macOS or Linux. On Windows, run `gradlew.bat assembleDebug`.

1. Enter a call sign and tap **Begin watch**.
2. Send a signal, read the result, then tap **Tune next channel**.
3. After the eighth attempt, open the **Watch log** for the outcome, score, and channel results.
4. Use **Play again** for a new watch or **Share score** to open Android’s share sheet.

## Rubric walkthrough

| Challenge | Where to see it |
| --- | --- |
| 1. Restart | **Play again** on the watch log. **Restart watch** in the app-bar menu also restarts mid-game. |
| 2. Rotation | Turn the device during a watch or between signals. The score, current channel, results, and sweep phase are restored. |
| 3. Second screen with data | The watch log receives the call sign, score data, and record. Play again returns a result to the station. |
| 4. Array-backed display | Each transmission advances through eight channel names, bearings, sweep speeds, and window widths. |
| 5. Text entry | The call sign appears in the watch log and shared result. |
| 6. Easter egg | Enter **low tide** as the call sign for wider practice windows. Practice scores are excluded from the station record. |
| 7. App bar button | **Guide** opens the game rules from the station app bar. |
| 8. Implicit intent | **Share score** sends the watch summary through Android’s system chooser. |
| 12. Custom view | The chart draws the sweep, target, and harbor and handles taps as an alternate signal control. |
| 13. Picture from shapes | The custom chart draws the lighthouse, water, compass, and beam from paths, lines, arcs, and gradients. |
| 14–16. Animation | A signal triggers a lighthouse flash and expanding wave. One AnimatorSet plays them together and color reflects the result. |
| 17. App icon | The launcher shows the lighthouse and beam icon. |
| 18. Dynamic text | The current channel and signal, catch, and point counts update throughout the watch. |

`Watch.java` contains the rules and scoring. `SignalDial.java` draws and animates the chart. `RelayActivity.java` manages a watch and saves its state. `LogActivity.java` displays and shares the report.
