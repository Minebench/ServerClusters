package de.themoep.serverclusters.bungee.commands;

import de.themoep.bungeeplugin.BungeePlugin;
import de.themoep.serverclusters.bungee.Cluster;

import de.themoep.vnpbungee.VNPBungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class ClusterCommand extends ServerClustersCommand {

    public ClusterCommand(BungeePlugin plugin, String name, String permission, String permissionMessage, String description, String usage, String... aliases) {
        super(plugin, name, permission, permissionMessage, description, usage, aliases);
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
        if (args.length == 0) {
            //send cluster list
            // TODO: Change messages to language system!
            sender.sendMessage(new ComponentBuilder("Verfügbare Server:").color(ChatColor.YELLOW).create());

            List<Cluster> cl = plugin.getClusterManager().getClusterlist();
            Collections.sort(cl);

            for (Cluster c : cl) {
                if (sender.hasPermission("serverclusters.cluster." + c.getName())) {
                    boolean current = (sender instanceof ProxiedPlayer && c.getServerlist().toString().matches(".*\\b" + ((ProxiedPlayer) sender).getServer().getInfo().getName() + "\\b.*"));

                    if (c.isHidden() && !current && !sender.hasPermission("serverclusters.seehidden")) {
                        continue;
                    }

                    ComponentBuilder msg = new ComponentBuilder(" ");

                    HoverEvent he;
                    if (current) {
                        msg.append(ChatColor.RED + ">").bold(true).color(ChatColor.RED);
                        he = new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("Hier befindest du dich!")
                                        .create()
                        );
                        msg.event(he);
                    } else {
                        he = new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("Klicke zum Beitreten von ").italic(true)
                                        .append(c.getName()).color(ChatColor.GREEN)
                                        .append("!").color(ChatColor.RESET).italic(true)
                                        .create()
                        );
                    }

                    msg.append(c.getName()).bold(false);

                    ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cluster " + c.getName());
                    if (current) {
                        msg.color(ChatColor.YELLOW);
                    } else {
                        msg.color(ChatColor.GREEN);
                        msg.event(ce);
                    }
                    msg.event(he);
                    int playerCount = 0;
                    if (!sender.hasPermission("vanish.see") && plugin.getVnpbungee() != null) {
                        VNPBungee vnpBungee = (VNPBungee) ProxyServer.getInstance().getPluginManager().getPlugin("VNPBungee");
                        for (ProxiedPlayer p : c.getPlayerlist()) {
                            if (vnpBungee.getVanishStatus(p) != VNPBungee.VanishStatus.VANISHED) {
                                playerCount++;
                            }
                        }
                    } else {
                        playerCount = c.getPlayerlist().size();
                    }
                    msg.append(" - " + playerCount + " Spieler").color(ChatColor.WHITE);
                    msg.event(he);
                    if (!current) msg.event(ce);
                    sender.sendMessage(msg.create());
                }
            }

        } else if (args.length == 1 && sender instanceof ProxiedPlayer) {

            Cluster targetCluster = plugin.getClusterManager().getCluster(args[0]);
            if (targetCluster == null || !sender.hasPermission("serverclusters.cluster." + targetCluster.getName().toLowerCase())) {
                // ERROR no perms
                // ERROR Cluster not found
                sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "Cluster " + args[0] + " not found!");
                return true;
            }

            // connect player to cluster
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if (targetCluster == plugin.getClusterManager().getClusterByServer(p.getServer().getInfo().getName())) {
                sender.sendMessage(ChatColor.RED + "Du bist bereits auf " + ChatColor.YELLOW + targetCluster.getName() + ChatColor.RED + "!");
            } else {
                p.sendMessage(ChatColor.GREEN + "Verbinde mit " + ChatColor.YELLOW + targetCluster.getName() + ChatColor.GREEN + "...");
                targetCluster.connectPlayer(p);
            }
        } else {
            if (!sender.hasPermission("serverclusters.command.cluster.others")) {
                // ERROR no perms
                sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "You don't have the permission serverclusters.command.cluster.others");
                return true;
            }

            Cluster targetCluster = plugin.getClusterManager().getCluster(args[0]);
            if (targetCluster == null || !sender.hasPermission("serverclusters.cluster." + targetCluster.getName().toLowerCase())) {
                // ERROR Cluster not found
                sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "Cluster " + args[0] + " not found!");
                return true;
            }

            ArrayList<String> playerlist = new ArrayList<>(Arrays.asList(args));
            playerlist.remove(0);
            for (String playername : playerlist) {
                ProxiedPlayer p = plugin.getProxy().getPlayer(playername);
                if (p == null) {
                    sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "Player " + ChatColor.RED + playername + ChatColor.YELLOW + " is not online!");
                } else if (targetCluster == plugin.getClusterManager().getClusterByServer(p.getServer().getInfo().getName())) {
                    sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "Player " + ChatColor.RED + playername + ChatColor.YELLOW + " is already on " + ChatColor.RED + targetCluster.getName() + ChatColor.YELLOW + "!");
                } else {
                    p.sendMessage(ChatColor.GREEN + "Verbinde " + ChatColor.YELLOW + playername + ChatColor.GREEN + " mit " + ChatColor.YELLOW + targetCluster.getName() + ChatColor.GREEN + "...");
                    targetCluster.connectPlayer(p);
                }
            }
        }
        return true;
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> cl = new ArrayList<>();
        for (Cluster c : plugin.getClusterManager().getClusterlist())
            if (args.length == 0 || c.getName().toLowerCase().startsWith(args[0].toLowerCase()))
                if (sender.hasPermission("serverclusters.cluster." + c.getName().toLowerCase()))
                    cl.add(c.getName());
        return cl;
    }


}
