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
import java.util.List;


public class TpCommand extends Command implements TabExecutor {

    private ServerClusters plugin;

    public TpCommand(ServerClusters plugin, String name, String permission, String[] aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender.hasPermission(getPermission())) {
            // TODO: Change messages to language system!
            if (args.length > 0) {
                if (sender instanceof ProxiedPlayer) {
                    ProxiedPlayer player = (ProxiedPlayer) sender;
                    if (args.length > 1) {
                        player = plugin.getProxy().getPlayer(args[1]);
                        if (player == null) {
                            for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                                if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                                    player = p;
                                }
                            }
                        }
                    }
                    if (player == null) {
                        sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "The player " + args[1] + " was not found online!");
                        return;
                    }
                    ProxiedPlayer target = plugin.getProxy().getPlayer(args[0]);
                    if (target == null) {
                        for (ProxiedPlayer t : plugin.getProxy().getPlayers()) {
                            if (t.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                                target = t;
                            }
                        }
                    }
                    if (target != null) {
                        Cluster playerCluster = plugin.getClusterManager().getPlayerCluster(player);
                        Cluster targetCluster = plugin.getClusterManager().getPlayerCluster(target);
                        if (playerCluster == targetCluster || player.hasPermission("serverclusters.command.tp.intercluster")) {
                            plugin.getTeleportUtils().teleportToPlayer(player, target);
                        } else {
                            sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "You are not allowed to teleport between clusters!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "The player " + args[0] + " was not found online!");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "This command can only be run by a player!");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.YELLOW + "/" + this.getName() + " <playername> [<targetplayer>]");
            }
        }
    }

    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> pl = new ArrayList<String>();
        for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
            if (plugin.getVnpbungee() != null) {
                if (plugin.getVnpbungee().getVanishStatus(p) != VNPBungee.VanishStatus.VISIBLE && !sender.hasPermission("vanish.see"))
                    continue;
            }
            if (args.length == 0 || p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                pl.add(p.getName());
            }
        }
        return pl;
    }
}
