package de.themoep.serverclusters.bungee.manager;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.LocationInfo;
import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.serverclusters.bungee.storage.YamlStorage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class WarpManager extends Manager {

    private YamlStorage warpStorage;

    private Map<String, LocationInfo> globalWarps = new HashMap<String, LocationInfo>();

    public WarpManager(ServerClusters plugin) {
        super(plugin);
        warpStorage = new YamlStorage(plugin, "warps");

        Configuration globalSection = warpStorage.getConfig().getSection("global");
        for(String warpName : globalSection.getKeys()) {
            Configuration warp = globalSection.getSection(warpName);
            String serverName = warp.getString("server");
            String world = warp.getString("world");
            Double x = warp.getDouble("x", Double.NaN);
            Double y = warp.getDouble("y", Double.NaN);
            Double z = warp.getDouble("z",Double.NaN);
            float pitch = warp.getFloat("pitch", 0);
            float yaw = warp.getFloat("pitch", 0);

            if(serverName == null || serverName.isEmpty() || world == null || world.isEmpty() || x.isNaN() || y.isNaN() || z.isNaN()) {
                plugin.getLogger().log(Level.SEVERE, ChatColor.YELLOW + "Could not load the global warp '" + warpName + "'! It was configured wrong!");
                continue;
            }
            ServerInfo server = plugin.getProxy().getServerInfo(serverName);
            if(server == null) {
                plugin.getLogger().log(Level.WARNING, "There is no server with the name " + serverName + " for the global warp " + warpName);
            }
            globalWarps.put(warpName, new LocationInfo(serverName, world, x, y, z, pitch, yaw));
        }

        Configuration clusterSection = warpStorage.getConfig().getSection("warps" );
        for(String clusterName : clusterSection.getKeys()) {
            Cluster c = plugin.getClusterManager().getCluster(clusterName);
            if(c == null) {
                plugin.getLogger().log(Level.WARNING, "Cannot load warps for cluster " + clusterName + " ás this cluster does not exist!");
                continue;
            }
            Configuration warpSection = warpStorage.getConfig().getSection("warps." + c.getName());
            for(String warpName : warpSection.getKeys()) {
                Configuration warp = warpSection.getSection(warpName);
                String serverName = warp.getString("server");
                String world = warp.getString("world");
                Double x = warp.getDouble("x", Double.NaN);
                Double y = warp.getDouble("y", Double.NaN);
                Double z = warp.getDouble("z",Double.NaN);
                float pitch = warp.getFloat("pitch", 0);
                float yaw = warp.getFloat("pitch", 0);

                if(serverName == null || serverName.isEmpty() || world == null || world.isEmpty() || x.isNaN() || y.isNaN() || z.isNaN()) {
                    plugin.getLogger().log(Level.SEVERE, ChatColor.YELLOW + "Could not load the warp '" + warpName + "' for cluster '" + c.getName() + "'! It was configured wrong!");
                    continue;
                }
                ServerInfo server = plugin.getProxy().getServerInfo(serverName);
                if(server == null) {
                    plugin.getLogger().log(Level.WARNING, "There is no server with the name " + serverName + " for the global warp " + warpName);
                }
                c.addWarp(warpName, new LocationInfo(serverName, world, x, y, z, pitch, yaw));
            }
        }
    }

    /**
     * Teleport a player to a specific warp, tries to resolve global warps if no cluster warp was found
     * @param player The player to teleport
     * @param warpName The warp to teleport to
     * @return <tt>true</tt> if the teleport was initiated, <tt>false</tt> if not (the player gets an error message)
     */
    public boolean warp(ProxiedPlayer player, String warpName) {
        Cluster cluster;
        boolean globalFallback = !warpName.contains(":");
        if(!globalFallback) {
            String[] parts = warpName.split(":");
            String clusterName = parts[0];
            warpName = parts[1];
            cluster = plugin.getClusterManager().getCluster(clusterName);
        } else {
            cluster = plugin.getClusterManager().getPlayerCluster(player);
        }

        if(cluster == null || !player.hasPermission("serverclusters.cluster." + cluster.getName())) {
            player.sendMessage(ChatColor.YELLOW + "Could not find the server for this warp!");
            return false;
        }
        if(!player.hasPermission("serverclusters.warp." + cluster.getName() + "." + warpName)) {
            player.sendMessage(ChatColor.YELLOW + "Der Warp " + ChatColor.RED + warpName + ChatColor.YELLOW + " existiert nicht.");
            return false;
        }

        LocationInfo warp = cluster.getWarp(warpName);
        if(globalFallback && warp == null) {
            if(!player.hasPermission("serverclusters.globalwarp." + warpName)) {
                player.sendMessage(ChatColor.YELLOW + "Der Warp " + ChatColor.RED + warpName + ChatColor.YELLOW + " existiert nicht.");
                return false;
            }
            warp = getGlobalWarp(warpName);
        }
        ServerInfo server = plugin.getProxy().getServerInfo(warp.getServer());
        if(server == null) {
            player.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "The warp " + warpName + " was configured wrong! Please contact an admin.");
            plugin.getLogger().severe("There is no server with the name " + warp.getServer() + " for the warp " + warpName);
            return false;
        }
        plugin.getTeleportUtils().teleport(player, warp);
        return true;
    }

    /**
     * Get a set of names of the global warps
     * @return Set of global warp names
     */
    public Set<String> getGlobalWarps() {
        return globalWarps.keySet();
    }

    private LocationInfo getGlobalWarp(String warpName) {
        if(globalWarps.containsKey(warpName))
            return globalWarps.get(warpName);

        for(String warp : globalWarps.keySet()) {
            if(warp.equalsIgnoreCase(warpName))
                return globalWarps.get(warp);
        }
        return null;
    }

    @Override
    public void destroy() {
        warpStorage.close();
    }
}
