package de.themoep.serverclusters.bungee.bukkitcommands;

import de.themoep.serverclusters.bungee.LocationInfo;
import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.serverclusters.bungee.utils.StringUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public abstract class BukkitCommand extends Command implements TabExecutor {

    protected ServerClusters plugin;

    public BukkitCommand(ServerClusters plugin, String name) {
        this(plugin, name, null);
    }

    public BukkitCommand(ServerClusters plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer) {
            ((ProxiedPlayer) sender).chat("/" + getName() + " " + StringUtils.join(args, " "));
        } else {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player and on the Bukkit server!");
        }
    }

    public abstract void run(CommandSender sender, LocationInfo location, String[] args);
}
