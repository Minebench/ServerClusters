package de.themoep.serverclusters.bungee.commands;

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
import java.util.Collections;
import java.util.List;


public class ListCommand extends ServerClustersCommand {

    public ListCommand(ServerClusters plugin, String name) {
        super(plugin, name);
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
        //send cluster list
        // TODO: Change messages to language system!

        List<Cluster> cl = plugin.getClusterManager().getClusterlist();
        Collections.sort(cl);

        boolean checkVanished = plugin.getVnpbungee() != null;

        List<String> extraPlayers = new ArrayList<>();
        for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
            if (p.hasPermission("serverclusters.list-extra")) {
                String name = "";
                if (checkVanished) {
                    if (plugin.getVnpbungee().getVanishStatus(p) == VNPBungee.VanishStatus.VANISHED) {
                        if (sender.hasPermission("vanish.see")) {
                            name = ChatColor.GRAY + "[Versteckt]" + ChatColor.RESET;
                        } else if (plugin.shouldHideVanished()) {
                            continue;
                        }
                    }
                }
                if (p == sender) {
                    name += ChatColor.ITALIC;
                }
                name += p.getName() + ChatColor.RESET;
                extraPlayers.add(name);
            }
        }

        if (!extraPlayers.isEmpty()) {
            sender.sendMessage(new ComponentBuilder("Teamler online:").color(ChatColor.YELLOW).create());
            if (!extraPlayers.isEmpty()) {
                sender.sendMessage(TextComponent.fromLegacyText(" " + String.join(", ", extraPlayers)));
            } else {
                sender.sendMessage(TextComponent.fromLegacyText(" - "));
            }
        }

        int totalPlayers = extraPlayers.size();

        sender.sendMessage(new ComponentBuilder("Spieler online:").color(ChatColor.YELLOW).create());

        for (Cluster c : cl) {
            if (c.canSee(sender)) {
                boolean current = sender instanceof ProxiedPlayer && c.getServerlist().toString().matches(".*\\b" + ((ProxiedPlayer) sender).getServer().getInfo().getName() + "\\b.*");

                if (c.isHidden() && !current && !sender.hasPermission("serverclusters.seehidden") && !sender.hasPermission(c.getPermission() + ".see")) {
                    continue;
                }

                List<String> clusterPlayers = new ArrayList<>();
                for (ProxiedPlayer p : c.getPlayerlist()) {
                    String name = "";
                    if (p.hasPermission("serverclusters.list-extra") && !sender.hasPermission("serverclusters.list-extra")) {
                        continue;
                    }
                    if (checkVanished) {
                        if (plugin.getVnpbungee().getVanishStatus(p) == VNPBungee.VanishStatus.VANISHED) {
                            if (sender.hasPermission("vanish.see")) {
                                name = ChatColor.GRAY + "[Versteckt]" + ChatColor.RESET;
                            } else if (plugin.shouldHideVanished()) {
                                continue;
                            }
                        }
                    }
                    if (p == sender) {
                        name += ChatColor.ITALIC;
                    }
                    name += p.getName() + ChatColor.RESET;
                    clusterPlayers.add(name);
                }

                totalPlayers += clusterPlayers.size();

                String playerList = String.join(", ", clusterPlayers);

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
                } else if (c.hasAccess(sender)) {
                    he = new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("Klicke zum Beitreten von ").italic(true)
                                    .append(c.getName()).color(ChatColor.GREEN)
                                    .append("!").color(ChatColor.RESET).italic(true)
                                    .create()
                    );
                } else {
                    he = new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("Du darfst ").italic(true)
                                    .append(c.getName()).color(ChatColor.RED)
                                    .append(" nicht beitreten!").color(ChatColor.RESET).italic(true)
                                    .create()
                    );
                }
                msg.append(c.getName()).bold(false);

                if (current) {
                    msg.color(ChatColor.YELLOW);
                } else {
                    msg.color(ChatColor.GREEN);
                    msg.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cluster " + c.getName()));
                }
                msg.event(he);
                msg.append(" (" + clusterPlayers.size() + "): ").color(ChatColor.WHITE);
                msg.append(TextComponent.fromLegacyText(playerList), ComponentBuilder.FormatRetention.EVENTS);
                sender.sendMessage(msg.create());
            }
        }
        sender.sendMessage(new ComponentBuilder("Gesamt: " + totalPlayers).create());
        return true;
    }
}
