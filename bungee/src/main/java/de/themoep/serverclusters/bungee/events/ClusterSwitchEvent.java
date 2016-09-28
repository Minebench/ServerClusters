package de.themoep.serverclusters.bungee.events;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.ServerClusters;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Event;

public class ClusterSwitchEvent extends Event {

    private ServerClusters plugin = null;
    private Cluster from = null;
    private Cluster to = null;
    private ServerConnectEvent event = null;

    /**
     * Called before a player changes the cluster and is not called on the first connect (see NetworkConnectEvent for that) in contrast to it's Bungee counterpart ServerSwitchEvent!
     * @param plugin
     * @param event
     * @param from
     * @param to
     */
    public ClusterSwitchEvent(ServerClusters plugin, ServerConnectEvent event, Cluster from, Cluster to) {
        this.from = from;
        this.to = to;
        this.plugin = plugin;
        this.event = event;
    }

    /**
     * Get the cluster the player switches from
     * @return The origin cluster
     */
    public Cluster getFrom() {
        return from;
    }

    /**
     * Get the cluster the player tries to connect to
     * @return The targeted cluster
     */
    public Cluster getTo() {
        return to;
    }

    /**
     * Sets which cluster the player should connect to
     * @param cluster The target cluster
     */
    public void setTo(Cluster cluster) {
        event.setTarget(plugin.getProxy().getServerInfo(cluster.getLogoutServer(event.getPlayer().getUniqueId())));
        from = cluster;
    }

    /**
     * Get the player of the event
     * @return The ProxiedPlayer
     */
    public ProxiedPlayer getPlayer() {
        return event.getPlayer();
    }

}
