package de.themoep.serverclusters.bungee.commands;

import de.themoep.serverclusters.bungee.ServerClusters;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class TpaconfirmCommand extends Command {

    private ServerClusters plugin;

    public TpaconfirmCommand(ServerClusters plugin, String name, String permission) {
        super(name, permission);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission(getPermission())) {
            return;
        }
        if(sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            if(!plugin.getTeleportManager().applyCachedRequest(player)) {
                sender.sendMessage(ChatColor.RED + "Du hast keine Anfrage die du bestätigen müsstest!");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player!");
        }
    }
}
