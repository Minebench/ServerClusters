package de.themoep.serverclusters.bungee.listeners;

import de.themoep.serverclusters.bungee.ServerClusters;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import de.themoep.serverclusters.bungee.events.NetworkConnectEvent;
import net.md_5.bungee.event.EventPriority;

public class NetworkConnectListener implements Listener {

    private ServerClusters plugin;

    public NetworkConnectListener(ServerClusters plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onNetworkConnect(NetworkConnectEvent event) {
        // Just because why not? No seriously, we need this!
        // By setting the target here we make sure that the player connects to his
        // logout server when his join targets a cluster and not the default server
        // that BungeeCord wants him to connect to.
        event.setTarget(event.getTarget());
    }

}
