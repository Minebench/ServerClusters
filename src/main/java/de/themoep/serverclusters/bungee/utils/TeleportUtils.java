package de.themoep.serverclusters.bungee.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.ServerClusters;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

/**
 * Created by Phoenix616 on 05.01.2015.
 */
public class TeleportUtils {

    ServerClusters plugin;

    public TeleportUtils(ServerClusters plugin) {
        this.plugin = plugin;
    }

    /**
     * Teleports a player to a targetted player, checks if the player is allowed to teleport between clusters! (serverclusters.teleport.intercluster)
     * @param player The player to teleport
     * @param target The player to teleport to
     * @return True if player got teleported, false if not
     */
    public boolean teleportToPlayer(ProxiedPlayer player, ProxiedPlayer target) {
        Cluster playerCluster = plugin.getClusterManager().getClusterByServer(player.getServer().getInfo().getName());
        Cluster targetCluster = plugin.getClusterManager().getClusterByServer(target.getServer().getInfo().getName());
        if(player.getServer().getInfo().getName().equals(target.getServer().getInfo().getName())) {
            teleportToPlayerPM(player, target);
            player.sendMessage(ChatColor.GREEN + "Zu " + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + " teleportiert");
            return true;
        } else if (playerCluster == targetCluster || player.hasPermission("serverclusters.teleport.intercluster")){
            if(playerCluster != targetCluster)
                player.sendMessage(ChatColor.GREEN + "Verbinde mit " + ChatColor.YELLOW + targetCluster.getName() + ChatColor.GREEN + "...");
            player.connect(target.getServer().getInfo());
            teleportToPlayerPM(player, target);
            player.sendMessage(ChatColor.GREEN + "Zu " + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + " teleportiert");
            return true;
        } else
            player.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "You are not allowed to teleport between clusters!");
        return false;
    }

    /**
     * Helper method to send the plugin message for teleporting to players
     * @param player Player to teleport
     * @param target Targetted player
     */
    private void teleportToPlayerPM(ProxiedPlayer player, ProxiedPlayer target) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("TeleportToPlayer");
        out.writeUTF(player.getName());
        out.writeUTF(target.getName());
        target.getServer().sendData("ServerClusters", out.toByteArray());
    }

    /**
     * Teleport a player to a location on a server, checks if the player is allowed to teleport between clusters! (serverclusters.teleport.intercluster)
     * @param player Player to teleport
     * @param server Server to teleport to
     * @param world World to teleport to
     * @param x X coordinate of the location
     * @param y X coordinate of the location
     * @param z Z coordinate of the location
     * @param yaw Yaw of the location
     * @param pitch Pitch of the location
     * @return True if player got teleported, false if not
     */
    public boolean teleportToLocation(ProxiedPlayer player, Server server, String world, double x, double y, double z, float yaw, float pitch) {
        Cluster playerCluster = plugin.getClusterManager().getClusterByServer(player.getServer().getInfo().getName());
        Cluster targetCluster = plugin.getClusterManager().getClusterByServer(server.getInfo().getName());
        if(player.getServer().getInfo().getName().equals(server.getInfo().getName())) {
            teleportToLocationPM(player, server, world, x, y, z, yaw, pitch);
            return true;
        } else if (playerCluster == targetCluster || player.hasPermission("serverclusters.teleport.intercluster")){
            player.connect(server.getInfo());
            teleportToLocationPM(player, server, world, x, y, z, yaw, pitch);
            return true;
        } else
            player.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "You are not allowed to teleport between clusters!");
        return false;
    }

    /**
     * Helper method to send the plugin message for teleporting to locations
     * @param player Player to teleport
     * @param server Server to teleport to
     * @param world World to teleport to
     * @param x X coordinate of the location
     * @param y X coordinate of the location
     * @param z Z coordinate of the location
     * @param yaw Yaw of the location
     * @param pitch Pitch of the location
     * @return True if player got teleported, false if not
     */
    private void teleportToLocationPM(ProxiedPlayer player, Server server, String world, double x, double y, double z, float yaw, float pitch) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("TeleportToPlayer");
        out.writeUTF(player.getName());
        out.writeUTF(world);
        out.writeDouble(x);
        out.writeDouble(y);
        out.writeDouble(z);
        out.writeFloat(yaw);
        out.writeFloat(pitch);
        server.sendData("ServerClusters", out.toByteArray());
    }
}
