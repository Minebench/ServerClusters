package de.themoep.serverclusters.bungee.bukkitcommands;

import de.themoep.serverclusters.bungee.LocationInfo;
import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.serverclusters.bungee.enums.TeleportTarget;
import de.themoep.vnpbungee.VNPBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TpahereCommand extends BukkitCommand {

    public TpahereCommand(ServerClusters plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    @Override
    public void run(CommandSender sender, LocationInfo location, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            return;
        }

        // TODO: Change messages to language system!
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.YELLOW + "/" + this.getName() + " <playername>");
            return;
        }

        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "This command can only be run by a player!");
            return;
        }

        ProxiedPlayer p = (ProxiedPlayer) sender;
        ProxiedPlayer toTeleport = plugin.getProxy().getPlayer(args[0]);
        if (toTeleport == null) {
            for (ProxiedPlayer t : plugin.getProxy().getPlayers()) {
                if (t.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    toTeleport = t;
                }
            }
        }
        if (toTeleport == null || (plugin.getVnpbungee() != null && plugin.getVnpbungee().getVanishStatus(toTeleport) == VNPBungee.VanishStatus.VANISHED && !sender.hasPermission("vanish.see"))) {
            plugin.getTeleportManager().addRequest(p, toTeleport, TeleportTarget.SENDER, location);
        } else {
            sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "The player " + args[0] + " was not found online!");
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
