package de.themoep.serverclusters.bungee.manager;

import de.themoep.serverclusters.bungee.ServerClusters;

public abstract class Manager {
    protected final ServerClusters plugin;

    public Manager(ServerClusters plugin) {
        this.plugin = plugin;
    }

    public abstract void destroy();
}
