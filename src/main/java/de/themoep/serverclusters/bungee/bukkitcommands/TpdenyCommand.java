package de.themoep.serverclusters.bungee.bukkitcommands;

import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.vnpbungee.VNPBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;


public class TpdenyCommand extends BukkitCommand {

	private ServerClusters plugin;

	public TpdenyCommand(ServerClusters plugin, String name, String permission) {
		super(name, permission);
		this.plugin = plugin;
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if(sender.hasPermission(getPermission())) {
			if(!(sender instanceof ProxiedPlayer)) {
				sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "This command can only be run by a player!");
				return;
			}
            ProxiedPlayer p = (ProxiedPlayer) sender;
            if(args.length == 0) {
                plugin.getTeleportManager().acceptLastRequest(p);
            } else if(args.length == 1) {
                plugin.getTeleportManager().acceptRequest(p, args[0]);
			} else {
                sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.YELLOW + "/" + this.getName() + " [<playername>]");
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
