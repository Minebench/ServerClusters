package de.themoep.serverclusters.bungee.manager;

import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.serverclusters.bungee.storage.YamlStorage;

public class SpawnManager extends Manager {

    private final YamlStorage spawnStorage;

    public SpawnManager(ServerClusters plugin) {
        super(plugin);
        spawnStorage = new YamlStorage(plugin, "spawns");
    }

    @Override
    public void destroy() {
        spawnStorage.close();
    }

}
