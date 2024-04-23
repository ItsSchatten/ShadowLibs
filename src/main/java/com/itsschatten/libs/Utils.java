package com.itsschatten.libs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The utilities class.
 */
public class Utils {

    private static final Pattern HEX_PATTERN = Pattern.compile("<#(\\w{6})>");
    private static final String[] VERSION = Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.");

    /**
     * The prefix for the plugin.
     */
    @Setter(AccessLevel.PUBLIC)
    @Getter(AccessLevel.PUBLIC)
    private static String prefix = "";

    /**
     * The instance of the plugin that implements this library, allows the use of things from the JavaPlugin class.
     */
    @Setter(AccessLevel.PUBLIC)
    @Getter(AccessLevel.PUBLIC)
    private static JavaPlugin instance;

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PUBLIC)
    private static String noPermsMessage;

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PUBLIC)
    private static String updateAvailableMessage = "";

    @Setter(AccessLevel.PUBLIC)
    @Getter(AccessLevel.PUBLIC)
    private static boolean debugMode;

    /**
     * Get a NamespacedKey associated with the instance supplied to this utility.
     *
     * @param key The key to create.
     * @return a new {@link NamespacedKey}
     */
    public static NamespacedKey getKey(String key) {
        return new NamespacedKey(getInstance(), key);
    }

    /**
     * Check if server is running a minimum Minecraft version
     *
     * @param major Major version to check (Most likely just going to be 1)
     * @param minor Minor version to check
     * @return True if running this version or higher
     */
    public static boolean isRunningMinecraft(int major, int minor) {
        return isRunningMinecraft(major, minor, 0);
    }

    /**
     * Check if server is running a minimum Minecraft version
     *
     * @param major    Major version to check (Most likely just going to be 1)
     * @param minor    Minor version to check
     * @param revision Revision to check
     * @return True if running this version or higher
     */
    public static boolean isRunningMinecraft(int major, int minor, int revision) {
        int maj = Integer.parseInt(VERSION[0]);
        int min = Integer.parseInt(VERSION[1]);
        int rev;
        try {
            rev = Integer.parseInt(VERSION[2]);
        } catch (Exception ignore) {
            rev = 0;
        }
        return maj >= major && min >= minor && rev >= revision;
    }

    /**
     * Check if a class exists
     *
     * @param className The {@link Class#getCanonicalName() canonical name} of the class
     * @return True if the class exists
     */
    public static boolean classExists(final String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    /**
     * Sends a title with a subtitle to a player.
     *
     * @param pl       The player to send the title and subtitle to.
     * @param title    The message for the title. (Or the bigger message)
     * @param subtitle The message for the subtitle (Or the smaller message)/
     */
    public static void sendTitle(Player pl, String title, String subtitle) {
        pl.sendTitle(colorize(title), colorize(subtitle), 20, 3 * 20, 10);
    }

    /**
     * Sends an actionbar to a player.
     *
     * @param pl    The player to send the bar to.
     * @param title The message for the bar.
     */
    public static void sendBar(Player pl, String title) {
        try {
            pl.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(colorize(title)));
        } catch (final Throwable t) {
            tell(pl, title);
        }
    }

    /**
     * Log multiple messages to console.
     *
     * @param messages The messages to log.
     */
    public static void log(String... messages) {
        for (final String message : messages)
            log(message);
    }

    /**
     * Log a message to console.
     *
     * @param message The messages to log.
     */
    public static void log(String message) {
        tell(Bukkit.getConsoleSender(), "[" + instance.getName() + "] " + message);
    }

    /**
     * Logs a debug message to console.
     *
     * @param enabled Is debugging enabled?
     * @param message The message to be sent.
     */
    private static void debugLog(boolean enabled, String message) {
        if (enabled) {
            log("[DEBUG] " + message);
        }
    }

    /**
     * Logs a debug message to console.
     *
     * @param message The message to be sent to console.
     */
    public static void debugLog(String message) {
        debugLog(isDebugMode(), message);
    }

    /**
     * Logs debug messages to console.
     *
     * @param messages The messages to be logged in console.
     */
    public static void debugLog(String... messages) {
        for (String message : messages)
            debugLog(message);
    }

    /**
     * Send multiple messages to someone.
     *
     * @param toWhom   The person to send the message to.
     * @param messages Send multiple message to a player, all separated by a comma (,).
     */
    public static void tell(CommandSender toWhom, String... messages) {
        for (final String message : messages)
            tell(toWhom, message);
    }

    /**
     * Tell a player a message.
     *
     * @param toWhom  The person to tell the message to.
     * @param message The message to send.
     */
    public static void tell(CommandSender toWhom, String message) {
        if (!message.equals(""))
            toWhom.sendMessage(colorize(message.replace("{prefix}", getPrefix())));
    }

    /**
     * <p>Iterates through a message replacing any RGB (hex) color first.
     * <p>
     * Then uses ampersand and a number 0-9 or a letter a-f to color a message.
     * Use l, m, n, o to add modifiers to the message.
     * </p>
     *
     * @param message The message to colorize.
     * @return The colorized message.
     */
    public static String colorize(final String message) {

        if (message.contains("<#")) {
            final Matcher hexMatch = HEX_PATTERN.matcher(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message));
            final StringBuffer buffer = new StringBuffer();

            while (hexMatch.find()) {
                hexMatch.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + hexMatch.group(1)).toString());
            }

            return hexMatch.appendTail(buffer).toString();
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Registers a command for a plugin, doesn't use the plugin.yml.
     *
     * @param command The command to register.
     */
    public static void registerCommand(Command command) {
        try {
            final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            final CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            commandMap.register(instance.getName(), command);

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if something is valid, if not get a default value.
     *
     * @param nullable The thing that could be null.
     * @param def      The default thing.
     * @param <T>      The type we should get or default for.
     * @return The object if not null, otherwise returns a default.
     */
    public static <T> T getOrDefault(T nullable, T def) {
        return nullable != null ? nullable : def;
    }

    /**
     * @param delay The delay.
     * @param task  The task.
     */
    public static void runLater(int delay, BukkitRunnable task) {
        runLater(delay, task);
    }

    /**
     * @param delay The delay
     * @param task  The task
     */
    public static void runLater(int delay, Runnable task) {
        Bukkit.getScheduler().runTaskLater(instance, task, delay);
    }


}