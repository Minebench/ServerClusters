package de.themoep.serverclusters.bungee.listeners;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.ServerClusters;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

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
            NetworkConnectEvent networkConnectEvent = new NetworkConnectEvent(event.getPlayer(), target);
            plugin.getProxy().getPluginManager().callEvent(networkConnectEvent);
            if (networkConnectEvent.isCancelled()) {
                event.setCancelled(true);
                event.getPlayer().disconnect(networkConnectEvent.getCancelMessage());
            } else if (networkConnectEvent.getTarget() != null) {
                String logoutServer = networkConnectEvent.getTarget().getLogoutServer(event.getPlayer().getUniqueId());
                if (logoutServer != null) {
                    ServerInfo server = plugin.getProxy().getServerInfo(logoutServer);
                    if (server != null) {
                        event.setTarget(server);
                    } else {
                        event.setCancelled(true);
                        event.getPlayer().disconnect(new ComponentBuilder("Error:").color(ChatColor.DARK_RED).append(" The server " + logoutServer + " does not exist!").color(ChatColor.RED).create());
                    }
                }
            }
        } else if (!target.containsServer(event.getPlayer().getServer().getInfo().getName())) {
            Cluster from = plugin.getClusterManager().getClusterByServer(event.getPlayer().getServer().getInfo().getName());
            plugin.getLogger().info("ServerConnectEvent - Origin: " + from.getName());
            ClusterSwitchEvent clusterSwitchEvent = new ClusterSwitchEvent(event.getPlayer(), from, target);
            plugin.getProxy().getPluginManager().callEvent(clusterSwitchEvent);
            if (clusterSwitchEvent.isCancelled()) {
                event.setCancelled(true);
            } else if (clusterSwitchEvent.getTo() != null) {
                String logoutServer = clusterSwitchEvent.getTo().getLogoutServer(event.getPlayer().getUniqueId());
                if (logoutServer != null) {
                    ServerInfo server = plugin.getProxy().getServerInfo(logoutServer);
                    if (server != null) {
                        event.setTarget(server);
                    } else {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(new ComponentBuilder("Error:").color(ChatColor.DARK_RED).append(" The server " + logoutServer + " does not exist!").color(ChatColor.RED).create());
                    }
                }
            }
        }
    }

}
