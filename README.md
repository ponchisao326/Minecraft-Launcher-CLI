# MinecraftLauncherCLI

A modern, modular, and user-friendly Minecraft CLI launcher with Microsoft authentication and mod support.

## Features

- **Microsoft Account Authentication** (fallback to offline mode)
- **Automatic updates** for Minecraft, Fabric Loader, and mods via [FlowUpdater](https://github.com/FlowArg/FlowUpdater)
- **Mod installation** using a simple JSON list of CurseForge mods
- **Customizable** via command line: game version, RAM, working directory, etc.
- **Offline mode** (`--offline` flag) for playing without Microsoft authentication
- **Progress reporting** and detailed logging

---

## Usage

### Running the Launcher

```sh
java -jar MinecraftLauncherCLI.jar [options]
```

### Options

| Argument                | Description                                      | Default Value                | Example                           |
|-------------------------|--------------------------------------------------|------------------------------|------------------------------------|
| `--game-version`        | Minecraft version                                | `1.20.1`                     | `--game-version 1.20.2`            |
| `--fabric-version`      | Fabric loader version                            | `0.14.21`                    | `--fabric-version 0.15.0`          |
| `--mods`                | Path to mods file (JSON)                         | _(none)_                     | `--mods mods.json`                 |
| `--max-ram`             | Maximum RAM (MB) for JVM                         | `2048`                       | `--max-ram 4096`                   |
| `--launcher-dir`        | Launcher working directory                       | `Minecraft-CLI-Launcher`     | `--launcher-dir /path/to/launcher` |
| `--offline`             | Force offline mode (skip Microsoft login)        | _(disabled)_                 | `--offline`                        |
| `--help`                | Show help message                                |                              | `--help`                           |

You can combine options as needed.  
Examples:

```sh
java -jar MinecraftLauncherCLI.jar --offline --mods mods.json
java -jar MinecraftLauncherCLI.jar --game-version 1.20.2 --mods mods.json --max-ram 4096
```

---

## Mods File Format

The launcher supports a simple JSON file for specifying mods to be installed from CurseForge.

#### Example: `mods.json`

```json
[
  {
    "projectId": 123456,
    "fileId": 789012
  },
  {
    "projectId": 234567,
    "fileId": 890123
  }
]
```

- Each object must include the `projectId` and `fileId` from CurseForge.

---

## Authentication

- By default, Microsoft authentication is used.
- Use `--offline` to **force offline mode**.  
  You will be prompted for a username, and no login or token is required.

---

## Default Behavior

If you run the launcher **without arguments**:

- **Minecraft version:** `1.20.1`
- **Fabric version:** `0.14.21`
- **Mods:** none (vanilla/fabric only)
- **RAM:** 2048 MB
- **Launcher directory:** `Minecraft-CLI-Launcher` in the current directory
- **Offline mode:** Disabled (auth is required unless `--offline` is set)

---

## Directory Structure

```
com/victorgponce/
├── MinecraftLauncherCLI.java
├── auth/
│   └── MicrosoftAuthManager.java
├── config/
│   └── LauncherConfig.java
├── updater/
│   └── GameUpdater.java
└── util/
    └── CLIArgsParser.java
```

---

## Building

Make sure you have all the dependencies:

- [OpenLauncherLib](https://github.com/FlowArg/OpenLauncherLib)
- [FlowLogger](https://github.com/FlowArg/FlowLogger)
- [FlowUpdater](https://github.com/FlowArg/FlowUpdater)
- [OpenAuth (Microsoft)](https://github.com/Litarvan/OpenAuth)
- [Gson](https://github.com/google/gson)

---

## Logging & Config

- Logs are saved in the launcher directory as `launcher.log`
- Configuration is persisted in `config.properties` in the launcher directory

---

## FAQ

**Q:** _How do I get CurseForge `projectId` and `fileId`?_

A: Visit the mod’s CurseForge page. The `projectId` is in the URL (e.g. `curseforge.com/minecraft/mc-mods/jei/123456`). The `fileId` is shown on the download button (hover or inspect).

**Q:** _Can I use Forge or other loaders?_

A: Currently, this launcher is tailored for Fabric, but modularity allows for support extensions.

---

## License

MIT