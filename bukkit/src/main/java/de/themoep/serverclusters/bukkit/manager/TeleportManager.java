package de.themoep.serverclusters.bukkit.manager;

import de.themoep.serverclusters.bukkit.enums.EntryType;
import de.themoep.serverclusters.bukkit.QueueEntry;
import de.themoep.serverclusters.bukkit.ServerClustersBukkit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.logging.Level;

/**
 * Created by Phoenix616 on 08.01.2015.
 */
public class TeleportManager implements Listener {

    private HashMap<String, QueueEntry> tpQueue = new HashMap<String, QueueEntry>();

    private ServerClustersBukkit plugin;

    public TeleportManager(ServerClustersBukkit plugin) {
        this.plugin = plugin;
    }

    /**
     * Teleport the player if he has an entry in the teleport queue which is younger then 15 seconds
     */
    @EventHandler
    public void OnPlayerLogin(PlayerJoinEvent event) {
        if(this.isQueued(event.getPlayer().getName(), 15)) {
            QueueEntry entry = this.getQueueEntry(event.getPlayer().getName());
            if(entry.getType() == EntryType.LOCATION) {
                Location targetloc = entry.getLocation();
                if(targetloc != null)
                    this.teleport(event.getPlayer(), targetloc);
            } else if(entry.getType() == EntryType.STRING){
                String s = entry.getString();
                if(s != null) {
                    Player targetplayer = Bukkit.getPlayer(s);
                    if(targetplayer != null && targetplayer.isOnline()) {
                        this.teleport(event.getPlayer(), targetplayer);
                    }
                }
            }
        }
    }

    /**
     * Teleports a player to a target location.
     * If the player is not online it will queue it 'til he is online and teleport him then.
     * @param playername The name of the player to teleport
     * @param target The location where to teleport the player to
     * @return byte: 1 if the player was teleported,
     *               0 if the teleport was queued
     */
    public byte teleport(String playername, Location target) {
        Player player = Bukkit.getPlayer(playername);
        if(player != null && player.isOnline()) {
            teleport(player, target);
            return 1;
        } else {
            this.addQueueEntry(playername, target);
            return 0;
        }
    }

    /**
     * Teleports a player to a target location.
     * @param player The player to teleport
     * @param target The location to teleport to
     * @return byte: 1 if the player was teleported,
     *              -1 if the player or target was not found was not found or is null
     */
    public byte teleport(Player player, Location target) {
        if(target != null && player != null && player.isOnline()) {
            player.teleport(target);
            removeQueueEntry(player.getName());
            plugin.getLogger().log(Level.INFO, "Teleported " + player.getName() + " to ([" + target.getWorld().getName() + "] " + target.getX() + ", " + target.getY() + ", " + target.getZ() + ")");
            return 1;
        }
        return -1;
    }

    /**
     * Teleports a player to a target player.
     * If the player is not online it will queue it 'til he is online and teleport him then.
     * @param playername Name of the player to teleport
     * @param targetname Name of the player to teleport to
     * @return byte: 1 if the player was teleported,
     *               0 if the teleport was queued,
     *              -1 if the target was not found
     */
    public byte teleport(String playername, String targetname) {
        Player target = Bukkit.getPlayer(targetname);
        if(target != null && target.isOnline()) {
            Player player = Bukkit.getPlayer(playername);
            if(player != null && player.isOnline()) {
                teleport(player, target);
                return 1;
            } else {
                addQueueEntry(playername, targetname);
                return 0;
            }
        }
        return -1;
    }

    /**
     * Teleports a player to a target player.
     * @param player The player to teleport
     * @param target The player to teleport to
     * @return byte: 1 if the player was teleported,
     *              -1 if the player or the target was not found or is null
     */
    public byte teleport(Player player, Player target) {
        if(target != null && target.isOnline() && player != null && player.isOnline()) {
            player.teleport(target);
            removeQueueEntry(player.getName());
            Location loc = target.getLocation();
            plugin.getLogger().log(Level.INFO, "Teleported " + player.getName() + " to " + target.getName() + " ([" + loc.getWorld().getName() + "] " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ")");
            return 1;
        }
        return -1;
    }

    /**
     * Get if a player is queued for teleport
     * @param playername Name of the player to check
     * @return boolean: true if queued,
     *                  false if not
     */
    public boolean isQueued(String playername) {
        return tpQueue.containsKey(playername);
    }

    /**
     * Get if a player is queued for teleport
     * @param player Name of the player to check
     * @return boolean: true if queued,
     *                  false if not
     */
    public boolean isQueued(Player player) {
        return isQueued(player.getName());
    }

    /**
     * Get if a player was queued for teleport in the last x seconds
     * @param playername Name of the player to check
     * @param x How far back the player has to be gotten queued
     * @return boolean: true if queued in the last x seconds,
     *                  false if not
     */
    public boolean isQueued(String playername, int x) {
        if(isQueued(playername)) {
            QueueEntry entry = this.getQueueEntry(playername);
            return entry != null && entry.getTimeStamp() + x * 1000 > System.currentTimeMillis();
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
     * @param targetloc The targeted location
     */
    private void addQueueEntry(String playername, Location targetloc) {
        tpQueue.put(playername, new QueueEntry(targetloc));
    }

    /**
     * Remove an entry for a player from the teleport queue
     * @param playername The name of the player to remove
     */
    private void removeQueueEntry(String playername) {
        tpQueue.remove(playername);
    }

}
