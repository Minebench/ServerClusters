package de.themoep.serverclusters.bungee;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.themoep.serverclusters.bungee.enums.Backend;
import de.themoep.serverclusters.bungee.storage.MysqlStorage;
import de.themoep.serverclusters.bungee.storage.ValueStorage;
import de.themoep.serverclusters.bungee.storage.YamlStorage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

public class Cluster implements Comparable<Cluster> {

    private ServerClusters plugin = null;

    /**
     * The list of servernames in the cluster
     */
    private List<String> serverlist = new ArrayList<>();

    /**
     * Name of the cluster
     */
    private String name;

    /**
     * List of aliases of this cluster
     */
    private Set<String> aliases = new HashSet<>();

    /**
     * Map of lowercase warpnames to their warp info
     */

    private Map<String, WarpInfo> warps = new LinkedHashMap<>();

    /**
     * Allow ignoring the last logout server. This also stops storing of that value!
     */
    private boolean ignoreLogoutServer = false;

    /**
     * Map of players UUID's to the servername they logged out of
     */
    private LoadingCache<UUID, String> logoutCache = null;

    private ValueStorage logoutStorage = null;

    /**
     * The default server of this cluster one connects to the first time
     */
    private String defaultServer;

    /**
     * Whether or not this cluster should how up in the lists
     */
    private boolean hidden = false;
    private LocationInfo spawn;

    /**
     * The cluster object
     * @param plugin     The ServerClusters plugin
     * @param name       The name of the cluster
     * @param serverlist The list of servernames this cluster contains. Cannot be empty!
     */
    public Cluster(ServerClusters plugin, String name, List<String> serverlist) {
        this(plugin, name, serverlist, serverlist.get(0), false);
    }

    /**
     * The cluster object
     * @param plugin        The ServerClusters plugin
     * @param name          The name of the cluster
     * @param serverlist    The list of servernames this cluster contains. Cannot be empty!
     * @param defaultServer The name of the default server the player's connect to if they weren't on the cluster before.
     * @param ignoreLogoutServer Should we ignore the logout server on this cluster?
     */
    public Cluster(ServerClusters plugin, String name, List<String> serverlist, String defaultServer, boolean ignoreLogoutServer) {
        this.plugin = plugin;
        this.name = name;
        this.serverlist = serverlist;
        this.defaultServer = defaultServer;
        this.ignoreLogoutServer = ignoreLogoutServer;

        if (getServerlist().size() > 1 && !shouldIgnoreLogoutServer()) {
            initLogoutStorage();
        }
    }

    public Cluster(ServerClusters plugin, String name, Configuration config) {
        this(
                plugin,
                name,
                config.getStringList("server"),
                config.getString("default", null),
                config.getBoolean("ignoreLogoutServer", false)
        );

        setAliases(config.getStringList("alias"));
        setHidden(config.getBoolean("hidden", false));
        setDefaultServer(config.getString("cluster", null));
    }

