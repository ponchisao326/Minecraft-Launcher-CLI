package com.victorgponce.config;

import com.victorgponce.util.CLIArgsParser;
import fr.flowarg.flowlogger.ILogger;
import fr.theshark34.openlauncherlib.util.Saver;

import java.nio.file.Path;

/**
 * Handles configuration loading and saving.
 */
public class LauncherConfig {
    private final CLIArgsParser.CLIOptions cliOptions;
    private final Saver saver;

    public LauncherConfig(CLIArgsParser.CLIOptions cliOptions, ILogger logger) {
        this.cliOptions = cliOptions;
        Path configFile = cliOptions.launcherDir.resolve("config.properties");
        this.saver = new Saver(configFile);
        this.saver.load();
    }

    public String getGameVersion() {
        return cliOptions.gameVersion;
    }

    public String getFabricVersion() {
        return cliOptions.fabricVersion;
    }

    public String getModsFile() {
        return cliOptions.modsFile;
    }

    public int getMaxRam() {
        if (saver.get("maxRam") != null) {
            try {
                return Integer.parseInt(saver.get("maxRam"));
            } catch (NumberFormatException ignore) {}
        }
        return cliOptions.maxRam;
    }

    public Saver getSaver() {
        return saver;
    }

    public Path getLauncherDir() {
        return cliOptions.launcherDir;
    }
}