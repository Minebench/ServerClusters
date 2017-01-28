package de.themoep.serverclusters.bukkit;

import de.themoep.serverclusters.bukkit.enums.EntryType;
import org.bukkit.Location;

/**
 * Created by Phoenix616 on 08.01.2015.
 */
public class QueueEntry {

    private final String playerName;
    private final Location loc;
    private final String string;
    private final EntryType type;
    private final long timestamp;

    /**
     * An location entry in a player queue.
     * @param playerName The name of the player that this entry is for
     * @param loc The location
     */
    public QueueEntry(String playerName, Location loc) {
        this(playerName, null, loc, EntryType.LOCATION);
    }

    /**
     * A string entry in a player queue.
     * @param playerName The name of the player that this entry is for
     * @param string The location
     */
    public QueueEntry(String playerName, String string) {
        this(playerName, string, null, EntryType.STRING);
    }

    private QueueEntry(String playerName, String string, Location loc, EntryType type) {
        this.playerName = playerName;
        this.string = string;
        this.loc = loc;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Get the name of the player that this entry is for
     * @return The player name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Gets the type of the entry
     * @return EntryType: the type
     */
    public EntryType getType() {
        return type;
    }

    /**
     * Gets the timestamp associated with the entry
     * @return long: The timestamp
     */
    public long getTimeStamp() {
        return timestamp;
    }

    /**
     * Gets the location associated with the entry
     * @return Location: the location, null if no location entry
     */
    public Location getLocation() {
        return loc;
    }

    /**
     * Gets the string associated with the entry
     * @return String: the string, null if no string entry
     */
    public String getString() {
        return string;
    }

    /**
     * Make a copy of this entry because #CloneIsBroken
     * @return A copy of this QueueEntry
     */
    public QueueEntry copy() {
        return new QueueEntry(playerName, string, loc, type);
    }
}
