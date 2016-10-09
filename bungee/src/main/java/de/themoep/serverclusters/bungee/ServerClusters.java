package de.themoep.serverclusters.bungee;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import de.themoep.serverclusters.bungee.bukkitcommands.*;
import de.themoep.serverclusters.bungee.commands.*;
import de.themoep.serverclusters.bungee.enums.Backend;
import de.themoep.serverclusters.bungee.listeners.*;
import de.themoep.serverclusters.bungee.manager.*;
import de.themoep.serverclusters.bungee.utils.TeleportUtils;

import de.themoep.vnpbungee.VNPBungee;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class ServerClusters extends Plugin {

    private Configuration config = null;

    private Level infolevel = Level.INFO;

    private Backend backend;

    private SpawnManager sm;

    private WarpManager wm;

    private TeleportManager tm;

    private ClusterManager cm;

    private TeleportUtils teleportUtils;

    private BukkitCommandExecutor bukkitCommandExecutor;

    private final List<Command> commandList = new ArrayList<Command>();

    private VNPBungee vnpbungee = null;
    private long commandCooldown;
    private int teleportDelay;

    public void onEnable() {
        loadConfig();
        setupCommands(true);
        teleportUtils = new TeleportUtils(this);
        tm = new TeleportManager(this);

        getProxy().registerChannel("ServerClusters");

        getLogger().log(infolevel, "Registering Listeners...");
        getProxy().getPluginManager().registerListener(this, new PluginMessageListener(this));
        getProxy().getPluginManager().registerListener(this, new ServerSwitchListener(this));
        getProxy().getPluginManager().registerListener(this, new ServerConnectListener(this));

        vnpbungee = (VNPBungee) getProxy().getPluginManager().getPlugin("VNPBungee");
        if (vnpbungee != null) {
            getLogger().log(infolevel, "Found VNPBungee!");
        }

        getLogger().log(infolevel, "Done enabling!");
    }

    public void onDisable() {
        getSpawnManager().destroy();
        getWarpManager().destroy();
        getClusterManager().destroy();
        getTeleportManager().destroy();
    }

    private void loadConfig() {
        getLogger().log(infolevel, "Loading Config...");
        saveDefaultConfig();
        config = loadConfigFile();
        try {
            backend = Backend.valueOf(getConfig().getString("backend"));
        } catch (IllegalArgumentException e) {
            getLogger().info("No or wrong backend option in config.yml. Only YAML and MYSQL is allowed! Falling back to YAML backend!");
            backend = Backend.YAML;
        }

        teleportDelay = getConfig().getInt("teleportDelay", 5);
        commandCooldown = getConfig().getInt("commandCooldown", 10);

        getLogger().info("Loading Cluster Manager...");
        cm = new ClusterManager(this);
        Configuration section = getConfig().getSection("cluster");
        for (String clustername : section.getKeys()) {
            getClusterManager().addCluster(new Cluster(this, clustername, section.getSection(clustername)));
        }

        for (String servername : getProxy().getServers().keySet()) {
            if (!section.getKeys().contains(servername)) {
                boolean addserver = true;
                for (Cluster c : getClusterManager().getClusterlist()) {
                    if (c.containsServer(servername)) {
                        addserver = false;
                        break;
                    }
                }
                if (addserver) {
                    Cluster cluster = new Cluster(this, servername, Collections.singletonList(servername));
                    getClusterManager().addCluster(cluster);
                }
            }
        }

        getLogger().info("Loading Spawn Manager....");
        sm = new SpawnManager(this);

        getLogger().info("Loading Warp Manager...");
        wm = new WarpManager(this);
    }

    /**
     * Initialize and register all commands
     * @param latebind If we should wait a second or not after initialization to registering commands, useful to overwrite bungee's or other plugins commands
     */
    private void setupCommands(boolean latebind) {
        getLogger().log(infolevel, "Initializing Commands...");

        getProxy().getPluginManager().unregisterCommands(this);

        final List<Command> commandList = new ArrayList<>();

        commandList.add(new ServerClustersCommand(this, "serverclusters", "serverclusters.command.serverclusters", new String[]{"sc", "sclusters"}));

        List<String> cal = getConfig().getStringList("commandaliases.cluster");
        commandList.add(new ClusterCommand(this, "cluster", "serverclusters.command.cluster", cal.toArray(new String[cal.size()])));

        List<String> lal = getConfig().getStringList("commandaliases.clist");
        commandList.add(new ListCommand(this, "clist", "serverclusters.command.clist", lal.toArray(new String[lal.size()])));

        List<String> tpal = getConfig().getStringList("commandaliases.tp");
        commandList.add(new TpCommand(this, "tp", "serverclusters.command.tp", tpal.toArray(new String[tpal.size()])));

        List<String> tphal = getConfig().getStringList("commandaliases.tphere");
        commandList.add(new TphereCommand(this, "tphere", "serverclusters.command.tphere", tphal.toArray(new String[tphal.size()])));

        List<String> fal = getConfig().getStringList("commandaliases.find");
        commandList.add(new FindCommand(this, "find", "serverclusters.command.find", fal.toArray(new String[fal.size()])));

        List<String> dsal = getConfig().getStringList("commandaliases.delspawn");
        commandList.add(new DelspawnCommand(this, "delwarp", "serverclusters.command.delspawn", dsal.toArray(new String[fal.size()])));

        List<String> dwal = getConfig().getStringList("commandaliases.delwarp");
        commandList.add(new DelwarpCommand(this, "delwarp", "serverclusters.command.delwarp", dwal.toArray(new String[fal.size()])));

        if (latebind) {
            getLogger().log(infolevel, "Scheduling the Registering of the Commands...");
            final ServerClusters plugin = this;
            getProxy().getScheduler().schedule(this, new Runnable() {
                public void run() {
                    plugin.getLogger().log(infolevel, "Late-binding Commands...");
                    for (Command c : commandList) {
                        ProxyServer.getInstance().getPluginManager().registerCommand(plugin, c);
                    }
                }
            }, 1, TimeUnit.SECONDS);
        } else {
            getLogger().log(infolevel, "Registering Commands...");
            for (Command c : commandList) {
                getProxy().getPluginManager().registerCommand(this, c);
            }
        }

        getLogger().log(infolevel, "Setting up Bukkit commands");
        bukkitCommandExecutor = new BukkitCommandExecutor(this);
        getBukkitCommandExecutor().registerCommand(new TpaCommand(this, "tpa", "serverclusters.command.tpa"));
        getBukkitCommandExecutor().registerCommand(new TpahereCommand(this, "tpahere", "serverclusters.command.tpahere"));
        getBukkitCommandExecutor().registerCommand(new TpacceptCommand(this, "tpaccept", "serverclusters.command.tpaccept"));
        getBukkitCommandExecutor().registerCommand(new TpdenyCommand(this, "tpdeny", "serverclusters.command.tpdeny"));
        getBukkitCommandExecutor().registerCommand(new TpaconfirmCommand(this, "tpaconfirm", "serverclusters.command.tpaconfirm"));
        getBukkitCommandExecutor().registerCommand(new SpawnCommand(this, "setspawn", "serverclusters.command.spawn"));
        getBukkitCommandExecutor().registerCommand(new SetspawnCommand(this, "setspawn", "serverclusters.command.setwarp"));
        getBukkitCommandExecutor().registerCommand(new WarpCommand(this, "warp", "serverclusters.command.warp"));
        getBukkitCommandExecutor().registerCommand(new SetwarpCommand(this, "setwarp", "serverclusters.command.setwarp"));
    }

    /**
     * Reloads the plugin's config.yml from the disk.
     */
    public void reloadConfig() {
        loadConfig();
        setupCommands(false);
    }

    public int getTeleportDelay() {
        return teleportDelay;
    }

    public long getCommandCooldown() {
        return commandCooldown;
    }

    public SpawnManager getSpawnManager() {
        return sm;
    }

    public WarpManager getWarpManager() {
        return wm;
    }

    public TeleportManager getTeleportManager() {
        return tm;
    }

    public ClusterManager getClusterManager() {
        return cm;
    }

    public TeleportUtils getTeleportUtils() {
        return teleportUtils;
    }

    public BukkitCommandExecutor getBukkitCommandExecutor() {
        return bukkitCommandExecutor;
    }

    /**
     * Get the VNPBungee plugin
     * @return The plugin; null if not installed
     */
    public VNPBungee getVnpbungee() {
        return vnpbungee;
    }

    /**
     * @return the backend
     */
    public Backend getBackend() {
        return backend;
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
