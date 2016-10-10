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
import java.util.Collections;
import java.util.List;


public class TpaCommand extends CooldownBukkitCommand {

    public TpaCommand(ServerClusters plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    @Override
    public void run(CommandSender sender, LocationInfo location, String[] args) {
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
        ProxiedPlayer target = plugin.getProxy().getPlayer(args[0]);
        if (target == null) {
            for (ProxiedPlayer t : plugin.getProxy().getPlayers()) {
                if (t.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    target = t;
                }
            }
        }

        if (target == null || (plugin.getVnpbungee() != null && plugin.getVnpbungee().getVanishStatus(target) == VNPBungee.VanishStatus.VANISHED && !sender.hasPermission("vanish.see"))) {
            sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "The player " + args[0] + " was not found online!");
            return;
        }

        Cluster senderCluster = plugin.getClusterManager().getPlayerCluster(p);
        if (senderCluster == null) {
            sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "On what cluster are you on? Oo");
            return;
        }
        Cluster targetCluster = plugin.getClusterManager().getPlayerCluster(target);
        if (targetCluster == null) {
            sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "On what cluster is " + target.getName() + " on? Oo");
            return;
        }

        if (senderCluster.equals(targetCluster)) {
            plugin.getTeleportManager().addRequest(p, target, TeleportTarget.RECEIVER);
            return;
        }

        if (!sender.hasPermission("serverclusters.command.tpa.intercluster")) {
            sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "You don't have permission to teleport between server! Please use /server to switch before you teleport!");
            return;
        }

        if (!sender.hasPermission("serverclusters.cluster." + targetCluster.getName())) {
            sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "You don't have permission to teleport to the server " + target.getName() + " is on!");
            return;
        }

        if (sender.hasPermission("serverclusters.command.tpa.intercluster.nowarning")) {
            plugin.getTeleportManager().addRequest(p, target, TeleportTarget.RECEIVER);
        } else {
            plugin.getTeleportManager().cacheRequest(p, target, TeleportTarget.RECEIVER);
            p.sendMessage(new ComponentBuilder(target.getName()).color(ChatColor.RED)
                            .append(" befindet sich auf dem Server " + targetCluster.getName() + "!").color(ChatColor.YELLOW)
                            .create()
            );
            p.sendMessage(new ComponentBuilder("Nutze ").color(ChatColor.YELLOW)
                            .append("/tpaconfirm").color(ChatColor.RED).event(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaconfirm")
                            ).event(
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                                            ChatColor.BLUE + "Klicke um " + ChatColor.YELLOW + "/tpaconfirm" + ChatColor.BLUE + " auszuf\u00fchren!")
                                    ))
                            .append(" um trotzdem eine Anfrage zu senden und den Server automatisch zu wechseln sobald deine Anfragen angenommen wird!")
                            .retain(ComponentBuilder.FormatRetention.NONE)
                            .color(ChatColor.YELLOW)
                            .create()
            );
        }
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
