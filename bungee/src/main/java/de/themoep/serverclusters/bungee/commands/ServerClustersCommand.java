package de.themoep.serverclusters.bungee.commands;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.ServerClusters;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.List;


public class ServerClustersCommand extends Command implements TabExecutor {

    ServerClusters plugin;

    public ServerClustersCommand(ServerClusters plugin, String name, String permission, String[] aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 || "version".equalsIgnoreCase(args[0])) {
            sender.sendMessage(ChatColor.YELLOW + plugin.getDescription().getName() + ChatColor.GREEN + " v" + plugin.getDescription().getVersion() + " by " + plugin.getDescription().getAuthor());
        } else if ("reload".equalsIgnoreCase(args[0])) {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.YELLOW + "Config reloaded!");
        }
    }

    public Iterable<String> onTabComplete(CommandSender arg0, String[] args) {
        List<String> cl = new ArrayList<String>();
        for (Cluster c : plugin.getClusterManager().getClusterlist())
            if (args.length == 0 || c.getName().toLowerCase().startsWith(args[0].toLowerCase()))
                cl.add(c.getName());
        return cl;
    }


}
