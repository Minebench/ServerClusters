package de.themoep.serverclusters.bungee.manager;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.serverclusters.bungee.ServerNotFoundException;
import de.themoep.serverclusters.bungee.WarpInfo;
import de.themoep.serverclusters.bungee.storage.YamlStorage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;

public class WarpManager extends Manager {

    private final YamlStorage warpStorage;

    private final Map<String, WarpInfo> globalWarps = new HashMap<String, WarpInfo>();

    public WarpManager(ServerClusters plugin) {
        super(plugin);
        warpStorage = new YamlStorage(plugin, "warps");

        Configuration globalSection = warpStorage.getConfig().getSection("global");
        for (String warpName : globalSection.getKeys()) {
            Configuration warp = globalSection.getSection(warpName);
            String serverName = warp.getString("server");
            String world = warp.getString("world");
            Double x = warp.getDouble("x", Double.NaN);
            Double y = warp.getDouble("y", Double.NaN);
            Double z = warp.getDouble("z", Double.NaN);
            float pitch = warp.getFloat("pitch", 0);
            float yaw = warp.getFloat("pitch", 0);

            if (serverName == null || serverName.isEmpty() || world == null || world.isEmpty() || x.isNaN() || y.isNaN() || z.isNaN()) {
                plugin.getLogger().log(Level.SEVERE, ChatColor.YELLOW + "Could not load the global warp '" + warpName + "'! It was configured wrong!");
                continue;
            }
            ServerInfo server = plugin.getProxy().getServerInfo(serverName);
            if (server == null) {
                plugin.getLogger().log(Level.WARNING, "There is no server with the name " + serverName + " for the global warp " + warpName);
            }
            globalWarps.put(warpName.toLowerCase(), new WarpInfo(warpName, serverName, world, x, y, z, pitch, yaw));
        }

        Configuration clusterSection = warpStorage.getConfig().getSection("warps");
        for (String clusterName : clusterSection.getKeys()) {
            Cluster c = plugin.getClusterManager().getCluster(clusterName);
            if (c == null) {
                plugin.getLogger().log(Level.WARNING, "Cannot load warps for cluster " + clusterName + " �s this cluster does not exist!");
                continue;
            }
            Configuration warpSection = warpStorage.getConfig().getSection("warps." + c.getName());
            for (String warpName : warpSection.getKeys()) {
                Configuration warp = warpSection.getSection(warpName);
                String serverName = warp.getString("server");
                String world = warp.getString("world");
                Double x = warp.getDouble("x", Double.NaN);
                Double y = warp.getDouble("y", Double.NaN);
                Double z = warp.getDouble("z", Double.NaN);
                float pitch = warp.getFloat("pitch", 0);
                float yaw = warp.getFloat("pitch", 0);

                if (serverName == null || serverName.isEmpty() || world == null || world.isEmpty() || x.isNaN() || y.isNaN() || z.isNaN()) {
                    plugin.getLogger().log(Level.SEVERE, ChatColor.YELLOW + "Could not load the warp '" + warpName + "' for cluster '" + c.getName() + "'! It was configured wrong!");
                    continue;
                }
                ServerInfo server = plugin.getProxy().getServerInfo(serverName);
                if (server == null) {
                    plugin.getLogger().log(Level.WARNING, "There is no server with the name " + serverName + " for the global warp " + warpName);
                }
                c.addWarp(new WarpInfo(warpName, serverName, world, x, y, z, pitch, yaw));
            }
        }
    }

    /**
     * Teleport a player to a specific warp, tries to resolve global warps if no cluster warp was found
     * @param player The player to teleport
     * @param warp   The warp to teleport to
     * @return <tt>true</tt> if the teleport was initiated, <tt>false</tt> if not (the player gets an error message)
     */
    public boolean warpPlayer(ProxiedPlayer player, WarpInfo warp) throws ServerNotFoundException {
        player.sendMessage(ChatColor.GRAY + "Teleportiere zu " + warp.getName() + "...");
        ServerInfo server = plugin.getProxy().getServerInfo(warp.getServer());
        if (server == null) {
            player.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "The warp " + warp.getName() + " was configured wrong! Please contact an admin.");
            plugin.getLogger().severe("There is no server with the name " + warp.getServer() + " for the warp " + warp.getName());
            throw new ServerNotFoundException("There is no server with the name " + warp.getServer() + " for the warp " + warp.getName());
        }
        plugin.getTeleportUtils().teleport(player, warp);
        return true;
    }

    /**
     * Check whether or not a sender has access to a warp, either by permissions or their location in the network
     * @param sender The sender
     * @param warp   The warp to check
     * @return <tt>true</tt> if the player can warp there; <tt>false</tt> if not
     */
    public boolean checkAccess(CommandSender sender, WarpInfo warp) {
        boolean access = false;
        Cluster cluster = plugin.getClusterManager().getClusterByServer(warp.getServer());
        if (cluster != null && cluster.hasAccess(sender)) {
            if (globalWarps.containsValue(warp)) {
                access = sender.hasPermission("serverclusters.globalwarp." + warp.getName().toLowerCase());
            } else {
                access = sender.hasPermission("serverclusters.warp." + cluster.getName().toLowerCase() + "." + warp.getName().toLowerCase());
            }
            if (sender instanceof ProxiedPlayer && !sender.hasPermission("serverclusters.command.warp.intercluster")) {
                Cluster playerCluster = plugin.getClusterManager().getPlayerCluster((ProxiedPlayer) sender);
                access &= cluster.equals(playerCluster);
            }
        }
        return access;
    }

    /**
     * Get a collection of the global warps
     * @return Collection of global warp info
     */
    public Collection<WarpInfo> getGlobalWarps() {
        return globalWarps.values();
    }

    /**
     * Get a collection of the global warps a sender has access to
     * @param sender The sender to check permissions for
     * @return Collection of global warp info
     */
    public Collection<WarpInfo> getGlobalWarps(CommandSender sender) {
        Collection<WarpInfo> warps = new HashSet<WarpInfo>();
        for (WarpInfo warp : getGlobalWarps()) {
            if (checkAccess(sender, warp)) {
                warps.add(warp);
            }
        }
        return warps;
    }

    private WarpInfo getGlobalWarp(String warpName) {
        return globalWarps.get(warpName.toLowerCase());
    }

    /**
     * Get a warp by it's name, uses the global warp if no cluster is specified
     * @param warpName The name of the warp, use clustername:warpname to target a specific cluster
     * @return The warp or null if none found
     */
    public WarpInfo getWarp(String warpName) {
        WarpInfo warp = getGlobalWarp(warpName);
        if (warp == null) {
            if (warpName.contains(":")) {
                String[] parts = warpName.split(":");
                Cluster cluster = plugin.getClusterManager().getCluster(parts[0]);
                if (cluster != null) {
                    warp = cluster.getWarp(parts[1]);
                }
            } else {
                for (Cluster cluster : plugin.getClusterManager().getClusterlist()) {
                    warp = cluster.getWarp(warpName);
                    if (warp != null) {
                        break;
                    }
                }
            }
        }
        return warp;
    }

    @Override
    public void destroy() {
        warpStorage.close();
    }
}