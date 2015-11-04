package de.themoep.serverclusters.bungee;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import de.themoep.serverclusters.bungee.commands.ClusterCommand;
import de.themoep.serverclusters.bungee.commands.FindCommand;
import de.themoep.serverclusters.bungee.commands.ListCommand;
import de.themoep.serverclusters.bungee.commands.TpCommand;
import de.themoep.serverclusters.bungee.enums.Backend;
import de.themoep.serverclusters.bungee.listeners.ServerConnectListener;
import de.themoep.serverclusters.bungee.listeners.ServerSwitchListener;
import de.themoep.serverclusters.bungee.manager.ClusterManager;
import de.themoep.serverclusters.bungee.utils.TeleportUtils;

import de.themoep.vnpbungee.VNPBungee;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class ServerClusters extends Plugin {

	Configuration config = null;
	
	Level infolevel = Level.INFO;

	private Backend backend;	
	
	private String dbuser;
	private String dbpassword;
	private String dbname;
	private String dbhost;
	private int dbport;
	private String dburl;
	private String dbtableprefix;
	
	private Connection conn = null;

	private ClusterManager cm;

	private TeleportUtils teleportUtils;

    List<Command> commandList = new ArrayList<Command>();

    private VNPBungee vnpbungee = null;

    private ServerClusters plugin;

    public void onEnable() {
		saveDefaultConfig();
		config = loadConfigFile();
		loadConfig();
		setupCommands(true);

		teleportUtils = new TeleportUtils(this);

		getProxy().registerChannel("ServerClusters");

		getLogger().log(infolevel, "Registering Listeners...");
		getProxy().getPluginManager().registerListener(this, new ServerSwitchListener(this));
		getProxy().getPluginManager().registerListener(this, new ServerConnectListener(this));

        vnpbungee = (VNPBungee) getProxy().getPluginManager().getPlugin("VNPBungee");
        if(vnpbungee != null) {
            getLogger().log(infolevel, "Found VNPBungee!");
        }

        plugin = this;
		getLogger().log(infolevel, "Done enabling!");
	}

	public void onDisable() {
		try {
			conn.close();
		} catch (SQLException e) {
			getLogger().severe("Error while closing the database connection");
			e.printStackTrace();
		}
	}
	
	private void loadConfig() {
		getLogger().log(infolevel, "Loading Config...");
		try {
			backend = Backend.valueOf(getConfig().getString("backend"));
		} catch (IllegalArgumentException e) {
			getLogger().info("No or wrong backend option in config.yml. Only YAML and MYSQL is allowed! Falling back to YAML backend!");
			backend = Backend.YAML;
		}
		if(getBackend() == Backend.MYSQL) {
			dbuser = getConfig().getString("mysql.user");
			dbpassword = getConfig().getString("mysql.password");
			dbname = getConfig().getString("mysql.dbname");
			dbhost = getConfig().getString("mysql.host");
			dbport = getConfig().getInt("mysql.port");
			dbtableprefix = config.getString("mysql.tableprefix", "serverclusters_");
			
			if(dbhost != null && dbuser != null && dbpassword != null && dbname != null && dbport > 0) {				
				dburl = ("jdbc:mysql://" + dbhost + ":" + dbport + "/" + dbname);
				
				getLogger().info("Checking Database Connection...");
				checkConnection();
				
				getLogger().info("Initializing Database...");
				initDb();
			} else {
				getLogger().warning("MySQL settings not or not fully configured! Falling back to YAML backend!");
				backend = Backend.YAML;
			}
		}
		
		getLogger().info("Loading Cluster Manager...");
		cm = new ClusterManager(this);
		Configuration section = getConfig().getSection("cluster");
		for(String clustername : section.getKeys()) {			
			Cluster cluster = new Cluster(this, clustername, getConfig().getStringList("cluster." + clustername + ".server"));
			cluster.setAliaslist(getConfig().getStringList("cluster." + clustername + ".alias"));
			cluster.setHidden(getConfig().getBoolean("cluster." + clustername + ".hidden", false));
			cluster.setDefaultServer(getConfig().getString("cluster." + clustername + ".default", null));
			getClusterManager().addCluster(cluster);
		}
		
		for(String servername : getProxy().getServers().keySet()) {
			if(!section.getKeys().contains(servername)) {
				Boolean addserver = true;
				for(Cluster c : getClusterManager().getClusterlist()) {
					if(c.containsServer(servername)) {
						addserver = false;
						break;
					}
				}
				if(addserver) {
					Cluster cluster = new Cluster(this, servername, Arrays.asList(servername));
					getClusterManager().addCluster(cluster);
				}
			}
		}
		
	}

	/**
	 * Initialize and register all commands
	 * @param latebind If we should wait a second or not after initialization to registering commands, useful to overwrite bungee's or other plugins commands
	 */
	private void setupCommands(Boolean latebind) {
		getLogger().log(infolevel, "Initializing Commands...");

		List<String> cal = getConfig().getStringList("commandaliases.cluster");
		commandList.add(new ClusterCommand(this, "cluster","serverclusters.command.cluster", cal.toArray(new String[cal.size()])));

		List<String> lal = getConfig().getStringList("commandaliases.clist");
        commandList.add(new ListCommand(this, "clist","serverclusters.command.clist", lal.toArray(new String[lal.size()])));

		List<String> tpal = getConfig().getStringList("commandaliases.ctp");
        commandList.add(new TpCommand(this, "ctp","serverclusters.command.ctp", tpal.toArray(new String[tpal.size()])));

		List<String> fal = getConfig().getStringList("commandaliases.cfind");
		commandList.add(new FindCommand(this, "cfind","serverclusters.command.cfind", fal.toArray(new String[fal.size()])));

		if(latebind) {
			getLogger().log(infolevel, "Scheduling the Registering of the Commands...");
			getProxy().getScheduler().schedule(this, new Runnable() {
				public void run() {
					plugin.getLogger().log(infolevel, "Late-binding Commands...");
                    for(Command c : plugin.commandList) {
                        ProxyServer.getInstance().getPluginManager().registerCommand(plugin, c);
                    }
				}
			}, 1, TimeUnit.SECONDS);
		} else {
			getLogger().log(infolevel, "Registering Commands...");
            for(Command c : commandList) {
                ProxyServer.getInstance().getPluginManager().registerCommand(this, c);
            }
		}
	}

	/**
	 * Reloads the plugin's config.yml from the disk.
	 */
	public void reloadConfig() {
        config = loadConfigFile();
        loadConfig();
        setupCommands(false);
	}	
	
	public ClusterManager getClusterManager() {
		return cm;
	}

    public TeleportUtils getTeleportUtils() {
        return teleportUtils;
    }

    /**
     * Get the VNPBungee plugin
     * @return The plugin; null if not installed
     */
    public VNPBungee getVnpbungee() {
        return vnpbungee;
    }
	
//-VVV- Database stuff -VVV-*/

    public String getTablePrefix() {
        return dbtableprefix;
    }

    /**
     * @return the backend
     */
    public Backend getBackend() {
        return backend;
    }

	/**
	 * Connects to the Database.
	 */
	private void connectDb() {
		getLogger().info("Connecting to Database...");
	    try {
		    conn = (Connection) DriverManager.getConnection(dburl, dbuser, dbpassword);
		    conn.setAutoCommit(true);
	    } catch (SQLException e) {
	    	getLogger().severe("Could not establish a connection to the database! Error: " + e.getMessage());
	    	getLogger().warning(getDescription().getName() + " will not work proberly without its database!");
	    }
	}
	
	/**
	 * Initializes the databases for the plugin if they don't exist.
	 */
	private void initDb() {
		try {
			Statement sta = (Statement)getConnection().createStatement();
			sta.execute("CREATE TABLE IF NOT EXISTS `" + getTablePrefix() + "_logoutserver` ( `playerid` varchar(52) NOT NULL, `servername` count(16) NOT NULL, PRIMARY KEY (`playerid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
			sta.close();
		} catch (SQLException e) {
			getLogger().severe("Could not initialize the tables! Error: " + e.getMessage());
			//e.printStackTrace();
		}
	}
	
	/**
	 * Checks if the connection to the database stil exists and reconnects if it doesn't.
	 */
	private void checkConnection() {
		try {
			if (conn == null || !conn.isValid(1))
				connectDb();
		} catch (SQLException e) {
			//e.printStackTrace(); //We don't need to warnings...
		}
	}
	
	/**
	 * Gets the database connection
	 * @return
	 */	
	public Connection getConnection() {
		checkConnection();
		return conn;
	}
	
//-VVV- Config handling! -VVV-*/
	/**
     * Get the plugin's default config.
     * @return The plugin's configuration
     */
	public Configuration getConfig() {
		return config;
	}

	/**
	 * Save the default config.yml
	 */
	public void saveDefaultConfig() {
		saveDefaultConfig("config.yml");
	}
	
	/**
	 * Save a default config file
	 * @param filename The name of the config file
	 */
	public void saveDefaultConfig(final String filename) {
		if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), filename);

        if (!file.exists()) {
            try {
				Files.copy(getResourceAsStream(filename), file.toPath());
			} catch (IOException e) {
				getLogger().log(Level.SEVERE, "Error while saving default config '" + filename + "'!");
				e.printStackTrace();
			}
        }
	}

	/**
	 * Load the default config.yml file
	 * @return The default Configuration
	 */
	public Configuration loadConfigFile() {
		return loadConfigFile("config.yml");
	}	

	/**
	 * Load a different config file
	 * @param filename The name of the config file
	 * @return The Configuration in the file
	 */
	public Configuration loadConfigFile(String filename) {
		try {
			return ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), filename));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			getLogger().log(Level.SEVERE, "Error while loading config '" + filename + "'!");
			e.printStackTrace();
		}
		return null;
		
	}
}
