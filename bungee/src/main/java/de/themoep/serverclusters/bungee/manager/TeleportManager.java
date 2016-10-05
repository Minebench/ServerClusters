package de.themoep.serverclusters.bungee.manager;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.LocationInfo;
import de.themoep.serverclusters.bungee.ServerClusters;
import de.themoep.serverclusters.bungee.enums.TeleportTarget;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ServerClusters
 * Copyright (C) 2016 Max Lee (https://github.com/Phoenix616/)
 */
public class TeleportManager extends Manager {

    private final Map<String, List<Request>> requestMap = new HashMap<String, List<Request>>();

    private final Map<String, Request> cachedRequests = new HashMap<String, Request>();

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
    public void cacheRequest(ProxiedPlayer sender, ProxiedPlayer receiver, TeleportTarget target, LocationInfo location) {
        cacheRequest(new Request(sender, receiver, target, location));
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
            return r;
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
    public boolean addRequest(ProxiedPlayer sender, ProxiedPlayer receiver, TeleportTarget target, LocationInfo location) {
        if (location != null) {
            return addRequest(new Request(sender, receiver, target, location));
        }
        sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "Your location is unknown? Oo");
        return false;
    }

    private boolean addRequest(Request request) {
        request.setAction(RequestAction.TELEPORT);
        ProxiedPlayer receiver = plugin.getProxy().getPlayer(request.getReceiver());
        ProxiedPlayer sender = plugin.getProxy().getPlayer(request.getSender());
        if (receiver == null || sender == null) {
            return false;
        }
        if (!requestMap.containsKey(request.getReceiver()))
            requestMap.put(request.getReceiver(), new ArrayList<Request>());

        requestMap.get(request.getReceiver()).add(request);

        sender.sendMessage(ChatColor.GREEN + "Teleportationsanfrage an " + ChatColor.YELLOW + receiver.getName() + ChatColor.GREEN + " gesendet!");
        if (request.getTarget() == TeleportTarget.RECEIVER) {
            receiver.sendMessage(ChatColor.YELLOW + sender.getName() + ChatColor.GREEN + " fragt, ob er sich zu " + ChatColor.YELLOW + "dir" + ChatColor.GREEN + " teleportieren darf.");
        } else {
            receiver.sendMessage(ChatColor.YELLOW + sender.getName() + ChatColor.GREEN + " fragt, ob du dich zu " + ChatColor.YELLOW + "ihm" + ChatColor.GREEN + " teleportieren willst.");
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
                            receiver.sendMessage(ChatColor.RED + "Achtung: " + ChatColor.YELLOW + sender.getName() + " befindet sich auf einem anderen Server als du! (" + receiverCluster.getName() + ")");
                        }
                    } else {
                        receiver.sendMessage(ChatColor.RED + "Achtung: " + ChatColor.YELLOW + "Du hast nicht die Rechte um dich auf den Server zu teleportieren auf dem sich " + sender.getName() + " gerade befindet!");
                    }
                } else {
                    receiver.sendMessage(new ComponentBuilder("Achtung: ").color(ChatColor.RED)
                                    .append("Du hast nicht die Rechte um direkt zwischen Servern zu teleportieren! Wechsele zuerst mit /server " + receiverCluster.getName() + " auf den selben Server!").color(ChatColor.YELLOW)
                                    .event(
                                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cluster " + receiverCluster.getName())
                                    ).event(
                                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Klicke um /server " + receiverCluster.getName() + " auszuführen und den Server zu wechseln!"))
                                    ).create()
                    );
                }
            }
        }
        receiver.sendMessage(new ComponentBuilder("Nutze /tpaccept um die Anfrage anzunehmen.")
                        .color(ChatColor.GRAY)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + sender.getName()))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Klicke um /tpaccept auszuführen und die Anfrage anzunehmen!")))
                        .create()
        );
        receiver.sendMessage(new ComponentBuilder("Nutze /tpdeny um die Anfrage abzulehnen.")
                        .color(ChatColor.GRAY)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny " + sender.getName()))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Klicke um /tpdeny auszuführen und die Anfrage abzulehnen!")))
                        .create()
        );
        return true;
    }

    /**
     * Remove a request
     * @param request The Request to remove
     * @return <tt>true</tt> if it was removed; <tt>false</tt> if it wasn't there anymore
     */
    private boolean removeRequest(Request request) {
        List<Request> requestList = requestMap.get(request.getReceiver());
        if (requestList == null || requestList.isEmpty())
            return false;

        return requestList.remove(request);
    }

    /**
     * Get the an open request
     * @param player The player to get the request for
     * @param sender The sender to search for
     * @return The request of the sender; the last request if the sender is null or empty
     */
    private Request getRequest(ProxiedPlayer player, String sender) {
        List<Request> requestList = requestMap.get(player.getName());
        if (requestList == null || requestList.isEmpty())
            return null;

        if (sender == null || sender.isEmpty()) {
            return requestList.get(requestList.size() - 1);
        } else {
            for (int i = requestList.size(); i > 0; i--) {
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
        // TODO: Change messages to language system!
        Request request = getRequest(player, senderName);
        if (request == null) {
            player.sendMessage(ChatColor.RED + "Du hast keine offenen Anfragen" + (senderName == null || senderName.isEmpty() ? "!" : " von " + ChatColor.YELLOW + senderName + ChatColor.RED + "!"));
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

        // TODO: Make timeout configurable
        if (request.getTimestamp() + 120 * 1000000 < System.currentTimeMillis()) {
            player.sendMessage(ChatColor.RED + "Die letzte Anfrage von " + ChatColor.YELLOW + request.getSender() + ChatColor.RED + " ist bereits ausgelaufen!");
            return false;
        }

        ProxiedPlayer sender = plugin.getProxy().getPlayer(request.getSender());
        if (sender == null) {
            player.sendMessage(ChatColor.YELLOW + request.getSender() + ChatColor.RED + " ist nichtmehr online!");
            return false;
        }

        ProxiedPlayer receiver = plugin.getProxy().getPlayer(request.getReceiver());
        if (receiver == null) {
            player.sendMessage(ChatColor.YELLOW + request.getReceiver() + ChatColor.RED + " ist nichtmehr online!");
            return false;
        }

        if (!isCached(request) && request.getTarget() == TeleportTarget.RECEIVER) {
            Cluster senderCluster = plugin.getClusterManager().getPlayerCluster(sender);
            if (senderCluster == null) {
                sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "On what cluster are you on? Oo");
                return false;
            }
            Cluster receiverCluster = plugin.getClusterManager().getPlayerCluster(receiver);
            if (receiverCluster == null) {
                sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.YELLOW + "On what cluster is " + receiver.getName() + " on? Oo");
                return false;
            }

            if (!senderCluster.equals(receiverCluster)) {
                if (sender.hasPermission("serverclusters.command.tpa.intercluster")) {
                    if (sender.hasPermission("serverclusters.cluster." + receiverCluster.getName())) {
                        if (!sender.hasPermission("serverclusters.command.tpa.intercluster.nowarning")) {
                            request.setAction(RequestAction.TELEPORT);
                            cacheRequest(request);
                            sender.sendMessage(new ComponentBuilder(receiver.getName()).color(ChatColor.RED)
                                            .append("befindet sich auf einem anderen Server als du! (" + receiverCluster.getName() + ")").color(ChatColor.YELLOW)
                                            .create()
                            );
                            sender.sendMessage(new ComponentBuilder("Nutze ").color(ChatColor.YELLOW)
                                            .append("/tpaconfirm").color(ChatColor.RED).event(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaconfirm")
                                            ).event(
                                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Klicke um /tpaconfirm auszuführen!"))
                                            ).retain(ComponentBuilder.FormatRetention.NONE)
                                            .append(" um dich trotzdem zu ihm zu teleportieren!").color(ChatColor.YELLOW)
                                            .create()
                            );
                        }
                    } else {
                        receiver.sendMessage(ChatColor.RED + "Achtung: " + ChatColor.YELLOW + "Du hast nicht die Rechte um dich auf den Server zu teleportieren auf dem sich " + receiver.getName() + " gerade befindet!");
                    }
                } else {
                    sender.sendMessage(new ComponentBuilder("Achtung: ").color(ChatColor.RED)
                                    .append("Du hast nicht die Rechte um direkt zwischen Servern zu teleportieren! Wechsele zuerst mit /server " + receiverCluster.getName() + " auf den selben Server!").color(ChatColor.YELLOW)
                                    .event(
                                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cluster " + receiverCluster.getName())
                                    ).event(
                                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Klicke um /server " + receiverCluster.getName() + " auszuführen und den Server zu wechseln!"))
                                    ).create()
                    );
                }
            }
        }

        player.sendMessage(ChatColor.GREEN + "Teleportationsanfrage von " + ChatColor.YELLOW + request.getSender() + ChatColor.GREEN + " akzeptiert!");
        sender.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " hat deine Teleportationsanfrage angenommen!");

        boolean r = false;
        if (request.getTarget() == TeleportTarget.RECEIVER) {
            r = plugin.getTeleportUtils().teleportToPlayer(sender, player);
        } else if (request.getTarget() == TeleportTarget.SENDER) {
            r = plugin.getTeleportUtils().teleport(player, request.getLocation());
        }

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
     * @param player     The player who wants to deny the request
     * @param senderName The name of the player who sent the request
     * @return <tt>true</tt> if a request by that player was found, <tt>false</tt> if not
     */
    private boolean denyRequest(ProxiedPlayer player, String senderName) {
        // TODO: Change messages to language system!
        Request request = getRequest(player, senderName);
        if (request == null) {
            player.sendMessage(ChatColor.RED + "Du hast keine offenen Anfragen" + (senderName == null || senderName.isEmpty() ? "!" : " von " + ChatColor.YELLOW + senderName + ChatColor.RED + "!"));
            return false;
        }

        removeRequest(request);

        // TODO: Make timeout configurable
        if (request.getTimestamp() + 120 * 1000000 < System.currentTimeMillis()) {
            player.sendMessage(ChatColor.RED + "Die letzte Anfrage von " + ChatColor.YELLOW + request.getSender() + ChatColor.RED + " ist bereits ausgelaufen!");
            return false;
        }

        ProxiedPlayer sender = plugin.getProxy().getPlayer(request.getSender());
        if (sender != null)
            sender.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.RED + " hat deine Teleportationsanfrage abgelehnt!");

        player.sendMessage(ChatColor.GRAY + "Teleportationsanfrage von " + ChatColor.YELLOW + request.getSender() + ChatColor.GRAY + " verweigert!");
        return true;
    }

    private class Request {
        private final long timestamp = System.currentTimeMillis();
        private final String sender;
        private final String receiver;
        private final TeleportTarget target;
        private final LocationInfo location;
        private RequestAction action = RequestAction.QUEUE;

        public Request(String sender, String receiver, TeleportTarget target, LocationInfo location) {
            this.sender = sender;
            this.receiver = receiver;
            this.target = target;
            this.location = location;
        }

        public Request(ProxiedPlayer sender, ProxiedPlayer receiver, TeleportTarget target, LocationInfo location) {
            this.sender = sender.getName();
            this.receiver = receiver.getName();
            this.target = target;
            this.location = location;
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

        public LocationInfo getLocation() {
            return location;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{timestamp=" + timestamp + ",sender=" + sender + ",receiver=" + receiver + ",target=" + target + ",location=" + location + "}";
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
