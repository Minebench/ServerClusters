package de.themoep.serverclusters.bungee.manager;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.serverclusters.bungee.enums.TeleportTarget;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * ServerClusters
 * Copyright (C) 2016 Max Lee (https://github.com/Phoenix616/)
 */
public class TeleportManager extends Manager {

    private final Multimap<String, Request> requestMap = MultimapBuilder.hashKeys().arrayListValues().build();

    private final Map<String, Request> cachedRequests = new HashMap<>();

    private Map<UUID, ScheduledTask> teleportTasks = new HashMap<>();

    public TeleportManager(ServerClusters plugin) {
        super(plugin);
    }

    @Override
    public void destroy() {
        // Nothing to do here
    }

    /**
     * Cache a request if the user would get a warning for teleporting across clusters
     * @param sender   The player who send the request
     * @param receiver The player who the request was sent to
     * @param target   Where we should teleport to
     */
    public void cacheRequest(ProxiedPlayer sender, ProxiedPlayer receiver, TeleportTarget target) {
        cacheRequest(new Request(sender, receiver, target));
    }

    /**
     * Cache a request if the user would get a warning for teleporting across clusters
     * @param request The Request
     */
    public void cacheRequest(Request request) {
        cachedRequests.put(request.getSender(), request);
    }

    private boolean isCached(Request request) {
        return cachedRequests.containsValue(request);
    }

    /**
     * Add a cached request
     * @param sender The player who wants to add a cached request
     * @return <tt>true</tt> if the request was send successfully; <tt>false</tt> if no request was cached or the receiver was already offline
     */
    public boolean applyCachedRequest(ProxiedPlayer sender) {
        boolean r = false;
        Request request = cachedRequests.get(sender.getName());
        if (request == null) {
            return false;
        }
        if (request.getAction() == RequestAction.QUEUE) {
            r = addRequest(request);
            if (r) {
                cachedRequests.remove(sender.getName());
            }
        } else if (request.getAction() == RequestAction.TELEPORT) {
            r = acceptRequest(sender, request);
            if (r) {
                cachedRequests.remove(sender.getName());
            }
        }
        return r;
    }

    /**
     * Add a teleport request
     * @param sender   The player who send the request
     * @param receiver The player who the request was sent to
     * @param target   Where we should teleport to
     * @return <tt>true</tt> if the request was successfully added; <tt>false</tt> if the receiver isn't online anymore or an error occurred
     */
    public boolean addRequest(ProxiedPlayer sender, ProxiedPlayer receiver, TeleportTarget target) {
        return addRequest(new Request(sender, receiver, target));
    }

    private boolean addRequest(Request request) {
        request.setAction(RequestAction.TELEPORT);
        ProxiedPlayer receiver = plugin.getProxy().getPlayer(request.getReceiver());
        ProxiedPlayer sender = plugin.getProxy().getPlayer(request.getSender());
        if (receiver == null || sender == null) {
            return false;
        }

        requestMap.put(request.getReceiver(), request);

        if (request.getTarget() == TeleportTarget.RECEIVER) {
            plugin.sendLang(receiver, "teleport.request.target.receiver", "player", sender.getName());
        } else {
            Cluster receiverCluster = plugin.getClusterManager().getPlayerCluster(receiver);
            Cluster targetCluster = plugin.getClusterManager().getPlayerCluster(sender);
            if (receiverCluster == null) {
                receiver.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "On what cluster is " + sender.getName() + " on? Oo");
            }
            if (targetCluster == null) {
                receiver.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "On what cluster are you on? Oo");
            }
            if (receiverCluster != null && targetCluster != null && !receiverCluster.equals(targetCluster)) {
                if (receiver.hasPermission("serverclusters.command.tpahere.intercluster")) {
                    if (receiver.hasPermission("serverclusters.cluster." + targetCluster.getName())) {
                        if (!receiver.hasPermission("serverclusters.command.tpahere.intercluster.nowarning")) {
                            plugin.sendLang(receiver, "teleport.request.target.sender.other-cluster", "player", sender.getName(), "cluster", targetCluster.getName());
                        }
                    } else {
                        plugin.sendLang(receiver, "teleport.request.no-rights.self", "cluster", targetCluster.getName(), "player", sender.getName());
                        plugin.sendLang(receiver, "teleport.request.no-rights.other", "player", sender.getName(), "cluster", targetCluster.getName());
                    }
                } else {
                    plugin.sendLang(receiver, "teleport.request.no-rights.directly", "cluster", targetCluster.getName(), "player", sender.getName());
                }
            }
            plugin.sendLang(receiver, "teleport.request.target.sender.received", "player", sender.getName());
        }
        plugin.sendLang(sender, "teleport.request.send", "player", receiver.getName());
        plugin.sendLang(receiver, "teleport.request.received", "player", sender.getName());
        plugin.sendLang(receiver, "teleport.request.deny", "player", sender.getName());
        plugin.sendLang(receiver, "teleport.request.timeout", "timeout", String.valueOf(plugin.getTeleportTimeout()));
        return true;
    }

    /**
     * Remove a request
     * @param request The Request to remove
     * @return <tt>true</tt> if it was removed; <tt>false</tt> if it wasn't there anymore
     */
    private boolean removeRequest(Request request) {
        return requestMap.remove(request.getReceiver(), request);
    }

    /**
     * Get the an open request
     * @param player The player to get the request for
     * @param sender The sender to search for
     * @return The request of the sender; the last request if the sender is null or empty
     */
    private Request getRequest(ProxiedPlayer player, String sender) {
        List<Request> requestList = (List<Request>) requestMap.get(player.getName());
        if (requestList == null || requestList.isEmpty())
            return null;

        if (sender == null || sender.isEmpty()) {
            return requestList.get(requestList.size() - 1);
        } else {
            for (int i = requestList.size() - 1; i >= 0; i--) {
                if (requestList.get(i).getSender().equalsIgnoreCase(sender)) {
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
        return acceptRequest(player, (String) null);
    }

    /**
     * Accept a teleport request by a specific player
     * @param player     The player who wants to accept the request
     * @param senderName The name of the player who sent the request
     * @return <tt>true</tt> if he was teleported, <tt>false</tt> if not
     */
    public boolean acceptRequest(ProxiedPlayer player, String senderName) {
        Request request = getRequest(player, senderName);
        if (request == null) {
            if (senderName == null || senderName.isEmpty()) {
                plugin.sendLang(player, "error.request.none");
            } else {
                plugin.sendLang(player, "error.request.none-player", "player", senderName);
            }
            return false;
        }

        return acceptRequest(player, request);
    }

    /**
     * Accept a teleport request
     * @param player  The player who wants to accept the request
     * @param request The Request to accept
     * @return <tt>true</tt> if he was teleported, <tt>false</tt> if not
     */
    public boolean acceptRequest(ProxiedPlayer player, Request request) {
        removeRequest(request);

        if (request.getTimestamp() + plugin.getTeleportTimeout() * 1000 < System.currentTimeMillis()) {
            plugin.sendLang(player, "error.request.timeout", "player", request.getSender());
            return false;
        }

        ProxiedPlayer sender = plugin.getProxy().getPlayer(request.getSender());
        if (sender == null) {
            plugin.sendLang(player, "error.request.offline", "player", request.getSender());
            return false;
        }

        ProxiedPlayer receiver = plugin.getProxy().getPlayer(request.getReceiver());
        if (receiver == null) {
            plugin.sendLang(player, "error.request.offline", "player", request.getReceiver());
            return false;
        }

        if (!isCached(request)) {
            Cluster fromCluster;
            Cluster toCluster;
            if (request.getTarget() == TeleportTarget.RECEIVER) {
                fromCluster = plugin.getClusterManager().getPlayerCluster(sender);
                toCluster = plugin.getClusterManager().getPlayerCluster(receiver);
            } else {
                fromCluster = plugin.getClusterManager().getPlayerCluster(receiver);
                toCluster = plugin.getClusterManager().getPlayerCluster(sender);
            }
            if (fromCluster == null && request.getTarget() == TeleportTarget.RECEIVER || toCluster == null && request.getTarget() == TeleportTarget.SENDER) {
                plugin.sendLang(sender, "error.unknown-cluster");
                return false;
            }
            if (toCluster == null && request.getTarget() == TeleportTarget.RECEIVER || fromCluster == null && request.getTarget() == TeleportTarget.SENDER) {
                plugin.sendLang(sender, "error.unknown-cluster-player", "player", receiver.getName());
                return false;
            }

            if (!fromCluster.equals(toCluster) && !checkTeleportPermission(sender, receiver, request, toCluster)) {
                return false;
            }
        }

        plugin.sendLang(player, "teleport.request.accepted.receiver", "player", request.getSender());
        plugin.sendLang(sender, "teleport.request.accepted.sender", "player", request.getSender());

        if (request.getTarget() == TeleportTarget.RECEIVER) {
            plugin.getTeleportUtils().teleportToPlayer(sender, player);
        } else if (request.getTarget() == TeleportTarget.SENDER) {
            plugin.getTeleportUtils().teleportToPlayer(player, sender);
        }
        return true;
    }

    private boolean checkTeleportPermission(ProxiedPlayer sender, ProxiedPlayer receiver, Request request, Cluster toCluster) {
        ProxiedPlayer toCheck = request.getTarget() == TeleportTarget.RECEIVER ? sender : receiver;
        ProxiedPlayer target = request.getTarget() == TeleportTarget.RECEIVER ? receiver : sender;
        String type = request.getTarget() == TeleportTarget.RECEIVER ? "tpa" : "tpahere";
        if (toCheck.hasPermission("serverclusters.command." + type + ".intercluster")) {
            if (toCheck.hasPermission("serverclusters.cluster." + toCluster.getName())) {
                if (!toCheck.hasPermission("serverclusters.command." + type + ".intercluster.nowarning")) {
                    request.setAction(RequestAction.TELEPORT);
                    cacheRequest(request);
                    plugin.sendLang(toCheck, "on-server", "player", target.getName(), "cluster", toCluster.getName());
                    plugin.sendLang(toCheck, "teleport.request.confirm", "player", target.getName(), "cluster", toCluster.getName());
                    return false;
                }
            } else {
                plugin.sendLang(toCheck, "teleport.request.no-rights.self", "cluster", toCluster.getName(), "player", target.getName());
                return false;
            }
        } else {
            plugin.sendLang(toCheck, "teleport.request.no-rights.directly", "cluster", toCluster.getName(), "player", target.getName());
            return false;
        }
        return true;
    }

    /**
     * Deny the last teleport request
     * @param player The player who wants to deny the request
     * @return <tt>true</tt> if a request by that player was found, <tt>false</tt> if not
     */
    public boolean denyLastRequest(ProxiedPlayer player) {
        return denyRequest(player, null);
    }

    /**
     * Deny a teleport request by a specific player
     * @param player     The player who wants to deny the request
     * @param senderName The name of the player who sent the request
     * @return <tt>true</tt> if a request by that player was found, <tt>false</tt> if not
     */
    public boolean denyRequest(ProxiedPlayer player, String senderName) {
        Request request = getRequest(player, senderName);
        if (request == null) {
            if (senderName == null || senderName.isEmpty()) {
                plugin.sendLang(player, "error.request.none");
            } else {
                plugin.sendLang(player, "error.request.none-player", "player", senderName);
            }
            return false;
        }

        removeRequest(request);

        if (request.getTimestamp() + plugin.getTeleportTimeout() * 1000 < System.currentTimeMillis()) {
            plugin.sendLang(player, "error.request.timeout", "player", request.getSender());
            return false;
        }

        ProxiedPlayer sender = plugin.getProxy().getPlayer(request.getSender());
        if (sender != null)
            plugin.sendLang(sender, "teleport.request.accepted.sender", "player", player.getName());

        plugin.sendLang(player, "teleport.request.accepted.receiver", "player", senderName);
        return true;
    }

    public void scheduleDelayedTeleport(ProxiedPlayer player, Runnable runnable) {
        if (teleportTasks.containsKey(player.getUniqueId())) {
            plugin.getProxy().getScheduler().cancel(teleportTasks.get(player.getUniqueId()));
        }
        teleportTasks.put(player.getUniqueId(), plugin.getProxy().getScheduler().schedule(plugin, runnable, plugin.getTeleportDelay(), TimeUnit.SECONDS));
    }

    public void cancelTeleport(ProxiedPlayer player) {
        if (teleportTasks.containsKey(player.getUniqueId())) {
            teleportTasks.get(player.getUniqueId()).cancel();
            plugin.sendLang(player, "teleport.cancelled", "delay", String.valueOf(plugin.getTeleportDelay()));
        }
    }

    private class Request {
        private final long timestamp = System.currentTimeMillis();
        private final String sender;
        private final String receiver;
        private final TeleportTarget target;
        private RequestAction action = RequestAction.QUEUE;

        public Request(String sender, String receiver, TeleportTarget target) {
            this.sender = sender;
            this.receiver = receiver;
            this.target = target;
        }

        public Request(ProxiedPlayer sender, ProxiedPlayer receiver, TeleportTarget target) {
            this.sender = sender.getName();
            this.receiver = receiver.getName();
            this.target = target;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getSender() {
            return sender;
        }

        public String getReceiver() {
            return receiver;
        }

        public TeleportTarget getTarget() {
            return target;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{timestamp=" + timestamp + ",sender=" + sender + ",receiver=" + receiver + ",target=" + target + "}";
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        public RequestAction getAction() {
            return action;
        }

        public void setAction(RequestAction action) {
            this.action = action;
        }
    }

    private enum RequestAction {
        TELEPORT,
        QUEUE
    }
}
