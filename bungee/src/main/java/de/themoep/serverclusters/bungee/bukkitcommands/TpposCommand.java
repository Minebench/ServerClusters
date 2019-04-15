package de.themoep.serverclusters.bungee.bukkitcommands;

import de.themoep.serverclusters.bungee.LocationInfo;
import de.themoep.serverclusters.bungee.ServerClusters;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;

public class TpposCommand extends BukkitCommand {
    public TpposCommand(ServerClusters plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    @Override
    public void run(CommandSender sender, LocationInfo location, String[] args) {
        // TODO: Change messages to language system!
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.YELLOW + "/" + this.getName() + " [<playername>] <x> <y> <z> [<yaw> <pitch>] [[<server>:]<world>]");
            return;
        }

        String worldName = null;
        String serverName = null;
        int locLength = args.length;
        if (args.length > 3 && sender.hasPermission("serverclusters.command.tppos.toworld")) {
            if (args[args.length - 1].contains(":") && sender.hasPermission("serverclusters.command.tppos.toserver")) {
                String[] parts = args[args.length - 1].split(":");
                serverName = parts[0];
                worldName = parts[1];
                locLength = args.length - 1;
            } else {
                try {
                    Float.parseFloat(args[args.length - 1]);
                    if (location != null) {
                        worldName = location.getWorld();
                        serverName = location.getServer();
                    }
                } catch (NumberFormatException e1) {
                    try {
                        Double.parseDouble(args[args.length - 1]);
                        if (location != null) {
                            worldName = location.getWorld();
                            serverName = location.getServer();
                        }
                    } catch (NumberFormatException e2) {
                        worldName = args[args.length - 1];
                        if (location != null) {
                            serverName = location.getServer();
                        }
                        locLength = args.length - 1;
                    }
                }
            }
        } else if (location != null) {
            worldName = location.getWorld();
            serverName = location.getServer();
        }

        if (serverName == null) {
            if (worldName == null) {
                sender.sendMessage(ChatColor.RED + "Please specify the world and the server!");
            } else {
                sender.sendMessage(ChatColor.RED + "Please specify the server!");
            }
            return;
        }

        ServerInfo server = plugin.getProxy().getServerInfo(serverName);
        if (server == null) {
            sender.sendMessage(ChatColor.RED + "The server " + ChatColor.YELLOW + serverName + ChatColor.RED + " does not exist!");
            return;
        }

        int locIndex = 0;
        ProxiedPlayer player;
        if (locLength == 3 || locLength == 5 || locLength == 7) {
            if (sender instanceof ProxiedPlayer) {
                player = (ProxiedPlayer) sender;
            } else {
                sender.sendMessage(ChatColor.RED + "To use this command from the console you have to specify the name of the player that you want to teleport!");
                return;
            }
        } else if (locLength > 3) {
            player = plugin.getProxy().getPlayer(args[0]);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "The player " + ChatColor.YELLOW + args[0] + ChatColor.RED + " was not found!");
                return;
            }
            locIndex = 1;
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.YELLOW + "/" + this.getName() + " [<playername>] <x> <y> <z> [<yaw> <pitch>] [[<server>:]<world>]");
            return;
        }

        try {
            double x = Double.parseDouble(args[locIndex + 0]);
            double y = Double.parseDouble(args[locIndex + 1]);
            double z = Double.parseDouble(args[locIndex + 2]);
            float yaw = 0;
            float pitch = 0;
            if (args.length > locIndex + 4) {
                yaw = Float.parseFloat(args[locIndex + 3]);
                pitch = Float.parseFloat(args[locIndex + 4]);
            } else if (location != null) {
                yaw = location.getYaw();
                pitch = location.getPitch();
            }

            player.sendMessage(ChatColor.GREEN + "Teleportiere...");
            plugin.getTeleportUtils().teleportToLocation(player, server, worldName, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid number! Usage: " + ChatColor.YELLOW + "/" + this.getName() + " [<playername>] <x> <y> <z> [<yaw> <pitch>] [[<server>:]<world>]");
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
