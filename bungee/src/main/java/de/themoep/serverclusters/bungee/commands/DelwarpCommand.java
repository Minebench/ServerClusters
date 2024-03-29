package de.themoep.serverclusters.bungee.commands;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.serverclusters.bungee.WarpInfo;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;

public class DelwarpCommand extends ServerClustersCommand {

    public DelwarpCommand(ServerClusters plugin, String name) {
        super(plugin, name);
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return false;
        }

        WarpInfo warp = plugin.getWarpManager().removeWarp(sender, args[0]);
        if (warp != null) {
            sender.sendMessage(ChatColor.GREEN + "Warp " + warp.getName() + " was removed!");
        } else {
            sender.sendMessage(ChatColor.RED + "No warp with the name " + ChatColor.YELLOW + args[0] + ChatColor.RED + " found!");
        }
        return true;
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> pl = new ArrayList<>();
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
