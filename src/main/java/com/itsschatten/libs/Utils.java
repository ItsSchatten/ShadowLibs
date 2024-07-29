package com.itsschatten.libs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The utility class.
 */
@UtilityClass
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
     * The instance of the plugin allowing the use of things from the JavaPlugin class.
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
    @Contract("_ -> new")
    public static @NotNull NamespacedKey getKey(String key) {
        return new NamespacedKey(getInstance(), key);
    }

    /**
     * Check if the server is running a minimum Minecraft version
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
    public static void sendTitle(@NotNull Player pl, String title, String subtitle) {
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
     * Send multiple messages to someone.
     *
     * @param toWhom   The person to send the message to.
     * @param messages Send multiple messages to a player, all separated by a comma (,).
     */
    public static void tell(CommandSender toWhom, String @NotNull ... messages) {
        for (final String message : messages)
            tell(toWhom, message);
    }

    /**
     * Tell a player a message.
     *
     * @param toWhom  The person to tell the message to.
     * @param message The message to send.
     */
    public static void tell(CommandSender toWhom, @NotNull String message) {
        if (!message.isEmpty())
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
    public static String colorize(final @NotNull String message) {

        if (message.contains("<#")) {
            final Matcher hexMatch = HEX_PATTERN.matcher(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message));
            final StringBuilder buffer = new StringBuilder();

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
            logError(e);
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
        return Objects.requireNonNullElse(nullable, def);
    }

    /**
     * @return Returns the instance's logger.
     */
    public static @NotNull Logger getLogger() {
        return getInstance().getLogger();
    }

    /**
     * Sends a {@link String message} (or multiple) to the console with the INFO level.
     *
     * @param message  The first message that should be sent.
     * @param messages An array of messages that are then iterated through and sent to the console.
     */
    public static void log(@NotNull final String message, final String... messages) {
        if (instance == null) {
            throw new NullPointerException("Cannot log messages with a null plugin instance.");
        }

        getLogger().info(message);

        if (!message.isEmpty())
            for (final String msg : messages)
                getLogger().info(msg);


    }

    /**
     * Sends a {@link String message} (or multiple) to the console with the INFO level and an added [DEBUG].
     *
     * @param message  The first message that should be sent.
     * @param messages An array of messages that are then iterated through and sent to the console.
     */
    public static void debugLog(@NotNull final String message, final String... messages) {
        if (instance == null) {
            throw new NullPointerException("Cannot log messages with a null plugin instance.");
        }

        if (isDebugMode()) {
            getLogger().info("[DEBUG] " + message);

            if (!message.isEmpty())
                for (final String msg : messages)
                    getLogger().info("[DEBUG] " + msg);
        }
    }

    /**
     * Sends a {@link String message} (or multiple) to the console with the WARNING level.
     *
     * @param message  The first message that should be sent.
     * @param messages An array of messages that are then iterated through and sent to the console.
     */
    public static void logWarning(@NotNull String message, String... messages) {
        if (instance == null) {
            throw new NullPointerException("Cannot log messages with a null plugin instance.");
        }

        getLogger().warning(message);

        if (!message.isEmpty())
            for (final String msg : messages)
                getLogger().warning(msg);
    }

    /**
     * Sends a {@link String message} (or multiple) to the console with the ERROR level.
     *
     * @param message  The first message that should be sent.
     * @param messages An array of messages that are then iterated through and sent to the console.
     */
    public static void logError(@NotNull String message, String... messages) {
        if (instance == null) {
            throw new NullPointerException("Cannot log messages with a null plugin instance.");
        }

        getLogger().severe(message);

        if (!message.isEmpty())
            for (final String msg : messages)
                getLogger().severe(msg);
    }

    /**
     * Quickly log a {@link Throwable} error to console.
     *
     * @param error The error we wish to send to console.
     */
    public static void logError(@NotNull Throwable error) {
        logError("---------------- [ ERROR LOG START ] ----------------");
        logError("ERROR TYPE: " + error);
        logError("CAUSE: " + (error.getCause() == null ? "N/A" : error.getCause().getMessage()));
        logError("MESSAGE: " + (error.getMessage() == null ? "" : error.getMessage()));
        for (final StackTraceElement elm : error.getStackTrace()) {
            logError(elm.toString());
        }
        logError("----------------- [ ERROR LOG END ] -----------------");
    }


    /**
     * Serialize a single {@link ItemStack} into base64.
     *
     * @param item The item to serialize.
     * @return A nullable string of base64.
     * @see #deserialize(String)
     * @see #deserializeArray(String)
     * @see #serializeArray(ItemStack[])
     */
    public static String serialize(final ItemStack item) {
        return serializeArray(new ItemStack[]{item});
    }

    /**
     * Deserializes base64 data into a single item stack.
     *
     * @param data The data to deserialize.
     * @return A nonnull {@link ItemStack}
     * @see #serialize(ItemStack)
     * @see #serializeArray(ItemStack[])
     * @see #deserializeArray(String)
     */
    public static ItemStack deserialize(final String data) {
        return Objects.requireNonNull(deserializeArray(data))[0];
    }

    /**
     * Serialize an {@link ItemStack} array into {@link java.util.Base64}
     *
     * @param item The array to serialize.
     * @return Returns a possibly null string that should be base64 encoded.
     * @see #deserialize(String)
     * @see #deserializeArray(String)
     * @see #serialize(ItemStack)
     */
    public static @Nullable String serializeArray(ItemStack[] item) {
        try {
            final ByteArrayOutputStream finalOutputStream = new ByteArrayOutputStream();
            final ByteArrayOutputStream tempOutputStream = new ByteArrayOutputStream();
            final BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(tempOutputStream);
            int failedItems = 0;

            dataOutput.writeInt(item.length);

            for (final ItemStack itemStack : item) {
                try {
                    dataOutput.writeObject(itemStack);
                } catch (final Exception ex) {
                    failedItems++;
                    tempOutputStream.reset();
                } finally {
                    if (tempOutputStream.size() == 0) {
                        dataOutput.writeObject(null);
                    }
                    finalOutputStream.write(tempOutputStream.toByteArray());
                    tempOutputStream.reset();
                }
            }

            if (failedItems > 0) {
                Utils.logError("Failed to serialize " + failedItems + " invalid items");
            }

            dataOutput.close();
            return Base64Coder.encodeLines(finalOutputStream.toByteArray());
        } catch (EOFException ignored) { // Fail gracefully.
            Utils.debugLog("EOF exception generated!");
        } catch (Exception ex) {
            logError(ex);
        }
        return null;
    }

    /**
     * Deserialize a base64 encoded string to an {@link ItemStack} array.
     *
     * @param data The data to use to deserialize.
     * @return A possible nullable ItemStack array.
     * @see #deserialize(String)
     * @see #serializeArray(ItemStack[])
     * @see #serialize(ItemStack)
     */
    public static ItemStack @Nullable [] deserializeArray(String data) {
        try {
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            final BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            final ItemStack[] output = new ItemStack[dataInput.readInt()];
            for (int i = 0; i < output.length; i++) {
                output[i] = (ItemStack) dataInput.readObject();
            }
            dataInput.close();
            return output;
        } catch (IOException | ClassNotFoundException e) {
            logError(e);
        }

        return null;
    }

}