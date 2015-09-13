package de.themoep.serverclusters.bukkit;

import de.themoep.serverclusters.bukkit.enums.EntryType;
import org.bukkit.Location;

/**
 * Created by Phoenix616 on 08.01.2015.
 */
public class QueueEntry {

    private Location loc = null;
    private String string = null;
    private EntryType type = null;
    private Long timestamp;

    /**
     * An location entry in a player queue.
     * @param loc The location
     */
    public QueueEntry(Location loc) {
        this.loc = loc;
        this.type = EntryType.LOCATION;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * A string entry in a player queue.
     * @param string The location
     */
    public QueueEntry(String string) {
        this.string = string;
        this.type = EntryType.STRING;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Gets the type of the entry
     * @return EntryType: the type
     */
    public EntryType getType() {
        return this.type;
    }

    /**
     * Gets the timestamp associated with the entry
     * @return long: The timestamp
     */
    public long getTimeStamp() {
        return this.timestamp;
    }

    /**
     * Gets the location associated with the entry
     * @return Location: the location, null if no location entry
     */
    public Location getLocation() {
        return this.loc;
    }

    /**
     * Gets the string associated with the entry
     * @return String: the string, null if no string entry
     */
    public String getString() {
        return this.string;
    }
}
