package de.themoep.serverclusters.bungee.storage;

import de.themoep.serverclusters.bungee.ServerClusters;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class YamlStorage extends ValueStorage {

    private Configuration config;

    protected final static ConfigurationProvider ymlCfg = ConfigurationProvider.getProvider( YamlConfiguration.class );

    protected File configFile;

    public YamlStorage(ServerClusters plugin, String name) {
        super(plugin, name);
        configFile = new File(plugin.getDataFolder(), name + ".yml");
        try {
            config = ymlCfg.load(configFile);
        } catch(FileNotFoundException e) {
            // Config file not found. Assuming first load.
            plugin.getLogger().log(Level.INFO, "File '" + configFile.getName() + "' not found! Creating a new one!");
            config = new Configuration();
            close();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while loading config '" + configFile.getName() + "'!");
            e.printStackTrace();
        }
    }

    @Override
    public String getValue(UUID playerId) {
        return config.getString(playerId.toString());
    }

    @Override
    public void close() {
        try {
            ymlCfg.save(config, configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Unable to save configuration at '" + configFile.getAbsolutePath() + "'");
            e.printStackTrace();
        }
    }
}
