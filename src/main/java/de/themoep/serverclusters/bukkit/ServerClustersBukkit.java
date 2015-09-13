package de.themoep.serverclusters.bukkit;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.themoep.serverclusters.bukkit.manager.TeleportManager;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class ServerClustersBukkit extends JavaPlugin {

    private TeleportManager tpman;

    public void onEnable() {

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
        if(sender instanceof Player) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("RunCommand");
            out.writeUTF(sender.getName());
            out.writeUTF(cmd.getName());
            out.writeUTF(StringUtils.join(args, " "));
            ((Player) sender).sendPluginMessage(this, "ServerClusters", out.toByteArray());
            return true;
        }
        sender.sendMessage(ChatColor.RED + "This command can only be run by a player!");
        return true;
    }
}
