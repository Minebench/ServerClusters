package de.themoep.serverclusters.bungee.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.LocationInfo;
import de.themoep.serverclusters.bungee.ServerClusters;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Created by Phoenix616 on 05.01.2015.
 */
public class TeleportUtils {

    private final ServerClusters plugin;
    private Map<UUID, Boolean> teleportingPlayers = new ConcurrentHashMap<>();

    public TeleportUtils(ServerClusters plugin) {
        this.plugin = plugin;
        plugin.getProxy().registerChannel("sc:tptoplayer");
        plugin.getProxy().registerChannel("sc:tptolocation");
    }

    /**
     * Teleports a player to a targeted player
     * @param player The player to teleport
     * @param target The player to teleport to
     */
    public void teleportToPlayer(ProxiedPlayer player, ProxiedPlayer target) {
        if (player.getServer().getInfo().getName().equals(target.getServer().getInfo().getName())) {
            player.sendMessage(ChatColor.GREEN + "Teleportiere zu " + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + "...");
            teleportToPlayerPM(player, target);
        } else {
            Cluster playerCluster = plugin.getClusterManager().getClusterByServer(player.getServer().getInfo().getName());
            Cluster targetCluster = plugin.getClusterManager().getClusterByServer(target.getServer().getInfo().getName());
            if (playerCluster != targetCluster)
                player.sendMessage(ChatColor.GREEN + "Verbinde mit " + ChatColor.YELLOW + targetCluster.getName() + ChatColor.GREEN + "...");
            connect(player, target.getServer().getInfo());
            player.sendMessage(ChatColor.GREEN + "Teleportiere zu " + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + "...");
            teleportToPlayerPM(player, target);
        }
    }

    /**
     * Helper method to send the plugin message for teleporting to players
     * @param player Player to teleport
     * @param target Targeted player
     */
    private void teleportToPlayerPM(ProxiedPlayer player, ProxiedPlayer target) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(player.getName());
        out.writeUTF(target.getName());
        target.getServer().sendData("sc:tptoplayer", out.toByteArray());
    }

    /**
     * Teleports a player to a location
     * @param player   The player to teleport
     * @param location The location to teleport to
     * @return <tt>true</tt> if all worked without any error; <tt>false</tt> if the server of the location wasn't found
     */
    public boolean teleport(ProxiedPlayer player, LocationInfo location) {
        ServerInfo server = plugin.getProxy().getServerInfo(location.getServer());
        if (server == null) {
            plugin.getLogger().log(Level.SEVERE, "Could not teleport player " + player.getName() + " as the server " + location.getServer() + " does not exist for the following location object: " + location);
            return false;
        }
        teleportToLocation(player, server, location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        return true;
    }

    /**
     * Teleport a player to a location on a server
     * @param player Player to teleport
     * @param server Server to teleport to
     * @param world  World to teleport to
     * @param x      X coordinate of the location
     * @param y      X coordinate of the location
     * @param z      Z coordinate of the location
     * @param yaw    Yaw of the location
     * @param pitch  Pitch of the location
     */
    public void teleportToLocation(ProxiedPlayer player, ServerInfo server, String world, double x, double y, double z, float yaw, float pitch) {
        if (!player.getServer().getInfo().getName().equals(server.getName())) {
            Cluster playerCluster = plugin.getClusterManager().getClusterByServer(player.getServer().getInfo().getName());
            Cluster targetCluster = plugin.getClusterManager().getClusterByServer(server.getName());
            if (playerCluster != targetCluster) {
                player.sendMessage(ChatColor.GREEN + "Verbinde mit " + ChatColor.YELLOW + targetCluster.getName() + ChatColor.GREEN + "...");
            }
            connect(player, server);
        }
        teleportToLocationPM(player, server, world, x, y, z, yaw, pitch);
    }

    /**
     * Helper method to send the plugin message for teleporting to locations
     * @param player Player to teleport
     * @param server Server to teleport to
     * @param world  World to teleport to
     * @param x      X coordinate of the location
     * @param y      X coordinate of the location
     * @param z      Z coordinate of the location
     * @param yaw    Yaw of the location
     * @param pitch  Pitch of the location
     */
    private void teleportToLocationPM(ProxiedPlayer player, ServerInfo server, String world, double x, double y, double z, float yaw, float pitch) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(player.getName());
        out.writeUTF(world);
        out.writeDouble(x);
        out.writeDouble(y);
        out.writeDouble(z);
        out.writeFloat(yaw);
        out.writeFloat(pitch);
        server.sendData("sc:tptolocation", out.toByteArray());
    }

    /**
     * Connect a player to a server while marking this connect as a teleport
     * @param player        The player to connect
     * @param serverInfo    The server to connect to
     */
    private void connect(ProxiedPlayer player, ServerInfo serverInfo) {
        teleportingPlayers.put(player.getUniqueId(), true);
        player.connect(serverInfo);
        teleportingPlayers.remove(player.getUniqueId());
    }

    /**
     * Check whether or not a player is currently teleporting
     * @param player    The player to check
     * @return          <tt>true</tt> if he is teleporting; <tt>false</tt> if not
     */
    public boolean isTeleporting(ProxiedPlayer player) {
        return teleportingPlayers.containsKey(player.getUniqueId());
    }
}
