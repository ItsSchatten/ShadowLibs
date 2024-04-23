package com.itsschatten.libs.configutils;

import com.itsschatten.libs.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerConfigManager {

    private static List<PlayerConfigManager> configs = new ArrayList<>();
    private final UUID u;
    private final String path;
    private final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(this.getClass());
    private FileConfiguration fc;
    private File file;

    private PlayerConfigManager(Player p) {
        this.u = p.getUniqueId();
        this.path = null;

        configs.add(this);
    }

    private PlayerConfigManager(Player p, String path) {
        this.u = p.getUniqueId();
        this.path = path;

        configs.add(this);
    }


    private PlayerConfigManager(UUID u, String path) {
        this.u = u;
        this.path = path;

        configs.add(this);
    }

    private PlayerConfigManager(UUID u) {
        this.u = u;
        this.path = null;

        configs.add(this);
    }

    /**
     * Get a config from type 'ConfigManager'. If it doesn't exist it will
     * create a new ConfigManager. NOTE: Player 'p' must be exactly the
     * ConfigManager's name. Creates thread safe. So there is only one
     * instanceof this class ever.
     *
     * @param p The Player of the config found by getOwner()
     * @return Config for given player.
     */
    public static PlayerConfigManager getConfig(Player p) {
        for (final PlayerConfigManager c : configs)
            if (c.getOwnerUUID().equals(p.getUniqueId()))
                return c;
        return new PlayerConfigManager(p);
    }

    public static PlayerConfigManager getConfig(Player p, String path) {
        for (final PlayerConfigManager c : configs)
            if (c.getOwnerUUID().equals(p.getUniqueId())) {
                return c;
            }
        return new PlayerConfigManager(p, path);
    }

    /**
     * Get a config from type 'ConfigManager'. If it doesn't exist it will
     * create a new ConfigManager. NOTE: UUID 'u' must be exactly the
     * ConfigManager's name. Creates thread safe. So there is only one
     * instanceof this class ever.
     *
     * @param u The UUID of the player who is the owner of the config
     * @return Config for given UUID
     */
    public static PlayerConfigManager getConfig(UUID u) {
        for (final PlayerConfigManager c : configs)
            if (c.getOwnerUUID().equals(u))
                return c;
        return new PlayerConfigManager(u);
    }

    /**
     * Gets the owner of the config
     *
     * @return The player as type bukkit.entity.Player
     */
    public Player getOwner() {
        if (u == null) {
            try {
                throw new Exception();
            } catch (final Exception e) {
                getInstance().getLogger().warning("ERR... Player is Null!");
                e.printStackTrace();
            }
        }

        return Bukkit.getPlayer(u);
    }

    /**
     * Gets the owner of the config as UUID
     *
     * @return java.util.UUID
     */
    public UUID getOwnerUUID() {
        if (u == null)
            try {
                throw new Exception();
            } catch (final Exception e) {
                getInstance().getLogger().warning("ERR... Player is Null!");
                e.printStackTrace();
            }
        return u;
    }

    /**
     * +
     * <p>
     * Returns an instanceof the JavaPlugin. AKA the Main class.
     *
     * @return The class that extends JavaPlugin
     */
    public JavaPlugin getInstance() {
        if (plugin == null)
            try {
                throw new Exception();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        return plugin;
    }

    /**
     * Deletes the file
     *
     * @return True if the config was successfully deleted. If anything went
     * wrong it returns false
     */
    public boolean delete() {
        return getFile().delete();
    }

    /**
     * Checks to make sure the config is null or not. This is only a check and
     * it wont create the config.
     *
     * @return True if it exists and False if it doesn't
     */
    public boolean exists() {
        if (fc == null || file == null) {
            File temp;
            if (path != null)
                temp = new File(path, getOwnerUUID() + ".yml");
            else
                temp = new File(getDataFolder(), getOwnerUUID() + ".yml");
            if (!temp.exists())
                return false;
            else
                file = temp;
        }
        return true;
    }


    /**
     * Gets the plugin's folder. If none exists it will create it.
     *
     * @return The folder as type java.io.File
     */
    public File getDataFolder() {
        final File dir = new File(PlayerConfigManager.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", " "));
        final File d = new File(dir.getParentFile().getPath(), getInstance().getName() + File.separator + "data");
        if (!d.exists())
            d.mkdirs();
        return d;
    }

    /**
     * Gets the File for the owner. If none exists it will create it.
     *
     * @return The File as type java.io.File
     */
    public File getFile() {
        if (file == null) {
            if (path != null) file = new File(path, getOwnerUUID() + ".yml");
            else file = new File(getDataFolder(), getOwnerUUID() + ".yml");

            if (!file.exists())
                try {
                    file.createNewFile();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
        }
        return file;
    }

    /**
     * Gets the config for the owner. If none exists it will create it.
     *
     * @return The config as type
     * org.bukkit.configuration.file.FileConfiguration
     */
    public FileConfiguration getConfig() {
        if (fc == null)
            fc = YamlConfiguration.loadConfiguration(getFile());
        return fc;
    }

    /**
     * Reloads or "Gets" the file and config
     */
    public void reload() {
        if (file == null) {
            if (path != null) file = new File(path, getOwnerUUID() + ".yml");
            else file = new File(getDataFolder(), getOwnerUUID() + ".yml");
            if (!file.exists())
                try {
                    file.createNewFile();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            fc = YamlConfiguration.loadConfiguration(file);
            Utils.log("Player Config for player " + getOwner().getDisplayName() + " has been 'reloaded'");
        }
    }

    /**
     * Deletes then creates the config
     */
    public void resetConfig() {
        delete();
        getConfig();
    }

    /**
     * Saves the config
     */
    public void saveConfig() {
        try {
            getConfig().save(getFile());
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}

