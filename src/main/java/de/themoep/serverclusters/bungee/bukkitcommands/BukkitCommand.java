package de.themoep.serverclusters.bungee.bukkitcommands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.apache.commons.lang.StringUtils;

public abstract class BukkitCommand extends Command implements TabExecutor {

    public BukkitCommand(String name) {
        this(name, null);
    }

    public BukkitCommand(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer) {
            ((ProxiedPlayer) sender).chat("/" + getName() + " " + StringUtils.join(args, ' '));
        } else {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player and on the Bukkit server!");
        }
    }

    public abstract void run(CommandSender sender, String[] args);
}
