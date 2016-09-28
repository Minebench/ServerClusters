package de.themoep.serverclusters.bungee.listeners;

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
        String servername = event.getPlayer().getServer().getInfo().getName();
        plugin.getLogger().info("ServerSwitchEvent - to " + servername);
        plugin.getClusterManager().getClusterByServer(servername).setLogoutServer(event.getPlayer(), servername);
    }

}
