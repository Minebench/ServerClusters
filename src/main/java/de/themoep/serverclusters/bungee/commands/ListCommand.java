package de.themoep.serverclusters.bungee.commands;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.ServerClusters;

import de.themoep.vnpbungee.VNPBungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ListCommand extends Command implements TabExecutor {

    private ServerClusters plugin;

    public ListCommand(ServerClusters plugin, String name, String permission, String[] aliases) {
		super(name, permission, aliases);
        this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {		
		if(sender.hasPermission("serverclusters.command.clist")) {
			if(args.length == 0) {
				//send cluster list
				// TODO: Change messages to language system!

				List<Cluster> cl = plugin.getClusterManager().getClusterlist();
				Collections.sort(cl);

				int totalPlayers = 0;

				sender.sendMessage(new ComponentBuilder("Spieler online:").color(ChatColor.YELLOW).create());
                boolean checkVanished = plugin.getVnpbungee() != null;
                boolean senderSeeUnknown = true;
                if(checkVanished) {
                    senderSeeUnknown = sender instanceof ProxiedPlayer && plugin.getVnpbungee().getVanishStatus((ProxiedPlayer) sender) != VNPBungee.VanishStatus.VISIBLE;
                }
                
				for(Cluster c : cl) {
					if(sender.hasPermission("serverclusters.cluster." + c.getName())) {
                        boolean current = sender instanceof ProxiedPlayer && c.getServerlist().toString().matches(".*\\b" + ((ProxiedPlayer) sender).getServer().getInfo().getName() + "\\b.*");

                        if(c.isHidden() && !current && !sender.hasPermission("serverclusters.seehidden")) {
                            continue;
                        }

                        List<String> clusterPlayers = new ArrayList<String>();
                        for(ProxiedPlayer p : c.getPlayerlist()) {
                            String name = "";
                            if(checkVanished) {
                                VNPBungee.VanishStatus vStatus = plugin.getVnpbungee().getVanishStatus(p);
                                if (!(senderSeeUnknown && vStatus == VNPBungee.VanishStatus.UNKNOWN) && vStatus != VNPBungee.VanishStatus.VISIBLE) {
                                    if (!sender.hasPermission("vanish.see")) continue;
                                    name = ChatColor.GRAY + "[Versteckt]" + ChatColor.RESET;
                                }
                            }
                            if(p == sender) {
                                name += ChatColor.ITALIC;
                            }
                            name += p.getDisplayName() + ChatColor.RESET;
                            clusterPlayers.add(name);
                        };

                        totalPlayers += clusterPlayers.size();
                        
                        String playerList = clusterPlayers.toString().substring(1, clusterPlayers.toString().length() - 1);

						ComponentBuilder msg = new ComponentBuilder(" ");

                        HoverEvent he;
                        
                        if(current) {
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

						if(current) {
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
			}
		}
	}

	public Iterable<String> onTabComplete(CommandSender arg0, String[] args) {
		List<String> cl = new ArrayList<String>();
		for(Cluster c : plugin.getClusterManager().getClusterlist())
			if(args.length == 0 || c.getName().toLowerCase().startsWith(args[0].toLowerCase()))
				cl.add(c.getName());
		return cl;
	}


}
