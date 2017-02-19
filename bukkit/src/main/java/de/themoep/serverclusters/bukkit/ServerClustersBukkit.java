package de.themoep.serverclusters.bukkit;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.themoep.serverclusters.bukkit.manager.TeleportManager;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ServerClustersBukkit extends JavaPlugin {

    private TeleportManager tpman;
    private int teleportDelay;
    private boolean debug;

    public void onEnable() {

        teleportDelay = getConfig().getInt("teleportDelay", -1);
        if (teleportDelay == -1) {
            teleportDelay = getConfig().getDefaults().getInt("teleportDelay");
            getConfig().set("teleportDelay", teleportDelay);
            saveConfig();
        }
        debug = getConfig().getBoolean("debug", true);

        getLogger().log(Level.INFO, "Initialising Teleport Manager");
        tpman = new TeleportManager(this);

        getLogger().log(Level.INFO, "Registering Plugin Message Channel");
        getServer().getMessenger().registerIncomingPluginChannel(this, "ServerClusters", new BungeePluginMessageListener(this));
        getServer().getMessenger().registerOutgoingPluginChannel(this, "ServerClusters");

        getLogger().log(Level.INFO, "Registering Event Listener");
        getServer().getPluginManager().registerEvents(getTeleportManager(), this);
    }

    /**
     * Get the teleport manager.
     */
    public TeleportManager getTeleportManager() {
        return tpman;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player;
        String senderName = sender.getName();
        if (sender instanceof Player) {
            player = (Player) sender;
            if (("warp".equals(cmd.getName()) || "spawn".equals(cmd.getName())) && getTeleportDelay() > 0 && !player.hasPermission("serverclusters.bypass.delay")) {
                getTeleportManager().addRequest(player.getUniqueId(), System.currentTimeMillis());
            }
        } else if (getServer().getOnlinePlayers().size() > 0) {
            senderName = "[@]";
            player = getServer().getOnlinePlayers().iterator().next();
        } else {
            sender.sendMessage(ChatColor.RED + "This command can only be run with at least one player online as it relies on plugin messages!");
            return true;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("RunCommand");
        out.writeUTF(senderName);
        out.writeUTF(cmd.getName());
        if (sender instanceof Entity) {
            out.writeBoolean(true);
            out.writeUTF(((Entity) sender).getLocation().getWorld().getName());
            out.writeDouble(((Entity) sender).getLocation().getX());
            out.writeDouble(((Entity) sender).getLocation().getY());
            out.writeDouble(((Entity) sender).getLocation().getZ());
            out.writeFloat(((Entity) sender).getLocation().getYaw());
            out.writeFloat(((Entity) sender).getLocation().getPitch());
        } else if (sender instanceof BlockCommandSender) {
            out.writeBoolean(true);
            out.writeUTF(((BlockCommandSender) sender).getBlock().getLocation().getWorld().getName());
            out.writeDouble(((BlockCommandSender) sender).getBlock().getLocation().getX());
            out.writeDouble(((BlockCommandSender) sender).getBlock().getLocation().getY());
            out.writeDouble(((BlockCommandSender) sender).getBlock().getLocation().getZ());
            out.writeFloat(((BlockCommandSender) sender).getBlock().getLocation().getYaw());
            out.writeFloat(((BlockCommandSender) sender).getBlock().getLocation().getPitch());
        } else {
            out.writeBoolean(false);
        }
        out.writeUTF(StringUtils.join(args, " "));
        player.sendPluginMessage(this, "ServerClusters", out.toByteArray());
        return true;
    }

    public int getTeleportDelay() {
        return teleportDelay;
    }

    public void debug(String message) {
        if (debug) {
            getLogger().log(Level.INFO, "[Debug] " + message);
        }
    }
}
