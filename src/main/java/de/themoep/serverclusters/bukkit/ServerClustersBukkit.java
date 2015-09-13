package de.themoep.serverclusters.bukkit;

import de.themoep.serverclusters.bukkit.manager.TeleportManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class ServerClustersBukkit extends JavaPlugin {

    private TeleportManager tpman;

    public void onEnable() {

        getLogger().log(Level.INFO, "Initialising Teleport Manager");
        tpman = new TeleportManager(this);

        getLogger().log(Level.INFO, "Registering Plugin Message Channel");
        getServer().getMessenger().registerIncomingPluginChannel(this, "ServerClusters", new BungeePluginMessageListener(this));
        getServer().getMessenger().registerOutgoingPluginChannel(this, "ServerClusters");

        getLogger().log(Level.INFO, "Registering Event Listener");
        getServer().getPluginManager().registerEvents(getTeleportManager(), this);
    }

    /**
     * Get the teleport manager.
     */
    public TeleportManager getTeleportManager() {
        return tpman;
    }

}
