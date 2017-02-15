package de.themoep.serverclusters.bungee.listeners;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.ServerClusters;

import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerSwitchListener implements Listener {

    private ServerClusters plugin;

    public ServerSwitchListener(ServerClusters plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {
        String serverName = event.getPlayer().getServer().getInfo().getName();
        plugin.getLogger().info("ServerSwitchEvent - to " + serverName);
        Cluster cluster = plugin.getClusterManager().getClusterByServer(serverName);
        if (cluster != null) {
            cluster.setLogoutServer(event.getPlayer().getUniqueId(), serverName);
        }
    }

}
