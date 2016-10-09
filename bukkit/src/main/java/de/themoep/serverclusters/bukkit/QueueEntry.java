package de.themoep.serverclusters.bukkit;

import de.themoep.serverclusters.bukkit.enums.EntryType;
import org.bukkit.Location;

/**
 * Created by Phoenix616 on 08.01.2015.
 */
public class QueueEntry {

    private final Location loc;
    private final String string;
    private final EntryType type;
    private final long timestamp;

    /**
     * An location entry in a player queue.
     * @param loc The location
     */
    public QueueEntry(Location loc) {
        this(null, loc, EntryType.STRING);
    }

    /**
     * A string entry in a player queue.
     * @param string The location
     */
    public QueueEntry(String string) {
        this(string, null, EntryType.STRING);
    }

    private QueueEntry(String string, Location loc, EntryType type) {
        this.string = string;
        this.loc = loc;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
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
}
