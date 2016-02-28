package de.themoep.serverclusters.bungee.storage;

import de.themoep.serverclusters.bungee.ServerClusters;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public abstract class ValueStorage {

    protected final ServerClusters plugin;
    protected final String name;

    public ValueStorage(ServerClusters plugin, String name) {
        this.plugin = plugin;
        this.name = name.replace(' ', '_');
    }

    public String getValue(String playerName) {
        ProxiedPlayer player = plugin.getProxy().getPlayer(playerName);
        if(player != null) {
            return getValue(player.getUniqueId());
        }
        return null;
    }

    public abstract String getValue(UUID playerId);

    public abstract void close();
}
