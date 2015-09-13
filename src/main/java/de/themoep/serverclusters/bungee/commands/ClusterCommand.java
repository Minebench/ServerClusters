package de.themoep.serverclusters.bungee.commands;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.ServerClusters;

import de.themoep.vnpbungee.VNPBungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class ClusterCommand extends Command implements TabExecutor {

	ServerClusters plugin;

	public ClusterCommand(ServerClusters plugin, String name, String permission, String[] aliases) {
		super(name, permission, aliases);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {		
		if(sender.hasPermission("serverclusters.command.cluster")) {
			if(args.length == 0) {
				//send cluster list
				// TODO: Change messages to language system!
				sender.sendMessage(new ComponentBuilder("Verfügbare Server:").color(ChatColor.YELLOW).create());

                
				List<Cluster> cl = plugin.getClusterManager().getClusterlist();
				Collections.sort(cl);

				for(Cluster c : cl) {
					if(sender.hasPermission("serverclusters.cluster." + c.getName())) {
						Boolean current = (sender instanceof ProxiedPlayer && c.getServerlist().toString().matches(".*\\b" + ((ProxiedPlayer) sender).getServer().getInfo().getName() + "\\b.*"));
						
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
                        
                        ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cluster " + c.getName());
						if(current) {
                            msg.color(ChatColor.YELLOW);
                        } else {
                            msg.color(ChatColor.GREEN);
                            msg.event(ce);
                        }
						msg.event(he);
                        int playerCount = 0;
                        if(!sender.hasPermission("vanish.see") && plugin.getVnpbungee() != null) {
                            VNPBungee vnpBungee = (VNPBungee) ProxyServer.getInstance().getPluginManager().getPlugin("VNPBungee");
                            boolean senderSeeUnknown = sender instanceof ProxiedPlayer && plugin.getVnpbungee().getVanishStatus((ProxiedPlayer) sender) != VNPBungee.VanishStatus.VISIBLE;
                            for(ProxiedPlayer p : c.getPlayerlist()) {
                                if(senderSeeUnknown || vnpBungee.getVanishStatus(p) == VNPBungee.VanishStatus.VISIBLE) {
                                    playerCount++;
                                }
                            }
                        } else {
                            playerCount = c.getPlayerlist().size();
                        }
						msg.append(" - " + playerCount + " Spieler").color(ChatColor.WHITE);
						msg.event(he);
						if(!current) msg.event(ce);
						sender.sendMessage(msg.create());
					}
				}
				
			} else if(args.length == 1 && sender instanceof  ProxiedPlayer){

				Cluster targetCluster = plugin.getClusterManager().getCluster(args[0]);
				if(targetCluster == null || !sender.hasPermission("serverclusters.cluster." + targetCluster.getName().toLowerCase())) {
					// ERROR no perms
					// ERROR Cluster not found
					sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "Cluster " + args[0] + " not found!");
				} else {
					// connect player to cluster
					ProxiedPlayer p = (ProxiedPlayer) sender;
					if(p == null){
						sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + " Use /cluster <clustername> <player> [<player2>...] to send players from the console!");
					} else if (targetCluster == plugin.getClusterManager().getClusterByServer(p.getServer().getInfo().getName())) {
						sender.sendMessage(ChatColor.RED + "Du bist bereits auf " + ChatColor.YELLOW + targetCluster.getName() + ChatColor.RED + "!");
					} else {
						targetCluster.connectPlayer(p);
						p.sendMessage(ChatColor.GREEN + "Verbinde mit " + ChatColor.YELLOW + targetCluster.getName() + ChatColor.GREEN + "...");
					}
				}
			} else {
				if(!sender.hasPermission("serverclusters.command.cluster.others")){
					sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "You don't have the permission serverclusters.tp.others");
				} else {
					Cluster targetCluster = plugin.getClusterManager().getCluster(args[0]);
					if(targetCluster == null || !sender.hasPermission("serverclusters.cluster." + targetCluster.getName().toLowerCase())) {
						// ERROR no perms
						// ERROR Cluster not found
						sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "Cluster " + args[0] + " not found!");
					} else {
						ArrayList<String> playerlist = new ArrayList<String>(Arrays.asList(args));
						playerlist.remove(0);
						for(String playername : playerlist) {
							ProxiedPlayer p = plugin.getProxy().getPlayer(playername);
							if(p == null){
								sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "Player " + ChatColor.RED + playername + ChatColor.YELLOW + " is not online!");
							} else if (targetCluster == plugin.getClusterManager().getClusterByServer(p.getServer().getInfo().getName())){
								sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "Player " + ChatColor.RED + playername + ChatColor.YELLOW + " is already on " + ChatColor.RED + targetCluster.getName() + ChatColor.YELLOW + "!");
							} else {
								targetCluster.connectPlayer(p);
								p.sendMessage(ChatColor.GREEN + "Verbinde mit " + ChatColor.YELLOW + targetCluster.getName() + ChatColor.GREEN + "...");
							}
						}
					}
				}
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
