package de.themoep.serverclusters.bungee.bukkitcommands;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.LocationInfo;
import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.serverclusters.bungee.ServerNotFoundException;
import de.themoep.serverclusters.bungee.WarpInfo;
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

public class WarpCommand extends CooldownBukkitCommand {

    public WarpCommand(ServerClusters plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    @Override
    public void run(CommandSender sender, LocationInfo location, String[] args) {
        if (args.length == 0) {
            Iterator<WarpInfo> globalWarps = plugin.getWarpManager().getGlobalWarps(sender).iterator();

            if (globalWarps.hasNext()) {
                sender.sendMessage(ChatColor.YELLOW + "Globale Warps:");

                ComponentBuilder builder = new ComponentBuilder(" ");
                while (globalWarps.hasNext()) {
                    WarpInfo globalWarp = globalWarps.next();
                    builder.append(globalWarp.getName()).color(ChatColor.WHITE).event(
                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + globalWarp.getName())
                    ).event(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                                    ChatColor.BLUE + "Klicke um zu " + ChatColor.YELLOW + globalWarp.getName() + ChatColor.BLUE + " zu warpen!"
                            ))
                    );

                    if (globalWarps.hasNext()) {
                        builder.append(", ").retain(ComponentBuilder.FormatRetention.NONE).color(ChatColor.YELLOW);
                    }
                }
                sender.sendMessage(builder.create());
            }

            if (sender instanceof ProxiedPlayer && !sender.hasPermission("serverclusters.command.warp.intercluster")) {
                Iterator<WarpInfo> clusterWarps = plugin.getClusterManager().getPlayerCluster((ProxiedPlayer) sender).getWarps(sender).iterator();

                if (clusterWarps.hasNext()) {
                    sender.sendMessage(ChatColor.YELLOW + "Server Warps:");

                    ComponentBuilder builder = new ComponentBuilder(" ");
                    while (clusterWarps.hasNext()) {
                        WarpInfo clusterWarp = clusterWarps.next();
                        String clusterStr = "";
                        Cluster cluster = plugin.getClusterManager().getClusterByServer(clusterWarp.getServer());
                        if (cluster != null) {
                            clusterStr = cluster.getName() + ":";
                        }
                        builder.append(clusterWarp.getName()).color(ChatColor.WHITE).event(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + clusterStr + clusterWarp.getName())
                        ).event(
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                                        ChatColor.BLUE + "Klicke um zu " + ChatColor.YELLOW + clusterWarp.getName() + ChatColor.BLUE + " zu warpen!"
                                ))
                        );

                        if (clusterWarps.hasNext()) {
                            builder.append(", ").retain(ComponentBuilder.FormatRetention.NONE).color(ChatColor.YELLOW);
                        }
                    }
                    sender.sendMessage(builder.create());
                }
            } else {
                for (Cluster cluster : plugin.getClusterManager().getClusterlist()) {
                    Iterator<WarpInfo> clusterWarps = cluster.getWarps(sender).iterator();

                    if (clusterWarps.hasNext()) {
                        sender.sendMessage(ChatColor.YELLOW + cluster.getName() + " Warps:");

                        ComponentBuilder builder = new ComponentBuilder(" ");
                        while (clusterWarps.hasNext()) {
                            WarpInfo clusterWarp = clusterWarps.next();
                            builder.append(clusterWarp.getName()).color(ChatColor.WHITE).event(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + cluster.getName() + ":" + clusterWarp.getName())
                            ).event(
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                                            ChatColor.BLUE + "Klicke um zu " + ChatColor.YELLOW + cluster.getName() + ":" + clusterWarp.getName() + ChatColor.BLUE + " zu warpen!"
                                    ))
                            );

                            if (clusterWarps.hasNext()) {
                                builder.append(", ").retain(ComponentBuilder.FormatRetention.NONE).color(ChatColor.YELLOW);
                            }
                        }
                        sender.sendMessage(builder.create());
                    }
                }
            }
            return;
        }

        WarpInfo warp = plugin.getWarpManager().getWarp(args[0]);
        if (warp == null && sender instanceof ProxiedPlayer) {
            Cluster cluster = plugin.getClusterManager().getPlayerCluster((ProxiedPlayer) sender);
            if (cluster != null) {
                warp = cluster.getWarp(args[0]);
            }
        }

        if (warp == null || !plugin.getWarpManager().checkAccess(sender, warp)) {
            sender.sendMessage(ChatColor.YELLOW + "Der Warp " + ChatColor.RED + args[0] + ChatColor.YELLOW + " existiert nicht.");
            return;
        }

        List<ProxiedPlayer> players = new ArrayList<>();

        if (args.length == 1) {
            if (sender instanceof ProxiedPlayer) {
                players.add((ProxiedPlayer) sender);
            } else {
                sender.sendMessage(ChatColor.RED + "To run this command from the console use /warp <warpname> <playername>");
                return;
            }
        } else if (args.length >= 2) {
            if (!sender.hasPermission("serverclusters.command.warp.others")) {
                sender.sendMessage(ChatColor.RED + "You don't have the permissions to teleport other players!");
                return;
            }
            for (int i = 1; i < args.length; i++) {
                ProxiedPlayer player = plugin.getProxy().getPlayer(args[i]);
                if (player != null) {
                    players.add(player);
                } else {
                    sender.sendMessage(ChatColor.RED + "No player with the name " + ChatColor.YELLOW + args[i] + ChatColor.RED + " is online?");
                }
            }
        }

        if (players.size() > 0) {
            try {
                for (ProxiedPlayer player : players) {
                    plugin.getWarpManager().warpPlayer(sender, player, warp);
                }
            } catch (ServerNotFoundException e) {
                if (args.length >= 2) {
                    sender.sendMessage(ChatColor.RED + "Configuration error: " + ChatColor.YELLOW + e.getMessage());
                }
            }
        }
    }

    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> pl = new ArrayList<String>();
        if (args.length > 1) {
            for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                if (plugin.getVnpbungee() != null
                        && plugin.getVnpbungee().getVanishStatus(p) == VNPBungee.VanishStatus.VANISHED
                        && !sender.hasPermission("vanish.see")) {
                    continue;
                }
                if (args.length == 2 || p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    pl.add(p.getName());
                }
            }
        } else if (args.length < 2) {
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
        }
        return pl;
    }
}
