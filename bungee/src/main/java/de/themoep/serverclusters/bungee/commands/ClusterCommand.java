package de.themoep.serverclusters.bungee.commands;

import de.themoep.serverclusters.bungee.Cluster;

import de.themoep.serverclusters.bungee.ServerClusters;
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

    public ClusterCommand(ServerClusters plugin, String name) {
        super(plugin, name);
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
                if (c.canSee(sender)) {
                    boolean current = (sender instanceof ProxiedPlayer && c.getServerlist().toString().matches(".*\\b" + ((ProxiedPlayer) sender).getServer().getInfo().getName() + "\\b.*"));

                    if (c.isHidden() && !current && !sender.hasPermission("serverclusters.seehidden") && !sender.hasPermission(c.getPermission() + ".see")) {
                        continue;
                    }

                    ComponentBuilder msg = new ComponentBuilder(" ");

                    HoverEvent he;
                    if (current) {
                        msg.append(ChatColor.RED + ">").bold(true).color(ChatColor.RED);
                        he = new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("Hier befindest du dich!").color(ChatColor.BLUE)
                                        .create()
                        );
                        msg.event(he);
                    } else if (c.hasAccess(sender)) {
                        he = new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("Klicke zum Betreten von ").italic(true)
                                        .append(c.getName()).color(ChatColor.GREEN)
                                        .append("!").reset().italic(true)
                                        .create()
                        );
                    } else {
                        he = new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("Du darfst ").color(ChatColor.RED)
                                        .append(c.getName()).color(ChatColor.YELLOW)
                                        .append(" nicht betreten!").color(ChatColor.RED)
                                        .create()
                        );
                    }

                    msg.append(c.getName()).bold(false);

                    ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cluster " + c.getName());
                    if (current) {
                        msg.color(ChatColor.YELLOW);
                    } else if (c.hasAccess(sender)) {
                        msg.color(ChatColor.GREEN);
                        msg.event(ce);
                    } else {
                        msg.color(ChatColor.RED);
                    }
                    msg.event(he);
                    int playerCount = 0;
                    for (ProxiedPlayer p : c.getPlayerlist()) {
                        if ((!plugin.shouldHideVanished() || plugin.getVnpbungee() == null || plugin.getVnpbungee().canSee(sender, p))
                                && (!p.hasPermission("serverclusters.list-extra") || sender.hasPermission("serverclusters.list-extra"))) {
                            playerCount++;
                        }
                    }
                    msg.append(" - " + playerCount + " Spieler").color(ChatColor.WHITE);
                    msg.event(he);
                    if (!current && c.hasAccess(sender)) msg.event(ce);
                    sender.sendMessage(msg.create());
                }
            }

        } else if (args.length == 1 && sender instanceof ProxiedPlayer) {

            Cluster targetCluster = plugin.getClusterManager().getCluster(args[0]);
            if (targetCluster == null || !targetCluster.hasAccess(sender)) {
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
            if (targetCluster == null || !targetCluster.hasAccess(sender)) {
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
