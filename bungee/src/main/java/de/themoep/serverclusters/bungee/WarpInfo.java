package de.themoep.serverclusters.bungee;

public class WarpInfo extends LocationInfo {
    private String name;

    public WarpInfo(String name, String server, String world, double x, double y, double z, float yaw, float pitch) {
        super(server, world, x, y, z, yaw, pitch);
        this.name = name;
    }

    public WarpInfo(String name, LocationInfo location) {
        super(location);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{name=" + getName() + ",server=" + getServer() + ",world=" + getWorld() + ",x=" + getX() + ",y=" + getY() + ",z=" + getZ() + ",yaw=" + getYaw() + ",pitch=" + getPitch() + "}";
    }
}
