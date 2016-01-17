package de.themoep.serverclusters.bungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.HashMap;
import java.util.Map;

/**
 * ServerClusters
 * Copyright (C) 2016 Max Lee (https://github.com/Phoenix616/)
 */
public class BukkitCommandExecutor {

    private final ServerClusters plugin;
    private Map<String, Command> commandMap = new HashMap<String, Command>();

    public BukkitCommandExecutor(ServerClusters plugin) {
        this.plugin = plugin;
    }

    public boolean registerCommand(Command command) {
        if(commandMap.containsKey(command.getName()))
            return false;
        commandMap.put(command.getName(), command);
        return true;
    }

    public boolean execute(String commandName, String sender, String[] args) {
        Command command = commandMap.get(commandName);

        if(command == null)
            return false;

        ProxiedPlayer player = plugin.getProxy().getPlayer(sender);
        if(player == null)
            return false;

        command.execute(player, args);
        return true;
    }
}
