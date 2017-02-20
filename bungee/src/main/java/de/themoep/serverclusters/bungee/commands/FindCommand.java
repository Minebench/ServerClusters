package de.themoep.serverclusters.bungee.commands;

import de.themoep.bungeeplugin.BungeePlugin;
import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.vnpbungee.VNPBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FindCommand extends ServerClustersCommand {

    public FindCommand(BungeePlugin plugin, String name, String permission, String permissionMessage, String description, String usage, String... aliases) {
        super(plugin, name, permission, permissionMessage, description, usage, aliases);
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return false;
        }

        for (String name : args) {
            ProxiedPlayer player = plugin.getProxy().getPlayer(name);
            if (player == null || (plugin.getVnpbungee() != null && plugin.getVnpbungee().getVanishStatus(player) != VNPBungee.VanishStatus.VISIBLE && !sender.hasPermission("vanish.see"))) {
                sender.sendMessage(ChatColor.RED + "Kein Spieler mit dem Namen " + ChatColor.YELLOW + name + ChatColor.RED + " gefunden!");
                continue;
            }
            Cluster cluster = plugin.getClusterManager().getClusterByServer(player.getServer().getInfo().getName());
            if (cluster.isHidden() && !sender.hasPermission("serverclusters.seehidden") || !sender.hasPermission("serverclusters.cluster." + cluster.getName())) {
                sender.sendMessage(ChatColor.RED + "Kein Spieler mit dem Namen " + ChatColor.YELLOW + name + ChatColor.RED + " gefunden!");
                continue;
            }
            sender.sendMessage(ChatColor.YELLOW + player.getDisplayName() + ChatColor.GREEN + " ist online auf " + ChatColor.YELLOW + cluster.getName());
        }
        return true;
    }

    @Override
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
        }
        return playerNames;
    }
}
