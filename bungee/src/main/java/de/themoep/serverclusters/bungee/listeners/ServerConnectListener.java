package de.themoep.serverclusters.bungee.listeners;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.ServerClusters;

import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import de.themoep.serverclusters.bungee.events.ClusterConnectEvent;
import de.themoep.serverclusters.bungee.events.ClusterSwitchEvent;
import de.themoep.serverclusters.bungee.events.NetworkConnectEvent;

public class ServerConnectListener implements Listener {

    private ServerClusters plugin;

    public ServerConnectListener(ServerClusters plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerConnect(ServerConnectEvent event) {
        plugin.getLogger().info("ServerConnectEvent - Target: " + event.getTarget().getName());
        Cluster target = plugin.getClusterManager().getClusterByServer(event.getTarget().getName());
        if (event.getPlayer().getServer() == null) {
            plugin.getProxy().getPluginManager().callEvent(new ClusterConnectEvent(plugin, event, target));
            plugin.getProxy().getPluginManager().callEvent(new NetworkConnectEvent(plugin, event, target));
        } else if (!target.containsServer(event.getPlayer().getServer().getInfo().getName())) {
            Cluster from = plugin.getClusterManager().getClusterByServer(event.getPlayer().getServer().getInfo().getName());
            plugin.getLogger().info("ServerConnectEvent - Origin: " + from.getName());
            plugin.getProxy().getPluginManager().callEvent(new ClusterSwitchEvent(plugin, event, from, target));
        }
    }

}
