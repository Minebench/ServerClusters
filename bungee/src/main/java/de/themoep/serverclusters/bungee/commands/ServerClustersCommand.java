package de.themoep.serverclusters.bungee.commands;

import de.themoep.bungeeplugin.PluginCommand;
import de.themoep.serverclusters.bungee.ServerClusters;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;


public class ServerClustersCommand extends PluginCommand {

    protected final ServerClusters plugin;

    public ServerClustersCommand(ServerClusters plugin, String name, String permission, String permissionMessage, String description, String usage, String... aliases) {
        super(plugin, name, permission, permissionMessage, description, usage, aliases);
        this.plugin = plugin;
    }

    @Override
    public boolean run(CommandSender sender, String[] args) {
        if (args.length == 0 || "version".equalsIgnoreCase(args[0])) {
            sender.sendMessage(ChatColor.YELLOW + plugin.getDescription().getName() + ChatColor.GREEN + " v" + plugin.getDescription().getVersion() + " by " + plugin.getDescription().getAuthor());
        } else if ("reload".equalsIgnoreCase(args[0])) {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.YELLOW + "Config reloaded!");
        }
        return true;
    }

}
