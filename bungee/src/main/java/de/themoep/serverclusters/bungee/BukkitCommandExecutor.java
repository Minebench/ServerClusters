package de.themoep.serverclusters.bungee;

import de.themoep.serverclusters.bungee.bukkitcommands.BukkitCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * ServerClusters
 * Copyright (C) 2016 Max Lee (https://github.com/Phoenix616/)
 */
public class BukkitCommandExecutor {

    private final ServerClusters plugin;
    private final Map<String, BukkitCommand> commandMap = new HashMap<String, BukkitCommand>();

    public BukkitCommandExecutor(ServerClusters plugin) {
        this.plugin = plugin;
    }

    public boolean registerCommand(BukkitCommand command) {
        if (commandMap.containsKey(command.getName())) {
            return false;
        }

        commandMap.put(command.getName(), command);
        plugin.getProxy().getPluginManager().registerCommand(plugin, command);
        return true;
    }

    public boolean execute(String commandName, String senderName, LocationInfo location, String[] args) {
        BukkitCommand command = commandMap.get(commandName);

        if (command == null) {
            return false;
        }

        CommandSender sender = null;
        if ("[@]".equalsIgnoreCase(senderName)) {
            sender = plugin.getProxy().getConsole();
        } else {
            sender = plugin.getProxy().getPlayer(senderName);
        }

        if (sender == null) {
            return false;
        }

        command.run(sender, location, args);
        return true;
    }
}
