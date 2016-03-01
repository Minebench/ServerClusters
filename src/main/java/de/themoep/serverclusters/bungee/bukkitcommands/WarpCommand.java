package de.themoep.serverclusters.bungee.bukkitcommands;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.vnpbungee.VNPBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;

public class WarpCommand extends BukkitCommand {

    public WarpCommand(ServerClusters plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    @Override
    public void run(CommandSender sender, String[] args) {

    }

    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> pl = new ArrayList<String>();
        if(args.length > 0) {
            for(ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                if(plugin.getVnpbungee() != null) {
                    if(plugin.getVnpbungee().getVanishStatus(p) != VNPBungee.VanishStatus.VISIBLE && !sender.hasPermission("vanish.see"))
                        continue;
                }
                if(args.length == 1 || p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    pl.add(p.getName());
                }
            }
        }
        if(args.length < 2) {
            for(String warp : plugin.getWarpManager().getGlobalWarps()) {
                if(args.length == 0 || warp.toLowerCase().startsWith(args[0].toLowerCase())) {
                    if(sender.hasPermission("serverclusters.globalwarp." + warp)) {
                        pl.add(warp);
                    }
                }
            }
            if(sender instanceof ProxiedPlayer) {
                Cluster cluster = plugin.getClusterManager().getPlayerCluster((ProxiedPlayer) sender);
                for(String warp : cluster.getWarps()) {
                    if(args.length == 0 || warp.toLowerCase().startsWith(args[0].toLowerCase())) {
                        if(sender.hasPermission("serverclusters.warp." + cluster.getName() + "." + warp)) {
                            pl.add(warp);
                        }
                    }
                }
            }
        }
        return pl;
    }
}
