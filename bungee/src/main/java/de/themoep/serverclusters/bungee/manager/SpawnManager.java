package de.themoep.serverclusters.bungee.manager;

import de.themoep.bungeeplugin.FileConfiguration;
import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.LocationInfo;
import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.serverclusters.bungee.ServerNotFoundException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class SpawnManager extends Manager {
    private final FileConfiguration spawnStorage;

    private LocationInfo globalSpawn = null;

    public SpawnManager(ServerClusters plugin) throws IOException {
        super(plugin);
        spawnStorage = new FileConfiguration(plugin, "spawns.yml");

        Configuration global = spawnStorage.getSection("global");
        if (global.getKeys().size() > 0) {
            String serverName = global.getString("server");
            String world = global.getString("world");
            Double x = global.getDouble("x", Double.NaN);
            Double y = global.getDouble("y", Double.NaN);
            Double z = global.getDouble("z", Double.NaN);
            float pitch = global.getFloat("pitch", 0);
            float yaw = global.getFloat("yaw", 0);

            if (serverName == null || serverName.isEmpty() || world == null || world.isEmpty() || x.isNaN() || y.isNaN() || z.isNaN()) {
                plugin.getLogger().log(Level.SEVERE, ChatColor.YELLOW + "Could not load the global spawn! It was configured wrong!");
            }
            ServerInfo server = plugin.getProxy().getServerInfo(serverName);
            if (server == null) {
                plugin.getLogger().log(Level.WARNING, "There is no server with the name " + serverName + " for the global spawn");
            }
            globalSpawn = new LocationInfo(serverName, world, x, y, z, yaw, pitch);
        }

        Configuration clusterSection = spawnStorage.getSection("cluster");
        for (String clusterName : clusterSection.getKeys()) {
            Cluster c = plugin.getClusterManager().getCluster(clusterName);
            if (c == null) {
                plugin.getLogger().log(Level.WARNING, "Cannot load warps for cluster " + clusterName + " �s this cluster does not exist!");
                continue;
            }
            Configuration spawn = clusterSection.getSection(clusterName);

            String serverName = spawn.getString("server");
            String world = spawn.getString("world");
            Double x = spawn.getDouble("x", Double.NaN);
            Double y = spawn.getDouble("y", Double.NaN);
            Double z = spawn.getDouble("z", Double.NaN);
            float pitch = spawn.getFloat("pitch", 0);
            float yaw = spawn.getFloat("yaw", 0);

            if (serverName == null || serverName.isEmpty() || world == null || world.isEmpty() || x.isNaN() || y.isNaN() || z.isNaN()) {
                plugin.getLogger().log(Level.SEVERE, ChatColor.YELLOW + "Could not load the spawn for cluster '" + c.getName() + "'! It was configured wrong!");
                continue;
            }
            ServerInfo server = plugin.getProxy().getServerInfo(serverName);
            if (server == null) {
                plugin.getLogger().log(Level.WARNING, "There is no server with the name '" + serverName + "' for spawn of cluster '" + c.getName() + "'");
            }
            c.setSpawn(new LocationInfo(serverName, world, x, y, z, yaw, pitch));
        }
    }

    @Override
    public void destroy() {
        spawnStorage.saveConfig();
    }

    /**
     * Teleport a player to the spawn of a cluster, if the cluster doesn't have one the global one is used.
     * @param player  The player to teleport
     * @param cluster The cluster to which's spawn to teleport to
     * @return <tt>true</tt> if the teleport was initiated, <tt>false</tt> if no spawn found
     */
    public boolean spawnPlayer(CommandSender sender, ProxiedPlayer player, final Cluster cluster) throws ServerNotFoundException {
        LocationInfo spawn = cluster.getSpawn();
        if (spawn == null) {
            spawn = getGlobalSpawn();
        }
        if (spawn == null) {
            return false;
        }
        ServerInfo server = plugin.getProxy().getServerInfo(spawn.getServer());
        if (server == null) {
            sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "The spawn of " + cluster.getName() + " was configured wrong! Please contact an admin.");
            plugin.getLogger().severe("There is no server with the name " + cluster.getSpawn().getServer() + " for the spawn of " + cluster.getName());
            throw new ServerNotFoundException("There is no server with the name " + cluster.getSpawn().getServer() + " for the spawn of " + cluster.getName());
        }

        final UUID playerId = player.getUniqueId();

        final LocationInfo finalSpawn = spawn;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ProxiedPlayer player = plugin.getProxy().getPlayer(playerId);
                if (player != null && player.isConnected()) {
                    plugin.getTeleportUtils().teleport(player, finalSpawn);
                }
            }
        };

        if (plugin.getTeleportDelay() <= 0 || player.hasPermission("serverclusters.bypass.delay") || (sender != player && sender.hasPermission("serverclusters.bypass.delay"))) {
            player.sendMessage(ChatColor.GRAY + "Teleportiere zum Spawn von " + cluster.getName() + "...");
            runnable.run();
        } else {
            player.sendMessage(ChatColor.GRAY + "Teleportiere zum Spawn von " + cluster.getName() + "! Bleibe " + plugin.getTeleportDelay() + " Sekunden ruhig stehen...");

            plugin.getTeleportManager().scheduleDelayedTeleport(player, runnable);
        }
        return true;
    }

    /**
     * Add a new warp or change the position of an existing one
     * @param location The location
     * @param global Whether or not it should be global or cluster only
     * @return The location
     */
    public LocationInfo setSpawn(LocationInfo location, boolean global) throws IllegalArgumentException {
        if (global) {
            globalSpawn = location;
            spawnStorage.set("global", location.toConfig());
            spawnStorage.saveConfig();
        } else {
            Cluster cluster = plugin.getClusterManager().getClusterByServer(location.getServer());
            if (cluster != null) {
                cluster.setSpawn(location);
                spawnStorage.set("cluster." + cluster.getName(), location.toConfig());
                spawnStorage.saveConfig();
            } else {
                throw new IllegalArgumentException("No Cluster found for server " + location.getServer() + " in provided location!");
            }
        }
        return location;
    }

    /**
     * Remove a spawn
     * @param cluster The cluster to remove the spawn of, null if global
     * @return The removed LocationInfo or null if none was found
     */
    public LocationInfo removeSpawn(Cluster cluster) {
        if (cluster == null) {
            LocationInfo spawn = globalSpawn;
            globalSpawn = null;
            spawnStorage.set("global", null);
            spawnStorage.saveConfig();
            return spawn;
        }
        LocationInfo spawn = cluster.getSpawn();
        if (spawn != null) {
            cluster.setSpawn(null);
            spawnStorage.set("cluster." + cluster.getName(), null);
            spawnStorage.saveConfig();
        }
        return spawn;
    }

    public LocationInfo getGlobalSpawn() {
        return globalSpawn;
    }
}
