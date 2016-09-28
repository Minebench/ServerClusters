package de.themoep.serverclusters.bungee.listeners;

import de.themoep.serverclusters.bungee.ServerClusters;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import de.themoep.serverclusters.bungee.events.NetworkConnectEvent;

public class NetworkConnectListener implements Listener {

    private ServerClusters plugin;

    public NetworkConnectListener(ServerClusters plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNetworkConnect(NetworkConnectEvent event) {
        // Just because why not? No seriously, we need this. Don't ask!
        event.setTarget(event.getTarget());
    }

}
