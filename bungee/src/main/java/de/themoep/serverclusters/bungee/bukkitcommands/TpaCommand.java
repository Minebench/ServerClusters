package de.themoep.serverclusters.bungee.bukkitcommands;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.LocationInfo;
import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.serverclusters.bungee.enums.TeleportTarget;
import de.themoep.vnpbungee.VNPBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;


public class TpaCommand extends BukkitCommand {

	public TpaCommand(ServerClusters plugin, String name, String permission) {
		super(plugin, name, permission);
	}

	@Override
	public void run(CommandSender sender, LocationInfo location, String[] args) {
		if(sender.hasPermission(getPermission())) {
			// TODO: Change messages to language system!
			if(args.length == 1) {
				if(sender instanceof ProxiedPlayer) {
					ProxiedPlayer p = (ProxiedPlayer) sender;
					ProxiedPlayer target = plugin.getProxy().getPlayer(args[0]);
					if(target == null) {
                        for(ProxiedPlayer t : plugin.getProxy().getPlayers()) {
                            if(t.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                                target = t;
                            }
                        }
                    }
					if(target != null && (plugin.getVnpbungee() == null || plugin.getVnpbungee().getVanishStatus(p) != VNPBungee.VanishStatus.VANISHED || sender.hasPermission("vanish.see"))) {
						Cluster senderCluster = plugin.getClusterManager().getPlayerCluster(p);
                        if(senderCluster == null) {
                            sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "On what cluster are you on? Oo");
                            return;
                        }
                        Cluster targetCluster = plugin.getClusterManager().getPlayerCluster(target);
                        if(targetCluster == null) {
                            sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "On what cluster is "+ target.getName() + " on? Oo");
                            return;
                        }

                        if(senderCluster.equals(targetCluster)) {
                            plugin.getTeleportManager().addRequest(p, target, TeleportTarget.RECEIVER, location);
                        } else {
                            if(sender.hasPermission("serverclusters.command.tpa.intercluster")) {
                                if(sender.hasPermission("serverclusters.cluster." + targetCluster.getName())) {
                                    if(sender.hasPermission("serverclusters.command.tpa.intercluster.nowarning")) {
                                        plugin.getTeleportManager().addRequest(p, target, TeleportTarget.RECEIVER, location);
                                    } else {
                                        plugin.getTeleportManager().cacheRequest(p, target, TeleportTarget.RECEIVER, location);
                                        p.sendMessage(new ComponentBuilder(target.getName()).color(ChatColor.RED)
                                                        .append("befindet sich auf einem anderen Server als du! (" + targetCluster.getName() + ")").color(ChatColor.YELLOW)
                                                        .create()
                                        );
                                        p.sendMessage(new ComponentBuilder("Nutze ").color(ChatColor.YELLOW)
                                                        .append("/tpaconfirm").color(ChatColor.RED).event(
                                                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaconfirm")
                                                        ).event(
                                                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Klicke um /tpaconfirm auszuführen!"))
                                                        ).retain(ComponentBuilder.FormatRetention.NONE)
                                                        .append(" um trotzdem eine Anfrage zu senden und den Server automatisch zu wechseln sobald deine Anfragen angenommen wird!").color(ChatColor.YELLOW)
                                                        .create()
                                        );
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "You don't have permission to teleport to the server " + target.getName() + " is on!");
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "You don't have permission to teleport between server! Please use /server to switch before you teleport!");
                            }
                        }
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
