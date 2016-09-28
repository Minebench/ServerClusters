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


public class FindCommand extends Command implements TabExecutor {

    private ServerClusters plugin;

    public FindCommand(ServerClusters plugin, String name, String permission, String[] aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            for (String name : args) {
                ProxiedPlayer player = plugin.getProxy().getPlayer(name);
                if (player == null || (plugin.getVnpbungee() != null && plugin.getVnpbungee().getVanishStatus(player) != VNPBungee.VanishStatus.VISIBLE && !sender.hasPermission("vanish.see"))) {
                    sender.sendMessage(ChatColor.RED + "Kein Spieler mit dem Namen " + ChatColor.YELLOW + name + ChatColor.RED + " gefunden!");
                    return;
                }
                Cluster cluster = plugin.getClusterManager().getClusterByServer(player.getServer().getInfo().getName());
                if (cluster.isHidden() && !sender.hasPermission("serverclusters.seehidden") || !sender.hasPermission("serverclusters.cluster." + cluster.getName())) {
                    sender.sendMessage(ChatColor.RED + "Kein Spieler mit dem Namen " + ChatColor.YELLOW + name + ChatColor.RED + " gefunden!");
                    return;
                }
                sender.sendMessage(ChatColor.YELLOW + player.getDisplayName() + ChatColor.GREEN + " ist online auf " + ChatColor.YELLOW + cluster.getName());
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /" + getName() + " <username>");
        }
    }

    public Iterable<String> onTabComplete(CommandSender sender, String[] strings) {
        if (strings.length == 0) {
            List<String> playerNames = new ArrayList<String>();
            for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                if (plugin.getVnpbungee() == null || plugin.getVnpbungee().getVanishStatus(player) != VNPBungee.VanishStatus.VANISHED || sender.hasPermission("vanish.see")) {
                    playerNames.add(player.getName());
                }
            }
            Collections.sort(playerNames);
            return playerNames;
        } else if (strings.length == 1) {
            String input = strings[0].toLowerCase();
            List<String> playerNames = new ArrayList<String>();
            for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                if (!player.getName().toLowerCase().startsWith(input)) continue;
                if (plugin.getVnpbungee() == null || plugin.getVnpbungee().getVanishStatus(player) != VNPBungee.VanishStatus.VANISHED || sender.hasPermission("vanish.see")) {
                    playerNames.add(player.getName());
                }
            }
            Collections.sort(playerNames);
            return playerNames;
        }
        return null;
    }
}
