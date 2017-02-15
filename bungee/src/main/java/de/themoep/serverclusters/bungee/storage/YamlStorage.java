package de.themoep.serverclusters.bungee.storage;

import de.themoep.bungeeplugin.FileConfiguration;
import de.themoep.serverclusters.bungee.ServerClusters;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class YamlStorage extends ValueStorage {

    private FileConfiguration config;

    public YamlStorage(ServerClusters plugin, String name) {
        super(plugin, name);
        try {
            config = new FileConfiguration(plugin, name + ".yml");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while loading config '" + name + ".yml'!");
            e.printStackTrace();
        }
    }

    @Override
    public String getValue(UUID playerId) {
        return config.getString(playerId.toString());
    }

    @Override
    public void putValue(UUID playerId, String value) {
        config.set(playerId.toString(), value);
        save();
    }

    /**
     * save configuration to disk
     */
    public void save() {
        config.saveConfig();
    }

    @Override
    public void close() {
        save();
    }
}
