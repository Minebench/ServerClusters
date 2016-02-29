package de.themoep.serverclusters.bungee.bukkitcommands;

import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.serverclusters.bungee.enums.TeleportTarget;
import de.themoep.vnpbungee.VNPBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;


public class TpahereCommand extends BukkitCommand implements TabExecutor {

	private ServerClusters plugin;

	public TpahereCommand(ServerClusters plugin, String name, String permission) {
		super(name, permission);
		this.plugin = plugin;
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if(sender.hasPermission(getPermission())) {
			// TODO: Change messages to language system!
			if(args.length == 1) {
				if(sender instanceof ProxiedPlayer) {
					ProxiedPlayer p = (ProxiedPlayer) sender;
					ProxiedPlayer toTeleport = plugin.getProxy().getPlayer(args[0]);
					if(toTeleport == null) {
                        for(ProxiedPlayer t : plugin.getProxy().getPlayers()) {
                            if(t.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                                toTeleport = t;
                            }
                        }
                    }
					if(toTeleport != null && (plugin.getVnpbungee() == null || plugin.getVnpbungee().getVanishStatus(p) != VNPBungee.VanishStatus.VANISHED || sender.hasPermission("vanish.see"))) {
						plugin.getTeleportManager().addRequest(p, toTeleport, TeleportTarget.SENDER);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "The player " + args[0] + " was not found online!");
                    }
				} else {
                    sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "This command can only be run by a player!");
                }
			} else {
                sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.YELLOW + "/" + this.getName() + " <playername>");
            }
		}
	}

	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		List<String> pl = new ArrayList<String>();
		for(ProxiedPlayer p : plugin.getProxy().getPlayers()) {
            if(plugin.getVnpbungee() != null) {
                if(plugin.getVnpbungee().getVanishStatus(p) != VNPBungee.VanishStatus.VISIBLE && !sender.hasPermission("vanish.see")) continue;
            }
            if (args.length == 0 || p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                pl.add(p.getName());
            }
        }
		return pl;
	}
}
