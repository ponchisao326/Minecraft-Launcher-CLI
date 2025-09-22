package com.victorgponce;

import com.victorgponce.auth.MicrosoftAuthManager;
import com.victorgponce.config.LauncherConfig;
import com.victorgponce.updater.GameUpdater;
import com.victorgponce.util.CLIArgsParser;
import fr.flowarg.flowlogger.ILogger;
import fr.flowarg.flowlogger.Logger;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;

import java.util.Optional;

/**
 * Main class for the Minecraft CLI Launcher.
 */
public class MinecraftLauncherCLI {

    private final LauncherConfig config;
    private final ILogger logger;
    private final MicrosoftAuthManager authManager;
    private final GameUpdater updater;
    private AuthInfos authInfos;

    public MinecraftLauncherCLI(CLIArgsParser.CLIOptions options) {
        this.logger = new Logger("[MinecraftCLI]", options.launcherDir.resolve("launcher.log"));
        this.config = new LauncherConfig(options, this.logger);
        this.authManager = new MicrosoftAuthManager(config, logger);
        this.updater = new GameUpdater(config, logger);
    }

    public static void main(String[] args) {
        CLIArgsParser.CLIOptions options = CLIArgsParser.parseArgs(args);

        MinecraftLauncherCLI launcher = new MinecraftLauncherCLI(options);

        System.out.println("=== Minecraft CLI Launcher ===");

        // Authentication
        Optional<AuthInfos> auth = launcher.authManager.authenticate(options.offlineMode);
        if (auth.isEmpty()) {
            System.err.println("Authentication failed. Exiting.");
            System.exit(1);
        }
        launcher.authInfos = auth.get();

        System.out.println("User: " + launcher.authInfos.getUsername());

        // Update game files
        System.out.println("\nUpdating game files...");
        launcher.updater.updateGameFiles();

        // Launch game
        System.out.println("\nLaunching game...");
        launcher.updater.launchGame(launcher.authInfos);
    }
}