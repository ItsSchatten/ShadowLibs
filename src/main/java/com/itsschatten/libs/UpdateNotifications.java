package com.itsschatten.libs;

import lombok.Getter;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public abstract class UpdateNotifications extends BukkitRunnable {

    @Getter
    private static int projectId = 0;

    @Getter
    private static String latestVersion = "";

    public UpdateNotifications(int projectId) {
        UpdateNotifications.projectId = projectId;
    }

    public static boolean isUpdateAvailable() {
        final String currentVersion = Utils.getInstance().getDescription().getVersion();

        final String currentIdentifier = currentVersion.contains("-") ? currentVersion.substring(currentVersion.indexOf("-")) : "";
        if (currentIdentifier.equalsIgnoreCase("dev")) {
            return false;
        }

        final String[] latest = latestVersion.split("\\.");
        final String[] current = currentVersion.split("\\.");

        final int major = NumberUtils.toInt(latest[0]), minor = NumberUtils.toInt(latest[1]), patch = NumberUtils.toInt(latest[2]);
        final int curMajor = NumberUtils.toInt(current[0]), curMinor = NumberUtils.toInt(current[1]), curPatch = NumberUtils.toInt(current[2]);

        if (major > curMajor) return true;
        if (minor > curMinor) return true;
        return patch > curPatch;
    }

    public static String getUpdateMessage() {
        return Utils.getUpdateAvailableMessage();
    }

    @Override
    public void run() {
        try {
            final URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + projectId);
            final URLConnection con = url.openConnection();

            try (BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                latestVersion = r.readLine();
            }

            Utils.log("&7Checking for update...");

            if (isUpdateAvailable())
                onUpdateAvailable();

        } catch (final IOException ex) {
            ex.printStackTrace();
            Utils.log("An error occurred while searching for an update! Are you offline?");
        }
    }

    public abstract void onUpdateAvailable();

}
