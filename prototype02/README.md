# Prototype 01 — libGDX Interaction Test

This prototype checks whether libGDX offers a better foundation for TableverseRPG's game-world display, mouse controls, and on-screen information than the approach used in Open Fields 2. It is an interaction experiment, not a rewrite of the RPG.

## What it does

- A single fixed-size room viewed from above, with a walkable grid and a few blocking obstacles.
- Two independently moving characters, rendered as simple labeled shapes.
- Left-click a character to select it; the selected character gets a visible outline and a status panel shows its name, position, and current order.
- Right-click a reachable point to order the selected character to move there, using grid-based, obstacle-aware pathfinding.
- A pause/resume control that freezes the simulation while leaving the interface responsive.
- The simulation model (`core`) has no libGDX imports; UI input is translated into explicit select/order/pause actions, and rendering reads from that state.

See `doc/planning/ideas/TableverseRPG Prototype 01 Design.md` for the full design brief.

## Project layout

- `core` — the libGDX-independent simulation, plus the view/HUD/input code that wires it to libGDX.
- `lwjgl3` — the desktop (LWJGL3) launcher used to run the prototype.
- `assets` — runtime assets shared by the application.

## Running it

The build targets Java 17 (`sourceCompatibility`/`targetCompatibility` = 17), but Gradle just needs a JDK it can run on to build and launch it — JDK 17 through 21 all work. Gradle 8.10 (the wrapper version used here) cannot run on JDK 24+; using one fails with an error like `Unsupported class file major version 68`.

Check what Gradle will use by default:

```
echo $env:JAVA_HOME
```

If that's unset, Gradle falls back to whatever `java` resolves to on `PATH`. If it points at an unsupported JDK (e.g. 24), tell Gradle to use a different install instead of changing `JAVA_HOME` system-wide. **Use a real path to a JDK you actually have installed — Gradle silently falls back to the default JDK if the path doesn't exist, so a typo or a placeholder like `/path/to/jdk-17` won't produce an error pointing at the actual problem.**

Find the JDKs installed on your machine first (e.g. on Windows, check `C:\Program Files\Java`, `C:\Program Files\Eclipse Adoptium`, or wherever your installer put them; on macOS/Linux, `/usr/lib/jvm` or `usr/libexec/java_home -V`). The paths below (`C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot`, `/usr/lib/jvm/temurin-21`) are examples from one machine, not generic locations — substitute the actual path to a JDK 17–21 install on yours.

For a single run, override `JAVA_HOME` for just that command. `gradlew`/`gradlew.bat` always launch their own bootstrap JVM using `JAVA_HOME` (see the script itself), before Gradle ever reads `-Dorg.gradle.java.home` — so passing `-Dorg.gradle.java.home` alone does **not** help if `JAVA_HOME` already points at an unsupported JDK; it never gets that far:

PowerShell:

```
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"; .\gradlew.bat lwjgl3:run
```

macOS/Linux:

```
JAVA_HOME=/usr/lib/jvm/temurin-21 ./gradlew lwjgl3:run
```

(`$env:JAVA_HOME = ...` in PowerShell only lasts for the current session, not just the one command — open a new terminal, or set it back, to undo it. The `VAR=value command` form on macOS/Linux applies only to that one command.)

To make it permanent for this project instead, add `org.gradle.java.home` to `gradle.properties` (in this directory) — that setting *does* take effect once Gradle is running, since by then it's past the bootstrap step — using your own JDK's path:

```
org.gradle.java.home=C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.7.6-hotspot
```

`gradle.properties` is tracked in this repo, so if the path is specific to your machine, set it instead in `$GRADLE_USER_HOME/gradle.properties` (typically `~/.gradle/gradle.properties`) so it isn't committed.

From this directory (`prototype01`):

Windows (PowerShell):

```
.\gradlew.bat lwjgl3:run
```

macOS/Linux:

```
./gradlew lwjgl3:run
```

Note the `.\` before `gradlew.bat` — PowerShell doesn't run scripts from the current directory unless told to, and will fail with `CommandNotFoundException` without it. If you're using `cmd.exe` instead, `gradlew.bat` works either way.

## Testing

```
.\gradlew.bat test
```
