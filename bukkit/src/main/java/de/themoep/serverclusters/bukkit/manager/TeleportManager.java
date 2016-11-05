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
import org.bukkit.block.Block;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Phoenix616 on 08.01.2015.
 */
public class TeleportManager implements Listener {

    private HashMap<String, QueueEntry> tpQueue = new HashMap<>();
    private Map<UUID, Long> tpRequests = new HashMap<>();

    private ServerClustersBukkit plugin;

    public TeleportManager(ServerClustersBukkit plugin) {
        this.plugin = plugin;
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
        out.writeUTF("CancelTeleport");
        out.writeUTF(player.getName());
        player.sendPluginMessage(plugin, "ServerClusters", out.toByteArray());
    }

    /**
     * Teleport the player if he has an entry in the teleport queue
     */
    @EventHandler
    public void OnPlayerLogin(PlayerJoinEvent event) {
        if (isQueued(event.getPlayer())) {
            QueueEntry entry = getQueueEntry(event.getPlayer().getName());
            if (entry.getType() == EntryType.LOCATION) {
                Location targetloc = entry.getLocation();
                if (targetloc != null) {
                    teleport(event.getPlayer(), targetloc);
                }
            } else if (entry.getType() == EntryType.STRING) {
                String s = entry.getString();
                if (s != null) {
                    Player targetplayer = plugin.getServer().getPlayer(s);
                    if (targetplayer != null && targetplayer.isOnline()) {
                        teleport(event.getPlayer(), targetplayer);
                    }
                }
            }
        }
    }

    /**
     * Teleports a player to a target location.
     * If the player is not online it will queue it 'til he is online and teleport him then.
     * @param playername The name of the player to teleport
     * @param target     The location where to teleport the player to
     */
    public void teleport(final String playername, final Location target) {
        addQueueEntry(playername, target);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isQueued(playername)) {
                    Player player = plugin.getServer().getPlayer(playername);
                    if (player != null && player.isOnline()) {
                        teleport(player, target);
                    }
                }
            }
        }.runTaskLater(plugin, 10);
    }

    /**
     * Teleports a player to a target location.
     * @param player The player to teleport
     * @param target The location to teleport to
     * @return byte: 1 if the player was teleported,
     * -1 if the player or target was not found was not found or is null
     */
    public byte teleport(Player player, Location target) {
        if (target != null && player != null && player.isOnline()) {
            Block block = target.getBlock().getRelative(BlockFace.DOWN);
            if (block.getType() == Material.AIR) {
                if (player.getAllowFlight() || player.getGameMode() == GameMode.CREATIVE) {
                    player.setFlying(true);
                } else {
                    target = getSafeLocation(block);
                    if (target == null) {
                        player.sendMessage(ChatColor.RED + "No safe location found!");
                        return -1;
                    }
                    block = target.getBlock().getRelative(BlockFace.DOWN);
                }
            }
            player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
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
     * @param playername Name of the player to teleport
     * @param targetname Name of the player to teleport to
     */
    public void teleport(final String playername, final String targetname) {
        addQueueEntry(playername, targetname);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isQueued(playername)) {
                    Player target = plugin.getServer().getPlayer(targetname);
                    if (target != null && target.isOnline()) {
                        Player player = plugin.getServer().getPlayer(playername);
                        if (player != null && player.isOnline()) {
                            teleport(player, target);
                        }
                    }
                }
            }
        }.runTaskLater(plugin, 10);
    }

    /**
     * Teleports a player to a target player.
     * @param player The player to teleport
     * @param target The player to teleport to
     * @return byte: 1 if the player was teleported,
     * -1 if the player or the target was not found or is null
     */
    public byte teleport(Player player, Player target) {
        if (target != null && target.isOnline() && player != null && player.isOnline()) {
            Location loc = target.getLocation();
            Block block = loc.getBlock().getRelative(BlockFace.DOWN);
            if (!player.isFlying() && (target.isFlying() || block.getType() == Material.AIR)) {
                if (player.getAllowFlight() || player.getGameMode() == GameMode.CREATIVE) {
                    player.setFlying(true);
                } else {
                    loc = getSafeLocation(block);
                    if (loc == null) {
                        player.sendMessage(ChatColor.RED + "No safe location found!");
                        return -1;
                    }
                }
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
     * @param playername Name of the player to check
     * @return boolean: true if queued,
     * false if not
     */
    public boolean isQueued(String playername) {
        return tpQueue.containsKey(playername);
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
     * @param playername Name of the player to check
     * @param x          How far back the player has to be gotten queued
     * @return boolean: true if queued in the last x seconds, false if not
     */
    public boolean isQueued(String playername, int x) {
        if (isQueued(playername)) {
            QueueEntry entry = getQueueEntry(playername);
            if (entry != null && entry.getTimeStamp() + x * 1000 > System.currentTimeMillis()) {
                return true;
            }
            removeQueueEntry(playername);
        }
        return false;
    }

    /**
     * Get the queue entry of a player
     * @param playername The name of the player
     * @return The full QueueEntry to the player, null if he doesn't have one
     */
    public QueueEntry getQueueEntry(String playername) {
        return tpQueue.get(playername);
    }

    /**
     * Add a new player entry to the teleport queue
     * @param playername The name of the player to queue
     * @param targetname The name of the targeted player
     */
    private void addQueueEntry(String playername, String targetname) {
        tpQueue.put(playername, new QueueEntry(targetname));
    }

    /**
     * Add a new location entry to the teleport queue
     * @param playername The name of the player to queue
     * @param targetloc  The targeted location
     */
    private void addQueueEntry(String playername, Location targetloc) {
        tpQueue.put(playername, new QueueEntry(targetloc));
    }

    /**
     * Remove an entry for a player from the teleport queue
     * @param playername The name of the player to remove
     */
    private QueueEntry removeQueueEntry(String playername) {
        return tpQueue.remove(playername);
    }

    public void addRequest(UUID playerId, long time) {
        tpRequests.put(playerId, time);
    }

    public Location getSafeLocation(Block block) {
        while (block.getType() == Material.AIR && block.getY() >= 0) {
            block = block.getRelative(BlockFace.DOWN);
        }
        if (block.getType() == Material.AIR) {
            block = block.getWorld().getHighestBlockAt(block.getLocation());
        }
        return block == null || block.getType() == Material.AIR ? null : block.getLocation().add(0, 1, 0);
    }
}
