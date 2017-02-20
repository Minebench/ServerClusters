package de.themoep.serverclusters.bungee.commands;

import de.themoep.bungeeplugin.BungeePlugin;
import de.themoep.serverclusters.bungee.Cluster;

import de.themoep.vnpbungee.VNPBungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ListCommand extends ServerClustersCommand {

    public ListCommand(BungeePlugin plugin, String name, String permission, String permissionMessage, String description, String usage, String... aliases) {
        super(plugin, name, permission, permissionMessage, description, usage, aliases);
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
        //send cluster list
        // TODO: Change messages to language system!

        List<Cluster> cl = plugin.getClusterManager().getClusterlist();
        Collections.sort(cl);

        int totalPlayers = 0;

        sender.sendMessage(new ComponentBuilder("Spieler online:").color(ChatColor.YELLOW).create());
        boolean checkVanished = plugin.getVnpbungee() != null;

        for (Cluster c : cl) {
            if (sender.hasPermission("serverclusters.cluster." + c.getName())) {
                boolean current = sender instanceof ProxiedPlayer && c.getServerlist().toString().matches(".*\\b" + ((ProxiedPlayer) sender).getServer().getInfo().getName() + "\\b.*");

                if (c.isHidden() && !current && !sender.hasPermission("serverclusters.seehidden")) {
                    continue;
                }

                List<String> clusterPlayers = new ArrayList<String>();
                for (ProxiedPlayer p : c.getPlayerlist()) {
                    String name = "";
                    if (checkVanished) {
                        if (plugin.getVnpbungee().getVanishStatus(p) == VNPBungee.VanishStatus.VANISHED) {
                            if (!sender.hasPermission("vanish.see"))
                                continue;
                            name = ChatColor.GRAY + "[Versteckt]" + ChatColor.RESET;
                        }
                    }
                    if (p == sender) {
                        name += ChatColor.ITALIC;
                    }
                    name += p.getDisplayName() + ChatColor.RESET;
                    clusterPlayers.add(name);
                }
                ;

                totalPlayers += clusterPlayers.size();

                String playerList = clusterPlayers.toString().substring(1, clusterPlayers.toString().length() - 1);

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

                if (current) {
                    msg.color(ChatColor.YELLOW);
                } else {
                    msg.color(ChatColor.GREEN);
                    msg.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cluster " + c.getName()));
                }
                msg.event(he);
                msg.append(" (" + clusterPlayers.size() + "): ").color(ChatColor.WHITE);
                msg.append(playerList).color(ChatColor.WHITE);
                sender.sendMessage(msg.create());
            }
        }
        sender.sendMessage(new ComponentBuilder("Gesamt: " + totalPlayers).create());
        return true;
    }
}
