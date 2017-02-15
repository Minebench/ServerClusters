package de.themoep.serverclusters.bungee.commands;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.ServerClusters;

import de.themoep.vnpbungee.VNPBungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TpCommand extends ServerClustersCommand implements TabExecutor {

    public TpCommand(ServerClusters plugin, String name, String permission, String permissionMessage, String description, String usage, String... aliases) {
        super(plugin, name, permission, permissionMessage, description, usage, aliases);
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
        // TODO: Change messages to language system!
        if (args.length == 0) {
            return false;
        }

        ProxiedPlayer player;
        if (args.length > 1) {
            player = plugin.getProxy().getPlayer(args[1]);
            if (player == null) {
                for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        player = p;
                    }
                }
            }
        } else if (sender instanceof ProxiedPlayer) {
            player = (ProxiedPlayer) sender;
        } else {
            sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "To run this command from the console use /" + getName() + " <playername> <targetplayer>");
            return true;
        }

        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "The player " + args[1] + " was not found online!");
            return true;
        }

        ProxiedPlayer target = plugin.getProxy().getPlayer(args[0]);
        if (target == null) {
            for (ProxiedPlayer t : plugin.getProxy().getPlayers()) {
                if (t.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    target = t;
                }
            }
        }

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "The player " + args[0] + " was not found online!");
            return true;
        }

        Cluster playerCluster = plugin.getClusterManager().getPlayerCluster(player);
        Cluster targetCluster = plugin.getClusterManager().getPlayerCluster(target);
        if (playerCluster == targetCluster || !(sender instanceof ProxiedPlayer) || player.hasPermission("serverclusters.command.tp.intercluster")) {
            plugin.getTeleportUtils().teleportToPlayer(player, target);
        } else {
            sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "You are not allowed to teleport between clusters!");
        }
        return true;
    }

    public Iterable<String> onTabComplete(CommandSender sender, String[] strings) {
        List<String> playerNames = new ArrayList<>();
        if (strings.length == 0) {
            for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                if (plugin.getVnpbungee() == null || plugin.getVnpbungee().getVanishStatus(player) != VNPBungee.VanishStatus.VANISHED || sender.hasPermission("vanish.see")) {
                    playerNames.add(player.getName());
                }
            }
            Collections.sort(playerNames);
        } else if (strings.length == 1) {
            String input = strings[0].toLowerCase();
            for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                if (!player.getName().toLowerCase().startsWith(input)) continue;
                if (plugin.getVnpbungee() == null || plugin.getVnpbungee().getVanishStatus(player) != VNPBungee.VanishStatus.VANISHED || sender.hasPermission("vanish.see")) {
                    playerNames.add(player.getName());
                }
            }
            Collections.sort(playerNames);
        } else if (strings.length == 2) {
            String input = strings[1].toLowerCase();
            for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                if (!player.getName().toLowerCase().startsWith(input)) continue;
                if (plugin.getVnpbungee() == null || plugin.getVnpbungee().getVanishStatus(player) != VNPBungee.VanishStatus.VANISHED || sender.hasPermission("vanish.see")) {
                    playerNames.add(player.getName());
                }
            }
            Collections.sort(playerNames);
        }
        return playerNames;
    }
}
