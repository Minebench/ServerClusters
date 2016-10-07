package de.themoep.serverclusters.bungee;

import com.google.common.base.Preconditions;
import net.md_5.bungee.config.Configuration;

public class LocationInfo {
    private String server;
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw = 0;
    private float pitch = 0;

    private LocationInfo(String server, String world, double x, double y, double z) {
        Preconditions.checkArgument(server != null, "server");
        Preconditions.checkArgument(world != null, "world");
        this.server = server;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public LocationInfo(String server, String world, double x, double y, double z, float yaw, float pitch) {
        this(server, world, x, y, z);
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public LocationInfo(LocationInfo location) {
        this(
                location.getServer(),
                location.getWorld(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }

    public String getServer() {
        return server;
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{server=" + getServer() + ",world=" + getWorld() + ",x=" + getX() + ",y=" + getY() + ",z=" + getZ() + ",yaw=" + getYaw() + ",pitch=" + getPitch() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof LocationInfo)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        LocationInfo other = (LocationInfo) o;
        return other.getServer().equalsIgnoreCase(getServer())
                && other.getWorld().equalsIgnoreCase(getWorld())
                && other.getX() == getX()
                && other.getY() == getY()
                && other.getZ() == getZ()
                && other.getYaw() == getYaw()
                && other.getPitch() == getPitch();
    }

    public Configuration toConfig() {
        Configuration config = new Configuration();;
        config.set("server", getServer());
        config.set("world", getWorld());
        config.set("x", getX());
        config.set("y", getY());
        config.set("z", getZ());
        config.set("pitch", getPitch());
        config.set("yaw", getYaw());
        return config;
    }
}
