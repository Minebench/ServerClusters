package de.themoep.serverclusters.bungee.events;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.ServerClusters;

import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Event;

public class NetworkConnectEvent extends Event {
	
	private ServerClusters plugin = null;
	private Cluster target = null;
	private ServerConnectEvent event = null;
	
	/**
	 * Event which represents a player connecting to the first server after he joined the BungeeCord network
	 * @param plugin
	 * @param event
	 * @param target
	 */
	public NetworkConnectEvent(ServerClusters plugin, ServerConnectEvent event, Cluster target) {
		this.target = target;
		this.plugin = plugin;
		this.event = event;
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
	 * @param cluster The target cluster
	 */
	public void setTarget(Cluster cluster) {
		event.setTarget(plugin.getProxy().getServerInfo(cluster.getLoggoutServer(event.getPlayer().getUniqueId())));
		target = cluster;
	}
}
