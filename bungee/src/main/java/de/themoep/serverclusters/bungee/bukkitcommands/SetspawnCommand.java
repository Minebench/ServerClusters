package de.themoep.serverclusters.bungee.bukkitcommands;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.LocationInfo;
import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.serverclusters.bungee.WarpInfo;
import de.themoep.vnpbungee.VNPBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;

public class SetspawnCommand extends BukkitCommand {

    public SetspawnCommand(ServerClusters plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    @Override
    public void run(CommandSender sender, LocationInfo location, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "This command can only be run by a player!");
            return;
        }

        try {
            boolean global = args.length > 0 && "global".equalsIgnoreCase(args[0]);
            LocationInfo spawn = plugin.getSpawnManager().setSpawn(location, global);
            Cluster cluster = plugin.getClusterManager().getClusterByServer(spawn.getServer());
            sender.sendMessage(ChatColor.GREEN + "Set " + (global ? "global spawn" : "spawn of " + cluster.getName()) + " to your location!");
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }

    }

    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> pl = new ArrayList<String>();
        if (args.length == 0 || "global".startsWith(args[0].toLowerCase())) {
            pl.add("global");
        }
        return pl;
    }
}