    private void initLogoutStorage() {
        if (plugin.getBackend() == Backend.MYSQL) {
            try {
                logoutStorage = new MysqlStorage(plugin, "logoutserver_" + getName());
            } catch (InvalidPropertiesFormatException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (logoutStorage == null) {
            logoutStorage = new YamlStorage(plugin, "logoutserver_" + getName());
        }
        logoutCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .build(new CacheLoader<UUID, String>() {
                    @Override
                    public String load(UUID uuid) throws Exception {
                        String serverName = null;
                        if(logoutStorage != null) {
                            serverName = logoutStorage.getValue(uuid);
                        }
                        if (serverName == null) {
                            throw new ServerNotFoundException("No logout server found for player " + uuid + " on cluster " + getName());
                        }
                        return serverName;
                    }
                });
    }

    /**
     * Connects a player to the server cluster and the last server he was on
     * @param player The player to connect
     */
    public void connectPlayer(ProxiedPlayer player) {
        String servername = getLogoutServer(player.getUniqueId());
        if (servername == null) {
            servername = getDefaultServer();
        }
        ServerInfo server = plugin.getProxy().getServers().get(servername);
        if (server != null) {
            player.connect(server);
        } else {
            player.sendMessage(new ComponentBuilder("Error:").color(ChatColor.DARK_RED).append(" The server " + servername + " does not exist!").color(ChatColor.RED).create());
        }
    }

    /**
     * Get the default server of this cluster
     * @return The name of the default server
     */
    public String getDefaultServer() {
        return defaultServer != null ? defaultServer : getServerlist().get(0);
    }

    /**
     * Set the default server of this cluster
     * @param defaultServer The name of the default server
     */
    public void setDefaultServer(String defaultServer) {
        if (defaultServer == null || getServerlist().contains(defaultServer)) {
            this.defaultServer = defaultServer;
        }
    }

    /**
     * Sets the server a player loggout out from
     * @param playerId  The UUID of the Player to set the logout server for
     */
    public void setLogoutServer(UUID playerId, String servername) {
        if (getServerlist().size() > 1 && !shouldIgnoreLogoutServer() && getServerlist().contains(servername)) {
            logoutStorage.putValue(playerId, servername);
            logoutCache.put(playerId, servername);
        }
    }

    /**
     * Gets the name of the server a player logged out of
     * @param playerId  The UUID of the Player to get the logout server for
     * @return          The name of the logout server as a string, null if not found
     */
    public String getLogoutServer(UUID playerId) {
        if (getServerlist().size() > 1 && !shouldIgnoreLogoutServer()) {
            try {
                return logoutCache.get(playerId);
            } catch (ExecutionException e) {
                if (!(e.getCause() instanceof ServerNotFoundException)) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Get the list of players on this cluster
     * @return The list of players on this cluster
     */
    public List<ProxiedPlayer> getPlayerlist() {
        List<ProxiedPlayer> playerlist = new ArrayList<ProxiedPlayer>();
        for (String s : getServerlist()) {
            ServerInfo si = plugin.getProxy().getServerInfo(s);
            if (si != null)
                playerlist.addAll(si.getPlayers());
        }
        return playerlist;
    }

    /**
     * Get the amount of players on this cluster
     * @return The amount of players
     */
    public int getPlayerCount() {
        int amount = 0;
        for (String s : getServerlist()) {
            ServerInfo si = plugin.getProxy().getServerInfo(s);
            if (si != null)
                amount += si.getPlayers().size();
        }
        return amount;
    }

    /**
     * Get the list of servers in this cluster
     * @return the serverlist
     */
    public List<String> getServerlist() {
        return serverlist;
    }

    /**
     * Adds a server to a cluster
     * @param name String representing the name of the server
     */
    public void addServer(String name) {
        int countOld = getServerlist().size();
        if (!getServerlist().contains(name)) {
            getServerlist().add(name);
            if (countOld == 1 && !shouldIgnoreLogoutServer()) {
                initLogoutStorage();
            }
        }
    }

    /**
     * Get a collection of all warps of this cluster
     * @return Collection of warp info
     */
    public Collection<WarpInfo> getWarps() {
        return warps.values();
    }

    /**
     * Get a collection of all warps of this cluster a sender has access to
     * @param sender The sender to check permissions for
     * @return Collection of warp info
     */
    public Collection<WarpInfo> getWarps(CommandSender sender) {
        Collection<WarpInfo> warps = new HashSet<WarpInfo>();
        for (WarpInfo warp : getWarps()) {
            if (plugin.getWarpManager().checkAccess(sender, warp)) {
                warps.add(warp);
            }
        }
        return warps;
    }

    /**
     * Get the location of a warp
     * @param name The name of the warp (case insensitive)
     * @return The location, <tt>null</tt> if not found
     */
    public WarpInfo getWarp(String name) {
        return warps.get(name.toLowerCase());
    }

    /**
     * Add a new warp point to this cluster
     * @param warp Info about the warp
     */
    public void addWarp(WarpInfo warp) {
        warps.put(warp.getName().toLowerCase(), warp);
    }

    /**
     * Removes a warp
     * @param name The name of the warp (case insensitive)
     * @return The old WarpInfo, <tt>null</tt> if there was no warp with this name
     */
    public WarpInfo removeWarp(String name) {
        return warps.remove(name.toLowerCase());
    }

    /**
     * Get the name of the cluster
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the aliases
     */
    public Set<String> getAliases() {
        return aliases;
    }

    /**
     * @param aliasList the aliases to set
     */
    public void setAliases(List<String> aliasList) {
        aliases = new HashSet<>();
        for (String alias : aliasList) {
            aliases.add(alias.toLowerCase());
        }
    }

    public boolean isAlias(String name) {
        return aliases.contains(name.toLowerCase());
    }

    public boolean containsServer(String servername) {
        if (getServerlist().contains(servername)) {
            return true;
        }
        for (String s : getServerlist()) {
            if (s.equalsIgnoreCase(servername)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param c is a non-null Cluster.
     * @throws IllegalArgumentException if o is null.
     */
    public int compareTo(Cluster c) {
        if (c == null) {
            throw new IllegalArgumentException("Cluster cannot be compared to null!");
        }
        if (this == c) {
            return 0;
        }

        return getName().compareToIgnoreCase(c.getName());
    }

    /**
     * Set whether or not this cluster should be hidden
     * @param hidden <tt>true</tt> if it should be hidden; <tt>false</tt> if not
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * Get whether or not this cluster is hidden
     * @return <tt>true</tt> if it should be hidden; <tt>false</tt> if not
     */
    public boolean isHidden() {
        return hidden;
    }

    public void destroy() {
        if (logoutStorage != null) {
            logoutStorage.close();
        }
    }

    public String getPermission() {
        return "serverclusters.cluster." + getName().toLowerCase();
    }

    public boolean hasAccess(CommandSender sender) {
        return sender.hasPermission(getPermission()) || sender.hasPermission(getPermission() + ".join");
    }

    public boolean canSee(CommandSender sender) {
        return sender.hasPermission(getPermission()) || sender.hasPermission(getPermission() + ".see");
    }

    public boolean shouldIgnoreLogoutServer() {
        return ignoreLogoutServer;
    }

    public void setIgnoreLogoutServer(boolean ignoreLogoutServer) {
        this.ignoreLogoutServer = ignoreLogoutServer;
    }

    public LocationInfo getSpawn() {
        return spawn;
    }

    public void setSpawn(LocationInfo spawn) {
        this.spawn = spawn;
    }
}
