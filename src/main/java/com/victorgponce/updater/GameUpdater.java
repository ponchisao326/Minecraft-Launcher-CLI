package com.victorgponce.updater;

import com.victorgponce.config.LauncherConfig;
import fr.flowarg.flowlogger.ILogger;
import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.download.DownloadList;
import fr.flowarg.flowupdater.download.IProgressCallback;
import fr.flowarg.flowupdater.download.Step;
import fr.flowarg.flowupdater.download.json.CurseFileInfo;
import fr.flowarg.flowupdater.utils.ModFileDeleter;
import fr.flowarg.flowupdater.versions.VanillaVersion;
import fr.flowarg.flowupdater.versions.fabric.FabricVersion;
import fr.flowarg.flowupdater.versions.fabric.FabricVersionBuilder;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;

import java.io.FileReader;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Handles updating and launching the game via FlowUpdater/NoFramework.
 */
public class GameUpdater {
    private final LauncherConfig config;
    private final ILogger logger;

    public GameUpdater(LauncherConfig config, ILogger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void updateGameFiles() {
        IProgressCallback callback = new IProgressCallback() {
            private final DecimalFormat decimalFormat = new DecimalFormat("#.#");
            private String stepTxt = "";
            private String percentTxt = "0.0%";

            @Override
            public void step(Step step) {
                stepTxt = getStepDetails(step.name());
                System.out.println(String.format("%s (%s)", stepTxt, percentTxt));
            }

            @Override
            public void update(DownloadList.DownloadInfo info) {
                percentTxt = decimalFormat.format(info.getDownloadedBytes() * 100.d / info.getTotalToDownloadBytes()) + "%";
                System.out.print(String.format("\r%s (%s)", stepTxt, percentTxt));
            }

            @Override
            public void onFileDownloaded(Path path) {
                String p = path.toString();
                System.out.println("\nDownloaded: " + p.replace(config.getLauncherDir().toFile().getAbsolutePath(), ""));
            }
        };

        try {
            VanillaVersion vanillaVersion = new VanillaVersion.VanillaVersionBuilder()
                    .withName(config.getGameVersion())
                    .build();

            List<CurseFileInfo> curseMods = loadModsFromFile(config.getModsFile());

            FabricVersion fabric = new FabricVersionBuilder()
                    .withFabricVersion(config.getFabricVersion())
                    .withCurseMods(curseMods)
                    .withFileDeleter(new ModFileDeleter(true))
                    .build();

            FlowUpdater updater = new FlowUpdater.FlowUpdaterBuilder()
                    .withVanillaVersion(vanillaVersion)
                    .withModLoaderVersion(fabric)
                    .withLogger(logger)
                    .withProgressCallback(callback)
                    .build();

            updater.update(config.getLauncherDir());
            System.out.println("\n✓ Game files updated successfully");
        } catch (Exception e) {
            logger.printStackTrace(e);
            System.err.println("Failed to update game files: " + e.getMessage());
        }
    }

    /**
     * Launches the game using NoFramework with the provided AuthInfos.
     * @param authInfos The authentication information for the user.
     */
    public void launchGame(AuthInfos authInfos) {
        try {
            System.out.println("Starting Minecraft...");

            NoFramework noFramework = new NoFramework(
                    config.getLauncherDir(),
                    authInfos,
                    GameFolder.FLOW_UPDATER
            );

            noFramework.getAdditionalVmArgs().add("-Xmx" + config.getMaxRam() + "M");

            Process gameProcess = noFramework.launch(
                    config.getGameVersion(),
                    config.getFabricVersion(),
                    NoFramework.ModLoader.FABRIC
            );

            System.out.println("✓ Game launched successfully");
            System.out.println("Waiting for game to close...");
            int exitCode = gameProcess.waitFor();
            System.out.println("Game closed with exit code: " + exitCode);

        } catch (Exception e) {
            logger.printStackTrace(e);
            System.err.println("Failed to launch game: " + e.getMessage());
        }
    }

    /**
     * Maps step names to user-friendly descriptions.
     * @param stepName The name of the step.
     * @return A user-friendly description of the step.
     */
    private String getStepDetails(String stepName) {
        return switch (stepName) {
            case "READ" -> "Reading the JSON file...";
            case "DL_LIBS" -> "Downloading libraries...";
            case "DL_ASSETS" -> "Downloading resources...";
            case "EXTRACT_NATIVES" -> "Extracting native files...";
            case "FORGE" -> "Installing Forge...";
            case "MODS" -> "Downloading mods...";
            case "EXTERNAL_FILES" -> "Downloading external files...";
            case "POST_EXECUTIONS" -> "Running post-installation tasks...";
            case "MOD_LOADER" -> "Installing mod loader...";
            case "INTEGRATION" -> "Integrating mods...";
            case "END" -> "Done!";
            default -> "Processing...";
        };
    }

    /**
     * Loads CurseFileInfo objects from a mods file (JSON).
     * Each entry in the JSON array must have "projectId" and "fileId".
     * @param modsFilePath The path to the mods file.
     * @return A list of CurseFileInfo objects.
     */
    private List<CurseFileInfo> loadModsFromFile(String modsFilePath) {
        List<CurseFileInfo> mods = new ArrayList<>();
        if (modsFilePath == null) return mods;
        try (FileReader reader = new FileReader(modsFilePath)) {
            Gson gson = new Gson();
            List<SimpleCurseMod> simpleMods = gson.fromJson(reader, new TypeToken<List<SimpleCurseMod>>() {}.getType());
            if (simpleMods != null) {
                for (SimpleCurseMod mod : simpleMods) {
                    mods.add(new CurseFileInfo(mod.projectId, mod.fileId));
                }
            }
        } catch (Exception e) {
            logger.err("Could not read mods file: " + e.getMessage());
            System.err.println("Could not load mods file (" + modsFilePath + "): " + e.getMessage());
        }
        return mods;
    }

    /**
     * Helper class for JSON deserialization.
     */
    private static class SimpleCurseMod {
        int projectId;
        int fileId;
    }
}