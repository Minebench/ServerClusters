package de.themoep.serverclusters.bungee.events;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.ServerClusters;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

public class NetworkConnectEvent extends Event implements Cancellable {

    private final ProxiedPlayer player;
    private Cluster target = null;
    private boolean cancelled = false;
    private BaseComponent[] cancelMessage = new BaseComponent[]{};

    /**
     * Event which represents a player connecting to the first server after he joined the BungeeCord network
     * @param player    The player that connects to the network
     * @param target    The target cluster
     */
    public NetworkConnectEvent(ProxiedPlayer player, Cluster target) {
        this.player = player;
        this.target = target;
    }

    /**
     * Get the cluster the player tries to connect to
     * @return The targeted cluster
     */
    public Cluster getTarget() {
        return target;
    }

    /**
     * Sets which cluster the player should connect to
     * @param cluster   The target cluster, if this is null the cluster will
     *                  not change or attempt to find the logout server
     */
    public void setTarget(Cluster cluster) {
        target = cluster;
    }

    /**
     * Get the player of the event
     * @return The ProxiedPlayer
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

    @Deprecated
    public void setCancelMessage(String message) {
        setCancelMessage(TextComponent.fromLegacyText(message));
    }

    private void setCancelMessage(BaseComponent[] message) {
        this.cancelMessage = message;
    }

    public BaseComponent[] getCancelMessage() {
        return cancelMessage;
    }
}
