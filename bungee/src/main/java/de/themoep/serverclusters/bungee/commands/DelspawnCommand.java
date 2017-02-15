package de.themoep.serverclusters.bungee.commands;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.LocationInfo;
import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.serverclusters.bungee.WarpInfo;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;

public class DelspawnCommand extends ServerClustersCommand {

    public DelspawnCommand(ServerClusters plugin, String name, String permission, String permissionMessage, String description, String usage, String... aliases) {
        super(plugin, name, permission, permissionMessage, description, usage, aliases);
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return false;
        }

        Cluster cluster = plugin.getClusterManager().getCluster(args[0]);

        if (cluster == null) {
            sender.sendMessage(ChatColor.RED + "No cluster with the name " + ChatColor.YELLOW + args[0] + ChatColor.RED + " found!");
            return true;
        }

        cluster = "--global".equalsIgnoreCase(args[0]) || "-g".equals(args[0]) ? null : cluster;

        LocationInfo loc = plugin.getSpawnManager().removeSpawn(cluster);

        if (loc != null) {
            if (cluster != null) {
                sender.sendMessage(ChatColor.GREEN + "Removed spawn for cluster " + cluster.getName());
            } else {
                sender.sendMessage(ChatColor.GREEN + "Removed global spawn " + loc);
            }
        } else {
            if (cluster != null) {
                sender.sendMessage(ChatColor.YELLOW + "There is no spawn set for cluster " + cluster.getName() + "?");
            } else {
                sender.sendMessage(ChatColor.YELLOW + "There is no global spawn?");
            }
        }
        return true;
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> pl = new ArrayList<String>();
        for (WarpInfo warp : plugin.getWarpManager().getGlobalWarps()) {
            if (args.length == 0 || warp.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                if (sender.hasPermission("serverclusters.globalwarp." + warp)) {
                    pl.add(warp.getName());
                }
            }
        }
        if (sender instanceof ProxiedPlayer) {
            Cluster cluster = plugin.getClusterManager().getPlayerCluster((ProxiedPlayer) sender);
            for (WarpInfo warp : cluster.getWarps()) {
                if (args.length == 0 || warp.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    if (sender.hasPermission("serverclusters.warp." + cluster.getName() + "." + warp)) {
                        pl.add(warp.getName());
                    }
                }
            }
        }
        return pl;
    }
}
