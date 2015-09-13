package de.themoep.serverclusters.bungee;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import de.themoep.serverclusters.bungee.enums.Backend;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Cluster implements Comparable<Cluster> {

	private ServerClusters plugin = null;
	
	/**
	 * The list of servernames in the cluster
	 */
	private List<String> serverlist = new ArrayList<String>();
	
	/**
	 * Name of the cluster
	 */
	private String name;
	
	/**
	 * List of aliases of this cluster
	 */
	private List<String> aliaslist = new ArrayList<String>();
	
	/**
	 * Map of players UUID's to the servername they logged out of
	 */
	private HashMap<UUID,String> logoutmap = new HashMap<UUID,String>();

	/**
	 * The default server of this cluster one connects to the first time
	 */
	private String defaultServer;

	/**
	 * The cluster object
	 * @param plugin The ServerClusters plugin
	 * @param name The name of the cluster
	 * @param serverlist The list of servernames this cluster contains. Cannot be empty!
	 */
	public Cluster(ServerClusters plugin, String name, List<String> serverlist) {
		this(plugin,name,serverlist,serverlist.get(0));
	}
	
	/**
	 * The cluster object
	 * @param plugin The ServerClusters plugin
	 * @param name The name of the cluster
	 * @param serverlist The list of servernames this cluster contains. Cannot be empty!
	 * @param defaultServer The name of the default server the player's connect to if they weren't on the cluster before.
	 */
	public Cluster(ServerClusters plugin, String name, List<String> serverlist, String defaultServer) {
		this.plugin = plugin;
		setName(name);
		if(serverlist != null) {
			setServerlist(serverlist);
		}
		this.defaultServer = defaultServer;
	}
	
	/**
	 * Connects a player to the server cluster and the last server he was on
	 * @param playername The name of the player
	 */	
	public void connectPlayer(String playername) {
		ProxiedPlayer player = plugin.getProxy().getPlayer(playername);
		if(player != null)
			connectPlayer(player);
	}
	
	/**
	 * Connects a player to the server cluster and the last server he was on
	 * @param playerid The UUID of the player
	 */	
	public void connectPlayer(UUID playerid) {
		ProxiedPlayer player = plugin.getProxy().getPlayer(playerid);
		if(player != null)
			connectPlayer(player);
	}

	/**
	 * Connects a player to the server cluster and the last server he was on
	 * @param player The player to connect
	 */
	public void connectPlayer(ProxiedPlayer player) {
		String servername = getLoggoutServer(player.getUniqueId());
		if(servername == null) {
            servername = getDefaultServer();
        }
		ServerInfo server = plugin.getProxy().getServers().get(servername);
		if(server != null) {
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
		return defaultServer;
	}

	/**
	 * Sets the server a player loggout out from
	 * @param player The Player to save the loggout server
	 * @param servername The name of the server the player logged out from as a string
	 */
	public void setLoggoutServer(ProxiedPlayer player, String servername) {
		if(getServerlist().contains(servername)) {
            logoutmap.put(player.getUniqueId(), servername);
        }
	}
	
	/**
	 * Gets the name of the server a player logged out of
	 * @param playerid The Player to get the servername
	 * @return The servername as a string, null if not found
	 */
	public String getLoggoutServer(UUID playerid) {
		if(logoutmap.containsKey(playerid))
			return logoutmap.get(playerid);
		if(plugin.getBackend() == Backend.MYSQL) {
			try {
				PreparedStatement sta;
				sta = plugin.getConnection().prepareStatement("SELECT servername as name from " + plugin.getTablePrefix() + "_logoutserver WHERE playerid=?");
		        sta.setString(1, getName() + playerid);
		        ResultSet rs = sta.executeQuery();
			    sta.close();		      
		        rs.next();	        
		        return rs.getString("name");
			} catch (SQLException e) {
				plugin.getLogger().severe("MySQL-Error! Something went wrong while fetching the logout server for player with the id " + playerid + " on cluster " + getName() + "! Does the table \""+ plugin.getTablePrefix() + "_loggoutserver\" exist?");
				//e.printStackTrace();
			}
		} else if(plugin.getBackend() == Backend.YAML) {
			// TODO: YAML BACKEND
		}
		return null;
	}
	
	public String getLoggoutServer(String playername) {
		return getLoggoutServer(plugin.getProxy().getPlayer(playername).getUniqueId().toString());
	}
	

	public List<ProxiedPlayer> getPlayerlist() {
		// TODO Auto-generated method stub
		List<ProxiedPlayer> playerlist = new ArrayList<ProxiedPlayer>();
		for(String s : getServerlist()) {
			ServerInfo si = plugin.getProxy().getServerInfo(s);
			if(si != null)
				playerlist.addAll(si.getPlayers());
		}
		return playerlist;
	}
		
	/**
	 * Get the list of servers in this cluster
	 * @return the serverlist
	 */
	public List<String> getServerlist() {
		return serverlist;
	}

	/**
	 * @param serverlist the serverlist to set
	 */
	public void setServerlist(List<String> serverlist) {
		this.serverlist = serverlist;
	}

	/**
	 * Adds a server to a cluster
	 * @param name String representing the name of the server
	 */
	public void addServer(String name) {
		if(!serverlist.contains(name.toLowerCase()))
			serverlist.add(name.toLowerCase());
	}

	/**
	 * Get the name of the cluster
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of the cluster
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the aliaslist
	 */
	public List<String> getAliaslist() {
		return aliaslist;
	}

	/**
	 * @param aliaslist the aliaslist to set
	 */
	public void setAliaslist(List<String> aliaslist) {
		this.aliaslist = aliaslist;
	}

	public boolean containsServer(String servername) {
		for(String s : getServerlist()) {
            if(s.equalsIgnoreCase(servername)) {
                return true;
            }
        }
		return false;
	}

	/**
	 * @param c is a non-null Cluster.
	 * @throws NullPointerException if o is null.
	 */
	public int compareTo(Cluster c) {
		if(this == c) return 0;
		return getName().compareToIgnoreCase(c.getName());
	}
}
