package de.themoep.serverclusters.bungee.bukkitcommands;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.vnpbungee.VNPBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WarpCommand extends BukkitCommand {

    public WarpCommand(ServerClusters plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        if(args.length == 0) {
            Iterator<String> globalWarps = plugin.getWarpManager().getGlobalWarps().iterator();

            if(globalWarps.hasNext()) {
                sender.sendMessage(ChatColor.YELLOW + "Globale Warps:");

                ComponentBuilder builder = new ComponentBuilder(" ");
                while(globalWarps.hasNext()) {
                    String globalWarp = globalWarps.next();
                    builder.append(globalWarp).color(ChatColor.WHITE).event(
                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + globalWarp)
                    ).event(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Klicke um zu " + globalWarp + " zu warpen!"))
                    ).retain(ComponentBuilder.FormatRetention.NONE);

                    if(globalWarps.hasNext()) {
                        builder.append(", ").color(ChatColor.YELLOW);
                    }
                }
                sender.sendMessage(builder.create());
            }

            if(sender instanceof ProxiedPlayer) {
                Iterator<String> clusterWarps = plugin.getClusterManager().getPlayerCluster((ProxiedPlayer) sender).getWarps().iterator();

                if(clusterWarps.hasNext()) {
                    sender.sendMessage(ChatColor.YELLOW + "Server Warps:");

                    ComponentBuilder builder = new ComponentBuilder(" ");
                    while(clusterWarps.hasNext()) {
                        String clusterWarp = clusterWarps.next();
                        builder.append(clusterWarp).color(ChatColor.WHITE).event(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + clusterWarp)
                        ).event(
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Klicke um zu " + clusterWarp + " zu warpen!"))
                        ).retain(ComponentBuilder.FormatRetention.NONE);

                        if(clusterWarps.hasNext()) {
                            builder.append(", ").color(ChatColor.YELLOW);
                        }
                    }
                    sender.sendMessage(builder.create());
                }
            } else {
                for(Cluster cluster : plugin.getClusterManager().getClusterlist()) {
                    Iterator<String> clusterWarps = cluster.getWarps().iterator();

                    if(clusterWarps.hasNext()) {
                        sender.sendMessage(ChatColor.YELLOW + cluster.getName() + " Warps:");

                        ComponentBuilder builder = new ComponentBuilder(" ");
                        while(clusterWarps.hasNext()) {
                            String clusterWarp = clusterWarps.next();
                            builder.append(clusterWarp).color(ChatColor.WHITE).event(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + cluster.getName() + ":" + clusterWarp)
                            ).event(
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Klicke um zu " + cluster.getName() + ":" + clusterWarp + " zu warpen!"))
                            ).retain(ComponentBuilder.FormatRetention.NONE);

                            if(clusterWarps.hasNext()) {
                                builder.append(", ").color(ChatColor.YELLOW);
                            }
                        }
                        sender.sendMessage(builder.create());
                    }
                }
            }
            return;
        }

        String warpName = args[0];
        List<ProxiedPlayer> players = new ArrayList<ProxiedPlayer>();

        if(args.length == 1) {
            if(sender instanceof ProxiedPlayer) {
                players.add((ProxiedPlayer) sender);
            } else {
                sender.sendMessage(ChatColor.RED + "To run this command from the console use /warp <warpname> <playername>");
                return;
            }
        } else if(args.length >= 2) {
            for(int i = 1; i < args.length; i++) {
                ProxiedPlayer player = plugin.getProxy().getPlayer(args[i]);
                if(player != null) {
                    players.add(player);
                } else {
                    sender.sendMessage(ChatColor.RED + "No player with the name " + ChatColor.YELLOW + args[i] + ChatColor.RED + " is online?");
                }
            }
        }

        if(players.size() > 0) {
            for(ProxiedPlayer player : players) {
                plugin.getWarpManager().warp(player, warpName);
            }
        }
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
