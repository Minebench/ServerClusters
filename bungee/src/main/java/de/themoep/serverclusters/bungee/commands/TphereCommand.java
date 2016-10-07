package de.themoep.serverclusters.bungee.commands;

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


public class TphereCommand extends Command implements TabExecutor {

    private ServerClusters plugin;

    public TphereCommand(ServerClusters plugin, String name, String permission, String[] aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
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
        if (toTeleport == null) {
            sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "The player " + args[0] + " was not found online!");
            return;
        }

        plugin.getTeleportUtils().teleportToPlayer(toTeleport, p);
        sender.sendMessage(ChatColor.YELLOW + toTeleport.getName() + ChatColor.GREEN + " zu dir teleportiert");
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
        }
        return playerNames;
    }
}
