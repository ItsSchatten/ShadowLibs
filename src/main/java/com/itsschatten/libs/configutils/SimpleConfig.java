package com.itsschatten.libs.configutils;

import com.itsschatten.libs.Utils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SimpleConfig extends YamlConfiguration {

    /**
     * The file with the configuration on the disk, example: settings.yml
     */
    private final File file;

    /**
     * The default file. Could be null
     */
    private final YamlConfiguration defaults;

    /**
     * Optionally, you can set the header that will appear when the file
     * gets edited automatically.
     * <p>
     * Call {@link #setHeader(String[])} to use this.
     * -- GETTER --
     * Gets the header that is applied when the file is updated, or null if not set.
     *
     * @return the edit header
     */
    @Getter
    private String[] editHeader;

    /**
     * Path prefix is what appears automatically before the path, when getting keys from the file.
     * <p>
     * Example: if you have a PlayerCache class that will always refer to a certain player,
     * you can set his unique id as the pathPrefix to save you time and energy while getting his values,
     * so instead of typing getString(id + ".key") you only need to type getString("key")
     * <p>
     * Only works when the default file does not exist! (logically, in the example above you cannot create default
     * values for each player out there :))
     * -- SETTER --
     * Set the new default
     *
     * @param pathPrefix, the new path prefix, or use null to un-set
     */
    @Setter
    private String pathPrefix;

    /**
     * Makes a new SimpleConfig instance that will manage one configuration file.
     * <p>
     * NB: Make sure you create the file with the exact same name and all
     * the default values inside your plugin in the src/main/resources folder!
     *
     * @param fileName, the name of the configuration file, e.g. settings.yml
     */
    public SimpleConfig(String fileName) {
        this(fileName, true);
    }

    /**
     * Makes a new instance with an optional default file (see above).
     *
     * @param fileName     The name of the file.
     * @param useDefaults, require the default file? see commentaries to the above constructor
     */
    public SimpleConfig(String fileName, boolean useDefaults) {

        // First, set the defaults from which we update your config.
        // The defaults are in your src/main/resources folder in your FirstSpigotPlugin.
        if (useDefaults) {
            // Now we use the file in your plugin .jar as defaults for updating the file on the disk.
            this.defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(SimpleConfig.class.getResourceAsStream("/" + fileName), StandardCharsets.UTF_8));
            Objects.requireNonNull(defaults, "Could not get the default " + fileName + " inside of your plugin, make sure you created the file and that you did not replace the jar on a running server!");

        } else
            this.defaults = null;

        // Now copy the file from your plugin .jar to the disk (if it doesn't exist)
        this.file = extract(fileName);

        // Finally, load or update the configuration.
        loadConfig();
    }

    /**
     * Set what header will appear when the file is automatically updated.
     * <p>
     * Due to the way of how Bukkit stores .yml files, all your # comments are lost
     * when the file is updated except for this header. You can inform the users
     * where they can find the default files with documentation.
     *
     * @param editHeader, the edit header
     */
    public void setHeader(String[] editHeader) {
        this.editHeader = editHeader;
    }

    /**
     * Saves the file on the disk and loads it again.
     */
    public void reloadConfig() {
        saveConfig();
        loadConfig();
    }

    /**
     * Writes a key with a value to your file.
     * Example: write("weather.disable", true)
     *
     * @param path,  the path, use '.' to split sections
     * @param value, the value can be a primitive, a String, HashMap or a Collection (List, or a Set)
     */
    public void write(String path, Object value) {
        set(path, value);

        reloadConfig();
    }

    // Saves the file on the disk and copies the {@link #editHeader} if exists.
    public void saveConfig() {
        try {

            // Copy the header
            if (editHeader != null) {
                options().setHeader(Collections.singletonList(StringUtils.join(editHeader, System.lineSeparator())));
                options().copyHeader(true);
            }

            // Call parent method for saving
            super.save(file);

        } catch (final IOException ex) {
            Utils.logError(ex);
            Utils.logError("Failed to save configuration from '" + file + "'.");
        }
    }

    // Loads the configuration from the disk
    private void loadConfig() {
        try {

            // Call parent method for loading
            super.load(file);

        } catch (final Throwable t) {
            Utils.logError(t);
            Utils.logError("Failed to load configuration from " + file);
        }
    }


    /**
     * Gets an unspecified value from your file, so you must cast it to your desired value (example: (boolean) get("disable.this.feature", true))
     * The "def" is the default value, must be null since we use default values from your file in your .jar.
     */
    @Override
    public Object get(@NotNull String path, Object def) {
        if (defaults != null) {

            if (def != null && !def.getClass().isPrimitive() && !PrimitiveWrapper.isWrapperType(def.getClass()))
                throw new IllegalArgumentException("The default value must be null since we use defaults from file inside of the plugin! Path: " + path + ", default called: " + def);

            if (super.get(path, null) == null) {
                final Object defaultValue = defaults.get(path);
                Objects.requireNonNull(defaultValue, "Default " + file.getName() + " in your .jar lacks a key at '" + path + "' path");

                Utils.log("Updating " + file.getName() + ". Set '" + path + "' to '" + defaultValue + "'");
                write(path, defaultValue);
            }
        }

        // hacky workaround: prevent infinite loop due to how get works in the parent class
        final String m = new Throwable().getStackTrace()[1].getMethodName();

        // Add path prefix, but only when the default file doesn't exist
        if (defaults == null && pathPrefix != null && !m.equals("getConfigurationSection") && !m.equals("get"))
            path = pathPrefix + "." + path;

        return super.get(path, null);
    }

    @Override
    public void set(@NotNull String path, Object value) {
        // hacky workaround: prevent infinite loop due to how get works in the parent class
        final String m = new Throwable().getStackTrace()[1].getMethodName();

        // Add path prefix, but only when the default file doesn't exist
        if (defaults == null && pathPrefix != null && !m.equals("getConfigurationSection") && !m.equals("get"))
            path = pathPrefix + "." + path;

        super.set(path, value);
    }

    // Extract the file from your jar to the plugins/YourPlugin folder.
    // Does nothing if the file exists
    private @NotNull File extract(String path) {
        final JavaPlugin i = Utils.getInstance();
        final File file = new File(i.getDataFolder(), path);

        if (file.exists())
            return file;

        // Create empty file and all necessary directories
        createFileAndDirectory(path);

        if (defaults != null)
            try (InputStream is = i.getResource(path)) {
                Objects.requireNonNull(is, "Inbuilt file not found: " + path);

                // Now copy the content of the default file to there
                Files.copy(is, Paths.get(file.toURI()), StandardCopyOption.REPLACE_EXISTING);
                Utils.log("Created the default file '" + path + "'.");
            } catch (final IOException e) {
                Utils.logError(e);
            }

        return file;
    }

    // Creates YourPlugin folder in plugins/ and the necessary file (e.g., settings.yml)
    private @NotNull File createFileAndDirectory(@NotNull String path) {
        // The data folder is your plugin's folder with your plugin's name inside plugins/ folder.
        final File dataFolder = Utils.getInstance().getDataFolder();
        final int lastIndex = path.lastIndexOf('/');
        final File directory = new File(dataFolder, path.substring(0, Math.max(lastIndex, 0)));

        // Create all directories if necessary
        directory.mkdirs();

        final File destination = new File(dataFolder, path);

        try {
            if (!destination.createNewFile()) Utils.logError("Failed to create the '" + destination + "' file...");

        } catch (final IOException ex) {
            Utils.logError(ex);
            Utils.logError("Failed to create file '" + path + "'.");
        }

        return destination;
    }

    // A helper class
    private static final class PrimitiveWrapper {
        private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

        private static boolean isWrapperType(Class<?> clazz) {
            return WRAPPER_TYPES.contains(clazz);
        }

        private static @NotNull Set<Class<?>> getWrapperTypes() {
            final Set<Class<?>> ret = new HashSet<>();
            ret.add(Boolean.class);
            ret.add(Character.class);
            ret.add(Byte.class);
            ret.add(Short.class);
            ret.add(Integer.class);
            ret.add(Long.class);
            ret.add(Float.class);
            ret.add(Double.class);
            ret.add(Void.class);
            return ret;
        }
    }
}