package de.themoep.serverclusters.bukkit.manager;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.themoep.serverclusters.bukkit.enums.EntryType;
import de.themoep.serverclusters.bukkit.QueueEntry;
import de.themoep.serverclusters.bukkit.ServerClustersBukkit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Phoenix616 on 08.01.2015.
 */
public class TeleportManager implements Listener {

    private final ServerClustersBukkit plugin;

    private HashMap<String, QueueEntry> tpQueue = new HashMap<>();
    private Map<UUID, Long> tpRequests = new HashMap<>();

    private BukkitTask teleportTask = null;

    public TeleportManager(ServerClustersBukkit plugin) {
        this.plugin = plugin;
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "sc:cancelteleport");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (plugin.getTeleportDelay() <= 0) {
            return;
        }

        if (event.getTo() == event.getFrom()) {
            return;
        }

        if (isQueued(event.getPlayer(), 60)) {
            event.setCancelled(true);
        }

        if (tpRequests.containsKey(event.getPlayer().getUniqueId())) {
            if (tpRequests.get(event.getPlayer().getUniqueId()) + plugin.getTeleportDelay() * 1000 < System.currentTimeMillis()) {
                cancelTeleport(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (plugin.getTeleportDelay() <= 0) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (isQueued((Player) event.getEntity(), 60)) {
            event.setCancelled(true);
        }

        if (tpRequests.containsKey(event.getEntity().getUniqueId())) {
            if (tpRequests.get(event.getEntity().getUniqueId()) + plugin.getTeleportDelay() * 1000 < System.currentTimeMillis()) {
                cancelTeleport((Player) event.getEntity());
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.getTeleportDelay() <= 0) {
            return;
        }

        if (isQueued(event.getPlayer(), 60)) {
            event.setCancelled(true);
        }

        if (tpRequests.containsKey(event.getPlayer().getUniqueId())) {
            if (tpRequests.get(event.getPlayer().getUniqueId()) + plugin.getTeleportDelay() * 1000 < System.currentTimeMillis()) {
                cancelTeleport(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.getTeleportDelay() <= 0) {
            return;
        }

        if (isQueued(event.getPlayer(), 60)) {
            event.setCancelled(true);
        }

        if (tpRequests.containsKey(event.getPlayer().getUniqueId())) {
            if (tpRequests.get(event.getPlayer().getUniqueId()) + plugin.getTeleportDelay() * 1000 < System.currentTimeMillis()) {
                cancelTeleport(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        if (isQueued(event.getPlayer(), 60)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (isQueued(event.getPlayer(), 60)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (isQueued(event.getPlayer(), 60)) {
            event.setCancelled(true);
        }
    }

    private void cancelTeleport(Player player) {
        tpRequests.remove(player.getUniqueId());
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(player.getName());
        player.sendPluginMessage(plugin, "sc:cancelteleport", out.toByteArray());
    }

    /**
     * Teleport the player if he has an entry in the teleport queue
     */
    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event) {
        if (isQueued(event.getPlayer())) {
            QueueEntry entry = getQueueEntry(event.getPlayer().getName());
            if (entry != null) {
                if (runEntry(event.getPlayer(), entry) < 0) {
                    addQueueEntry(event.getPlayer().getName(), entry.copy());
                }
            }
        }
    }

    /**
     * @return byte: 1 if the player was teleported,
     * 0 if the entry type was unsupported
     * -1 if the player or target was not found was not found or is null
     */
    private byte runEntry(Player player, QueueEntry entry) {
        if (entry.getType() == EntryType.LOCATION) {
            Location entryLocation = entry.getLocation();
            if (entryLocation != null) {
                return teleport(player, entryLocation);
            }
        } else if (entry.getType() == EntryType.STRING) {
            String targetName = entry.getString();
            if (targetName != null) {
                return teleport(player, plugin.getServer().getPlayer(targetName));
            }
        }
        return 0;
    }

    /**
     * Teleports a player to a target location.
     * If the player is not online it will queue it 'til he is online and teleport him then.
     * @param playerName The name of the player to teleport
     * @param target     The location where to teleport the player to
     */
    public void teleport(String playerName, Location target) {
        Player player = plugin.getServer().getPlayer(playerName);
        if (teleport(player, target) < 0) {
            addQueueEntry(playerName, new QueueEntry(playerName, target));
        }
    }

    /**
     * Teleports a player to a target location.
     * @param player The player to teleport
     * @param target The location to teleport to
     * @return byte: 1 if the player was teleported,
     * -1 if the player or target was not found was not found or is null
     */
    public byte teleport(Player player, Location target) {
        if (target != null && player != null && player.isOnline() && player.getLastPlayed() + 100 < System.currentTimeMillis()) {
            Location loc = makeTeleportSafe(player, target);
            if (loc == null) {
                plugin.getLogger().warning("Target location could not be made save to teleport " + player.getName() + " to ([" + target.getWorld().getName() + "] " + target.getX() + ", " + target.getY() + ", " + target.getZ() + ")");
                return 0;
            }
            player.teleport(target);
            removeQueueEntry(player.getName());

            player.sendMessage(ChatColor.GREEN + "Teleportiert!");
            plugin.debug("Teleported " + player.getName() + " to ([" + target.getWorld().getName() + "] " + target.getX() + ", " + target.getY() + ", " + target.getZ() + ")");
            return 1;
        }
        return -1;
    }

    /**
     * Teleports a player to a target player.
     * If the player is not online it will queue it 'til he is online and teleport him then.
     * @param playerName Name of the player to teleport
     * @param targetName Name of the player to teleport to
     */
    public void teleport(String playerName, String targetName) {
        Player player = plugin.getServer().getPlayer(playerName);
        if (teleport(player, plugin.getServer().getPlayer(targetName)) < 0) {
            addQueueEntry(playerName, new QueueEntry(playerName, targetName));
        }
    }

    /**
     * Teleports a player to a target player.
     * @param player The player to teleport
     * @param target The player to teleport to
     * @return byte: 1 if the player was teleported,
     * -1 if the player or the target was not found or is null
     */
    public byte teleport(Player player, Player target) {
        if (target != null && target.isOnline() && player != null && player.isOnline() && player.getLastPlayed() + 100 < System.currentTimeMillis()) {
            Location loc = makeTeleportSafe(player, target.getLocation());
            if (loc == null) {
                return 0;
            }
            player.teleport(loc);
            removeQueueEntry(player.getName());
            player.sendMessage(ChatColor.GREEN + "Teleportiert!");
            plugin.debug("Teleported " + player.getName() + " to " + target.getName() + " ([" + loc.getWorld().getName() + "] " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ")");
            return 1;
        }
        return -1;
    }

    /**
     * Get if a player is queued for teleport
     * @param playerName Name of the player to check
     * @return boolean: true if queued,
     * false if not
     */
    public boolean isQueued(String playerName) {
        return tpQueue.containsKey(playerName);
    }

    /**
     * Get if a player is queued for teleport
     * @param player Name of the player to check
     * @return boolean: true if queued,
     * false if not
     */
    public boolean isQueued(Player player) {
        return isQueued(player.getName());
    }

    /**
     * Get if a player was queued for teleport in the last x seconds and removes older entries
     * @param player The player to check
     * @param x      How far back the player has to be gotten queued
     * @return boolean: true if queued in the last x seconds, false if not
     */
    public boolean isQueued(Player player, int x) {
        return isQueued(player.getName(), x);
    }

    /**
     * Get if a player was queued for teleport in the last x seconds and removes older entries
     * @param playerName Name of the player to check
     * @param x          How far back the player has to be gotten queued
     * @return boolean: true if queued in the last x seconds, false if not
     */
    public boolean isQueued(String playerName, int x) {
        if (isQueued(playerName)) {
            QueueEntry entry = getQueueEntry(playerName);
            if (entry != null && entry.getTimeStamp() + x * 1000 > System.currentTimeMillis()) {
                return true;
            }
            removeQueueEntry(playerName);
        }
        return false;
    }

    /**
     * Get the queue entry of a player
     * @param playerName The name of the player
     * @return The full QueueEntry to the player, null if he doesn't have one
     */
    public QueueEntry getQueueEntry(String playerName) {
        return tpQueue.get(playerName);
    }

    /**
     * Add a new player entry to the teleport queue
     * @param playerName The name of the player to queue
     * @param entry The QueueEntry for the target
     */
    private void addQueueEntry(String playerName, QueueEntry entry) {
        tpQueue.put(playerName, entry);
        plugin.debug("Added new queue entry for " + entry.getPlayerName());
        if (teleportTask == null) {
            plugin.debug("No teleport task running. Starting a new one!");
            teleportTask = new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.debug("Running teleport task. Queue contains " + tpQueue.size() + " entries");
                    // Check all entries if they can teleport
                    for (Iterator<QueueEntry> i = tpQueue.values().iterator(); i.hasNext(); ) {
                        QueueEntry entry = i.next();
                        Player player = plugin.getServer().getPlayer(entry.getPlayerName());
                        if ((player != null && player.isOnline() && runEntry(player, entry) >= 0)
                                || entry.getTimeStamp() + plugin.getQueueTimeout() * 1000 < System.currentTimeMillis()) {
                            i.remove();
                        }
                    }

                    // If the queue is empty, cancel the task
                    if (tpQueue.isEmpty()) {
                        plugin.debug("Queue is empty, cancelling the teleport task!");
                        teleportTask = null;
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 20L, 20L);
        }
    }

    /**
     * Remove an entry for a player from the teleport queue
     * @param playerName The name of the player to remove
     */
    private QueueEntry removeQueueEntry(String playerName) {
        return tpQueue.remove(playerName);
    }

    public void addRequest(UUID playerId, long time) {
        tpRequests.put(playerId, time);
    }

    public Location makeTeleportSafe(Player player, Location target) {
        if (target != null && player != null && player.isOnline()) {
            if (!player.isFlying() && target.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
                if (player.getAllowFlight() || player.getGameMode() == GameMode.CREATIVE) {
                    player.setFlying(true);
                } else {
                    while (target.getBlock().getType() == Material.AIR && target.getY() >= 0) {
                        target.subtract(0, 1, 0);
                    }
                    if (target.getBlock().getType() == Material.AIR) {
                        target.setY(target.getWorld().getHighestBlockAt(target).getY());
                    }
                    target = target.getY() > 0 ? target.add(0, 1, 0) : null;
                    if (target == null) {
                        player.sendMessage(ChatColor.RED + "No safe location found!");
                        return null;
                    }
                }
            }
            return target;
        }
        return null;
    }
}
