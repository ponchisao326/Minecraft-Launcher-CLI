package com.victorgponce.auth;

import com.victorgponce.config.LauncherConfig;
import fr.flowarg.flowlogger.ILogger;
import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles authentication logic and token persistence.
 */
public class MicrosoftAuthManager {
    private final LauncherConfig config;
    private final ILogger logger;

    public MicrosoftAuthManager(LauncherConfig config, ILogger logger) {
        this.config = config;
        this.logger = logger;
    }

    /**
     * Authenticate the user, either via Microsoft or offline mode.
     * @param forceOffline If true, forces offline mode without attempting Microsoft authentication.
     * @return AuthInfos if authentication was successful, empty Optional otherwise.
     */
    public Optional<AuthInfos> authenticate(boolean forceOffline) {
        var saver = config.getSaver();

        if (forceOffline) {
            // Always use offline, skip Microsoft auth
            System.out.print("Enter username for offline mode: ");
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String username = reader.readLine();
                AuthInfos info = new AuthInfos(username, UUID.randomUUID().toString(), UUID.randomUUID().toString());
                saver.set("offline-username", username);
                saver.save();
                System.out.println("✓ Using offline mode for user: " + username);
                return Optional.of(info);
            } catch (IOException ioException) {
                System.err.println("Failed to read username. Exiting...");
                logger.err("Failed to read username for offline mode: " + ioException.getMessage());
                return Optional.empty();
            }
        }

        // Normal logic (refresh token, fallback to offline, etc)
        if (saver.get("msAccessToken") != null && saver.get("msRefreshToken") != null) {
            try {
                MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
                MicrosoftAuthResult result = authenticator.loginWithRefreshToken(saver.get("msRefreshToken"));
                saver.set("msAccessToken", result.getAccessToken());
                saver.set("msRefreshToken", result.getRefreshToken());
                saver.save();
                AuthInfos info = new AuthInfos(
                        result.getProfile().getName(),
                        result.getAccessToken(),
                        result.getProfile().getId()
                );
                System.out.println("✓ Session refreshed for user: " + info.getUsername());
                logger.info("Microsoft session refreshed for user: " + info.getUsername());
                return Optional.of(info);
            } catch (MicrosoftAuthenticationException e) {
                logger.err("Failed to refresh Microsoft token: " + e.getMessage());
                saver.remove("msAccessToken");
                saver.remove("msRefreshToken");
                saver.save();
            }
        }

        // Try offline mode
        if (saver.get("offline-username") != null) {
            AuthInfos info = new AuthInfos(
                    saver.get("offline-username"),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString()
            );
            System.out.println("✓ Using offline mode for user: " + info.getUsername());
            return Optional.of(info);
        }

        // Interactive Microsoft authentication
        try {
            MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
            System.out.println("Opening Microsoft authentication in browser...");
            MicrosoftAuthResult result = authenticator.loginWithAsyncWebview().get();
            saver.set("msAccessToken", result.getAccessToken());
            saver.set("msRefreshToken", result.getRefreshToken());
            saver.save();
            AuthInfos info = new AuthInfos(
                    result.getProfile().getName(),
                    result.getAccessToken(),
                    result.getProfile().getId()
            );
            logger.info("Microsoft authentication successful for user: " + info.getUsername());
            System.out.println("✓ Authentication successful! Welcome " + info.getUsername());
            return Optional.of(info);
        } catch (Exception e) {
            logger.err("Microsoft authentication failed: " + e.getMessage());
            System.err.println("Authentication failed: " + e.getMessage());
            System.err.println("Falling back to offline mode...");
            System.out.print("Enter username for offline mode: ");
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String username = reader.readLine();
                AuthInfos info = new AuthInfos(username, UUID.randomUUID().toString(), UUID.randomUUID().toString());
                saver.set("offline-username", username);
                saver.save();
                System.out.println("✓ Using offline mode for user: " + username);
                return Optional.of(info);
            } catch (IOException ioException) {
                System.err.println("Failed to read username. Exiting...");
                logger.err("Failed to read username for offline mode: " + ioException.getMessage());
                return Optional.empty();
            }
        }
    }

    /**
     * Logs out the current user by clearing stored tokens and usernames.
     */
    public void logout() {
        var saver = config.getSaver();
        saver.remove("msAccessToken");
        saver.remove("msRefreshToken");
        saver.remove("offline-username");
        saver.save();
        logger.info("User logged out successfully");
        System.out.println("✓ Logged out successfully");
    }
}