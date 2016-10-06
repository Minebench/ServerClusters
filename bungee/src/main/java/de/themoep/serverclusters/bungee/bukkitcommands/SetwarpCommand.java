package de.themoep.serverclusters.bungee.bukkitcommands;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.LocationInfo;
import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.serverclusters.bungee.ServerNotFoundException;
import de.themoep.serverclusters.bungee.WarpInfo;
import de.themoep.vnpbungee.VNPBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SetwarpCommand extends BukkitCommand {

    public SetwarpCommand(ServerClusters plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    @Override
    public void run(CommandSender sender, LocationInfo location, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.YELLOW + "/" + getName() + " <warp> [global]");
            return;
        }

        try {
            WarpInfo warp = plugin.getWarpManager().addWarp(args[0], location, args.length > 1 && "global".equalsIgnoreCase(args[1]));
            sender.sendMessage(ChatColor.GREEN + "Set warp " + ChatColor.YELLOW + warp.getName() + ChatColor.GREEN + " to your location!");
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());        }

    }

    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }
}
