package de.themoep.serverclusters.bungee;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import de.themoep.bungeeplugin.BungeePlugin;
import de.themoep.bungeeplugin.PluginCommand;
import de.themoep.serverclusters.bungee.bukkitcommands.*;
import de.themoep.serverclusters.bungee.commands.*;
import de.themoep.serverclusters.bungee.enums.Backend;
import de.themoep.serverclusters.bungee.listeners.*;
import de.themoep.serverclusters.bungee.manager.*;
import de.themoep.serverclusters.bungee.utils.TeleportUtils;

import de.themoep.vnpbungee.VNPBungee;

import net.md_5.bungee.config.Configuration;

public class ServerClusters extends BungeePlugin {

    private Level infolevel = Level.INFO;

    private Backend backend;

    private SpawnManager sm;

    private WarpManager wm;

    private TeleportManager tm;

    private ClusterManager cm;

    private TeleportUtils teleportUtils;

    private BukkitCommandExecutor bukkitCommandExecutor;

    private VNPBungee vnpbungee = null;
    private long commandCooldown;
    private int teleportDelay;
    private int teleportTimeout;

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
        try {
            getConfig().loadConfig();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error loading config!", e);
            return;
        }
        try {
            backend = Backend.valueOf(getConfig().getString("backend"));
        } catch (IllegalArgumentException e) {
            getLogger().info("No or wrong backend option in config.yml. Only YAML and MYSQL is allowed! Falling back to YAML backend!");
            backend = Backend.YAML;
        }

        teleportDelay = getConfig().getInt("teleportDelay", 5);
        commandCooldown = getConfig().getInt("commandCooldown", 10);
        teleportTimeout = getConfig().getInt("teleportTimeout", 30);

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
        try {
            sm = new SpawnManager(this);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load spawn storage!", e);
        }

        getLogger().info("Loading Warp Manager...");
        try {
            wm = new WarpManager(this);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load warp storage!", e);
        }
    }

    /**
     * Initialize and register all commands
     * @param latebind If we should wait a second or not after initialization to registering commands, useful to overwrite bungee's or other plugins commands
     */
    private void setupCommands(boolean latebind) {
        getLogger().log(infolevel, "Initializing Commands...");

        getProxy().getPluginManager().unregisterCommands(this);

        final List<PluginCommand> commands = new ArrayList<>();

        commands.add(new ServerClustersCommand(this, "serverclusters"));
        commands.add(new ClusterCommand(this, "cluster"));
        commands.add(new ListCommand(this, "clist"));
        commands.add(new TpCommand(this, "tp"));
        commands.add(new TphereCommand(this, "tphere"));
        commands.add(new FindCommand(this, "find"));
        commands.add(new DelspawnCommand(this, "delspawn"));
        commands.add(new DelwarpCommand(this, "delwarp"));

        if (latebind) {
            getLogger().log(infolevel, "Scheduling the Registering of the Commands...");
            final ServerClusters plugin = this;
            getProxy().getScheduler().schedule(this, () -> {
                plugin.getLogger().log(infolevel, "Late-binding Commands...");
                for (PluginCommand command : commands) {
                    getProxy().getPluginManager().registerCommand(this, command);
                }
            }, 1, TimeUnit.SECONDS);
        } else {
            getLogger().log(infolevel, "Registering Commands...");
            for (PluginCommand command : commands) {
                getProxy().getPluginManager().registerCommand(this, command);
            }
        }

        getLogger().log(infolevel, "Setting up Bukkit commands");
        bukkitCommandExecutor = new BukkitCommandExecutor(this);
        getBukkitCommandExecutor().registerCommand(new TpposCommand(this, "tppos", "serverclusters.command.tppos"));
        getBukkitCommandExecutor().registerCommand(new TpaCommand(this, "tpa", "serverclusters.command.tpa"));
        getBukkitCommandExecutor().registerCommand(new TpahereCommand(this, "tpahere", "serverclusters.command.tpahere"));
        getBukkitCommandExecutor().registerCommand(new TpacceptCommand(this, "tpaccept", "serverclusters.command.tpaccept"));
        getBukkitCommandExecutor().registerCommand(new TpdenyCommand(this, "tpdeny", "serverclusters.command.tpdeny"));
        getBukkitCommandExecutor().registerCommand(new TpaconfirmCommand(this, "tpaconfirm", "serverclusters.command.tpaconfirm"));
        getBukkitCommandExecutor().registerCommand(new SpawnCommand(this, "spawn", "serverclusters.command.spawn"));
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

    public int getTeleportTimeout() {
        return teleportTimeout;
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

}
