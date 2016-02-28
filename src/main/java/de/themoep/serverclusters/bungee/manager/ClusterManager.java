package de.themoep.serverclusters.bungee.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import de.themoep.serverclusters.bungee.Cluster;
import de.themoep.serverclusters.bungee.ServerClusters;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ClusterManager {

	private ServerClusters plugin;
		
	private Map<String, Cluster> clustermap = new HashMap<String, Cluster>();

    private Set<UUID> stayincluster = new HashSet<UUID>();
 	
	public ClusterManager(ServerClusters plugin) {
		this.plugin = plugin;
	}

	/**
	 * Get all clusters in a list
	 * @return List of clusters
	 */
	public List<Cluster> getClusterlist() {
		return new ArrayList<Cluster>(this.clustermap.values());
	}
		
	/**
	 * Get a cluster by name                                            
	 * @param name The name or alias of the cluster as a string, case-insensitive
	 * @return cluster or null if cluster does not exist                
	 */
	public Cluster getCluster(String name) {
		if(this.clustermap.containsKey(name))
			return this.clustermap.get(name);
		for(Cluster c : this.clustermap.values()) {
			if(c.getName().equalsIgnoreCase(name))
				return c;
			for(String a : c.getAliaslist())
				if(a.equalsIgnoreCase(name))
					return c;
		}
		return null;
	}
	
	/**
	 * Get the cluster a server belongs to
	 * @param servername The name of the server to search for as a string
	 * @return cluster or null if cluster does not exist
	 */
	public Cluster getClusterByServer(String servername) {
		for(Cluster c : this.getClusterlist()) {
			for(String s : c.getServerlist()) {
				if(s.equals(servername))
					return c;
			}
		}
		return null;		
	}

    /**
     * Get the cluster a player is currently on
     * @param player The player to get the cluster for
     * @return cluster or null if the player is on no server
     */
	public Cluster getPlayerCluster(ProxiedPlayer player) {
        if(player.getServer() == null || player.getServer().getInfo() == null) {
            return null;
        }
        return getClusterByServer(player.getServer().getInfo().getName());
	}

	/**
	 * Adds new cluster to the clustermanager
	 * @param cluster Cluster to add
	 */
	public void addCluster(Cluster cluster) {
		this.clustermap.put(cluster.getName(), cluster);		
	}
	
	/**
	 * @return the stayincluster HashSet of players UUID's
	 */
	public Set<UUID> getStayInCluster() {
		return stayincluster;
	}


	public void destroy() {
		for(Cluster cluster : clustermap.values()) {
			cluster.destroy();
		}
	}
}
