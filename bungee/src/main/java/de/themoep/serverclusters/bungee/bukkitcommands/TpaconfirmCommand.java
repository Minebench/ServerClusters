package de.themoep.serverclusters.bungee.bukkitcommands;

import de.themoep.serverclusters.bungee.LocationInfo;
import de.themoep.serverclusters.bungee.ServerClusters;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TpaconfirmCommand extends BukkitCommand {

    public TpaconfirmCommand(ServerClusters plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    @Override
    public void run(CommandSender sender, LocationInfo location, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            return;
        }
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player!");
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        if (!plugin.getTeleportManager().applyCachedRequest(player)) {
            sender.sendMessage(ChatColor.RED + "Du hast keine Anfrage die du best�tigen m�sstest!");
        }
    }

    public Iterable<String> onTabComplete(CommandSender commandSender, String[] strings) {
        return null;
    }
}