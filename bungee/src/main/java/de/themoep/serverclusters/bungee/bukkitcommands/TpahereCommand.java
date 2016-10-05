package de.themoep.serverclusters.bungee.bukkitcommands;

import de.themoep.serverclusters.bungee.LocationInfo;
import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.serverclusters.bungee.enums.TeleportTarget;
import de.themoep.vnpbungee.VNPBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
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
        if (toTeleport != null && (plugin.getVnpbungee() == null || plugin.getVnpbungee().getVanishStatus(p) != VNPBungee.VanishStatus.VANISHED || sender.hasPermission("vanish.see"))) {
            plugin.getTeleportManager().addRequest(p, toTeleport, TeleportTarget.SENDER, location);
        } else {
            sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "The player " + args[0] + " was not found online!");
        }
    }

    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> pl = new ArrayList<String>();
        for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
            if (plugin.getVnpbungee() != null
                    && plugin.getVnpbungee().getVanishStatus(p) == VNPBungee.VanishStatus.VANISHED
                    && !sender.hasPermission("vanish.see")) {
                continue;
            }
            if (args.length == 0 || p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                pl.add(p.getName());
            }
        }
        return pl;
    }
}
