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

public class SpawnCommand extends CooldownBukkitCommand {

    public SpawnCommand(ServerClusters plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    @Override
    public void run(CommandSender sender, LocationInfo location, String[] args) {
        Cluster cluster = null;
        if (sender instanceof ProxiedPlayer) {
            cluster = plugin.getClusterManager().getPlayerCluster((ProxiedPlayer) sender);
        }

        List<ProxiedPlayer> players = new ArrayList<>();

        if (args.length == 0) {
            if (sender instanceof ProxiedPlayer) {
                players.add((ProxiedPlayer) sender);
            } else {
                sender.sendMessage(ChatColor.RED + "To run this command from the console use /spawn -c <cluster> <playername>");
                return;
            }
        } else if (args.length >= 1) {
            if (!sender.hasPermission("serverclusters.command.spawn.others")) {
                sender.sendMessage(ChatColor.RED + "You don't have the permissions to teleport other players!");
                return;
            }
            for (int i = 0; i < args.length; i++) {
                if ("-c".equals(args[i]) || "--cluster".equalsIgnoreCase(args[i])) {
                    if (args.length > i + 1) {
                        cluster = plugin.getClusterManager().getCluster(args[i + 1]);
                        if (cluster == null) {
                            sender.sendMessage(ChatColor.RED + "No cluster with the name " + ChatColor.YELLOW + args[i + 1] + ChatColor.RED + " found?");
                            return;
                        }
                        i++;
                        continue;
                    }
                }
                ProxiedPlayer player = plugin.getProxy().getPlayer(args[i]);
                if (player != null) {
                    players.add(player);
                } else {
                    sender.sendMessage(ChatColor.RED + "No player with the name " + ChatColor.YELLOW + args[i] + ChatColor.RED + " is online?");
                }
            }
        }

        if (cluster == null) {
            sender.sendMessage(ChatColor.RED + "Don't know which cluster you mean? If you want to run it from the console use /spawn -c <cluster> <playername>");
            return;
        }

        if (players.size() > 0) {
            try {
                boolean success = false;
                for (ProxiedPlayer player : players) {
                    success = plugin.getSpawnManager().spawnPlayer(sender, player, cluster);
                }
                if (success) {
                    if (players.size() > 0 && !players.get(0).equals(sender)) {
                        sender.sendMessage(ChatColor.YELLOW + "Teleported " + (players.size() > 0 ? players.size() + " players " : players.get(0).getName()) + "!");
                    }
                } else {
                    sender.sendMessage(ChatColor.YELLOW + cluster.getName() + ChatColor.RED + " doesn't have a spawn and there is no global spawn?");
                }
            } catch (ServerNotFoundException e) {
                sender.sendMessage(ChatColor.RED + "Configuration error: " + ChatColor.YELLOW + e.getMessage());
            }
        }
    }

    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<String>();
        boolean cluster = false;
        if (args.length > 0 && "--cluster".startsWith(args[args.length - 1].toLowerCase())) {
            list.add("--cluster");
        } else if (args.length > 1) {
            if("--cluster".equalsIgnoreCase(args[args.length - 1]) || "-c".equalsIgnoreCase(args[args.length - 1])) {
                cluster = true;
                for (Cluster c : plugin.getClusterManager().getClusterlist()) {
                    if (sender.hasPermission("serverclusters.cluster." + c.getName().toLowerCase())) {
                        list.add(c.getName());
                    }
                }
            } else if("--cluster".equalsIgnoreCase(args[args.length - 2]) || "-c".equalsIgnoreCase(args[args.length - 2])) {
                cluster = true;
                for (Cluster c : plugin.getClusterManager().getClusterlist()) {
                    if (c.getName().toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                        if (sender.hasPermission("serverclusters.cluster." + c.getName().toLowerCase())) {
                            list.add(c.getName());
                        }
                    }
                }
            }
        }
        if (!cluster) {
            for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                if (plugin.getVnpbungee() != null
                        && plugin.getVnpbungee().getVanishStatus(p) == VNPBungee.VanishStatus.VANISHED
                        && !sender.hasPermission("vanish.see")) {
                    continue;
                }
                if (args.length == 0 || p.getName().toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                    list.add(p.getName());
                }
            }
        }
        return list;
    }
}
