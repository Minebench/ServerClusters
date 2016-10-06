package de.themoep.serverclusters.bungee.commands;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.LocationInfo;
import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.serverclusters.bungee.WarpInfo;
import de.themoep.serverclusters.bungee.bukkitcommands.BukkitCommand;
import de.themoep.vnpbungee.VNPBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;

public class DelwarpCommand extends Command implements TabExecutor {

    private final ServerClusters plugin;

    public DelwarpCommand(ServerClusters plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.YELLOW + "/" + getName() + " <warp>");
            return;
        }

        WarpInfo warp = plugin.getWarpManager().removeWarp(sender, args[0]);
        if (warp != null) {

        } else {
            sender.sendMessage(ChatColor.RED + "No warp with the name " + ChatColor.YELLOW + args[0] + ChatColor.RED + " found!");
        }
    }

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
