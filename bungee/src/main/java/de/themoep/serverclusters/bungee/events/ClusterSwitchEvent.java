package de.themoep.serverclusters.bungee.events;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.ServerClusters;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

public class ClusterSwitchEvent extends Event implements Cancellable {

    private final ProxiedPlayer player;
    private Cluster from = null;
    private Cluster to = null;
    private boolean cancelled = false;

    /**
     * Called before a player changes the cluster and is not called on the first connect (see NetworkConnectEvent for that) in contrast to it's Bungee counterpart ServerSwitchEvent!
     * @param from
     * @param to
     */
    public ClusterSwitchEvent(ProxiedPlayer player, Cluster from, Cluster to) {
        this.player = player;
        this.from = from;
        this.to = to;
    }

    /**
     * Get the cluster the player switches from
     * @return  The origin cluster
     */
    public Cluster getFrom() {
        return from;
    }

    /**
     * Get the cluster the player tries to connect to
     * @return  The targeted cluster
     */
    public Cluster getTo() {
        return to;
    }

    /**
     * Sets which cluster the player should connect to
     * @param cluster   The target cluster, if this is null the cluster will
     *                  not change or attempt to find the logout server
     */
    public void setTo(Cluster cluster) {
        to = cluster;
    }

    /**
     * Get the player of the event
     * @return  The ProxiedPlayer
     */
    public ProxiedPlayer getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
