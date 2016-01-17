package de.themoep.serverclusters.bungee.manager;

import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.serverclusters.bungee.enums.TeleportTarget;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ServerClusters
 * Copyright (C) 2016 Max Lee (https://github.com/Phoenix616/)
 */
public class TeleportManager {
    private final ServerClusters plugin;

    private Map<UUID, List<Request>> requestMap = new HashMap<UUID, List<Request>>();

    public TeleportManager(ServerClusters plugin) {
        this.plugin = plugin;
    }

    /**
     * Add a teleport request
     * @param sender  The player who send the request
     * @param receiver The player who the request was sent to
     * @param target Where we should teleport to
     */
    public void addRequest(ProxiedPlayer sender, ProxiedPlayer receiver, TeleportTarget target) {
        if(!requestMap.containsKey(receiver.getUniqueId()))
            requestMap.put(receiver.getUniqueId(), new ArrayList<Request>());

        requestMap.get(receiver.getUniqueId()).add(new Request(sender, target));
    }

    /**
     * Get the an open request
     * @param player The player to get the request for
     * @param sender The sender to search for
     * @return The request of the sender; the last request if the sender is null or empty
     */
    private Request getRequest(ProxiedPlayer player, String sender) {
        List<Request> requestList = requestMap.get(player.getUniqueId());
        if(requestList == null || requestList.isEmpty())
            return null;

        if(sender == null || sender.isEmpty()) {
            Request request = requestList.get(requestList.size() - 1);
            if(request.wasHandled())
                return null;
            return request;
        } else {
            for(int i = requestList.size(); i > 0; i--) {
                if(requestList.get(i).getSender().equalsIgnoreCase(sender)) {
                    if(requestList.get(i).wasHandled())
                        return null;
                    return requestList.get(i);
                }
            }
        }
        return null;
    }

    /**
     * Accept the last teleport request
     * @param player The player who wants to accept the request
     * @return <tt>true</tt> if he was teleported, <tt>false</tt> if not
     */
    public boolean acceptLastRequest(ProxiedPlayer player) {
        return acceptRequest(player, null);
    }

    /**
     * Accept a teleport request by a specific player
     * @param player The player who wants to accept the request
     * @param senderName The name of the player who sent the request
     * @return <tt>true</tt> if he was teleported, <tt>false</tt> if not
     */
    public boolean acceptRequest(ProxiedPlayer player, String senderName) {
        // TODO: Change messages to language system!
        Request request = getRequest(player, senderName);
        if(request == null) {
            player.sendMessage(ChatColor.RED + "Du hast keine offenen Anfragen" + (senderName == null || senderName.isEmpty() ? "!" : " von " + ChatColor.YELLOW + senderName + ChatColor.RED + "!"));
            return false;
        }

        // TODO: Make timeout configurable
        if(request.getTimestamp() + 120 * 1000000 < System.currentTimeMillis()) {
            player.sendMessage(ChatColor.RED + "Die letzte Anfrage von " + ChatColor.YELLOW + request.getSender() + ChatColor.RED + " ist bereits ausgelaufen!");
            return false;
        }

        ProxiedPlayer sender = plugin.getProxy().getPlayer(request.getSender());
        if(sender == null) {
            player.sendMessage(ChatColor.YELLOW + request.getSender() + ChatColor.RED + " ist nichtmehr online!");
            return false;
        }

        player.sendMessage(ChatColor.GRAY + "Teleportationsanfrage von " + ChatColor.YELLOW + request.getSender() + ChatColor.GRAY + " akzeptiert!");
        sender.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " hat deine Teleportationsanfrage angenommen!");

        boolean r = false;
        if(request.getTarget() == TeleportTarget.RECEIVER) {
            r = plugin.getTeleportUtils().teleportToPlayer(sender, player);
        } else if(request.getTarget() == TeleportTarget.SENDER) {
            r = plugin.getTeleportUtils().teleportToPlayer(player, sender);
        }
        if(r)
            request.setHandled();

        return r;
    }

    /**
     * Deny the last teleport request
     * @param player The player who wants to deny the request
     * @return <tt>true</tt> if a request by that player was found, <tt>false</tt> if not
     */
    private boolean denyLastRequest(ProxiedPlayer player) {
        return denyRequest(player, null);
    }

    /**
     * Deny a teleport request by a specific player
     * @param player The player who wants to deny the request
     * @param senderName The name of the player who sent the request
     * @return <tt>true</tt> if a request by that player was found, <tt>false</tt> if not
     */
    private boolean denyRequest(ProxiedPlayer player, String senderName) {
        // TODO: Change messages to language system!
        Request request = getRequest(player, senderName);
        if(request == null) {
            player.sendMessage(ChatColor.RED + "Du hast keine offenen Anfragen" + (senderName == null || senderName.isEmpty() ? "!" : " von " + ChatColor.YELLOW + senderName + ChatColor.RED + "!"));
            return false;
        }

        // TODO: Make timeout configurable
        if(request.getTimestamp() + 120 * 1000000 < System.currentTimeMillis()) {
            player.sendMessage(ChatColor.RED + "Die letzte Anfrage von " + ChatColor.YELLOW + request.getSender() + ChatColor.RED + " ist bereits ausgelaufen!");
            return false;
        }

        ProxiedPlayer sender = plugin.getProxy().getPlayer(request.getSender());
        if(sender != null)
            sender.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.RED + " hat deine Teleportationsanfrage abgelehnt!");

        player.sendMessage(ChatColor.GRAY + "Teleportationsanfrage von " + ChatColor.YELLOW + request.getSender() + ChatColor.GRAY + " verweigert!");
        return true;
    }

    private class Request {
        private final long timestamp = System.currentTimeMillis();
        private final String sender;
        private final TeleportTarget target;
        private boolean handled = false;

        public Request(String sender, TeleportTarget target) {
            this.sender = sender;
            this.target = target;
        }

        public Request(ProxiedPlayer sender, TeleportTarget target) {
            this.sender = sender.getName();
            this.target = target;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getSender() {
            return sender;
        }

        public TeleportTarget getTarget() {
            return target;
        }

        public void setHandled() {
            this.handled = true;
        }

        public boolean wasHandled() {
            return handled;
        }
    }
}
