package com.victorgponce.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Simple CLI arguments parser for the launcher.
 */
public class CLIArgsParser {

    public static class CLIOptions {
        public String gameVersion = "1.20.1";
        public String fabricVersion = "0.14.21";
        public String modsFile = null;
        public int maxRam = 2048;
        public Path launcherDir = Paths.get(System.getProperty("user.dir"), "Minecraft-CLI-Launcher");
        public boolean offlineMode = false;
        public String offlineUsername = null; // NEW
    }

    public static CLIOptions parseArgs(String[] args) {
        CLIOptions options = new CLIOptions();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--game-version":
                    options.gameVersion = args[++i];
                    break;
                case "--fabric-version":
                    options.fabricVersion = args[++i];
                    break;
                case "--mods":
                    options.modsFile = args[++i];
                    break;
                case "--max-ram":
                    options.maxRam = Integer.parseInt(args[++i]);
                    break;
                case "--launcher-dir":
                    options.launcherDir = Paths.get(args[++i]);
                    break;
                case "--offline":
                    options.offlineMode = true;
                    // Check if next argument exists and is not a flag
                    if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                        options.offlineUsername = args[++i];
                    }
                    break;
                case "--help":
                    printHelp();
                    System.exit(0);
                    break;
                default:
                    System.err.println("Unknown argument: " + args[i]);
                    printHelp();
                    System.exit(1);
            }
        }
        return options;
    }

    private static void printHelp() {
        System.out.println("""
                Usage: java -jar MinecraftLauncherCLI.jar [options]
                Options:
                  --game-version <version>     Minecraft version (default: 1.20.1)
                  --fabric-version <version>   Fabric loader version (default: 0.14.21)
                  --mods <file>                Path to mods JSON/TXT file
                  --max-ram <MB>               Max RAM for JVM (default: 2048)
                  --launcher-dir <dir>         Custom launcher working directory
                  --offline [username]         Force offline mode (skip Microsoft login). If username is provided, uses it directly.
                  --help                       Show this help message
                """);
    }
}