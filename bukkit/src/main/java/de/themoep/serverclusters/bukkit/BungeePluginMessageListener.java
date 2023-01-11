package de.themoep.serverclusters.bukkit;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.UUID;

/**
 * Created by Phoenix616 on 08.01.2015.
 */
public class BungeePluginMessageListener implements PluginMessageListener {

    ServerClustersBukkit plugin = null;

    public BungeePluginMessageListener(ServerClustersBukkit plugin) {
        this.plugin = plugin;

        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "sc:tptoplayer", this);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "sc:tptolocation", this);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "sc:getlocation", this);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "sc:addrequest", this);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "sc:playerlocation");
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "sc:error");
    }

    public void onPluginMessageReceived(String channel, Player receiver, byte[] message) {
        if (channel.startsWith("sc:")) {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);

            plugin.debug(receiver.getName() + " received plugin message on channel '" + channel + "'");

            if ("sc:tptoplayer".equals(channel)) {
                String playername = in.readUTF();
                String targetname = in.readUTF();
                plugin.getTeleportManager().teleport(playername, targetname);

            } else if ("sc:tptolocation".equals(channel)) {
                String playername = in.readUTF();
                String worldname = in.readUTF();
                double x = in.readDouble();
                double y = in.readDouble();
                double z = in.readDouble();
                float yaw = in.readFloat();
                float pitch = in.readFloat();
                World world = plugin.getServer().getWorld(worldname);
                if (world == null) {
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("unknown world");
                    out.writeUTF(worldname);
                    receiver.sendPluginMessage(this.plugin, "sc:error", out.toByteArray());
                    return;
                }

                Location loc = new Location(plugin.getServer().getWorld(worldname), x, y, z, yaw, pitch);
                plugin.getTeleportManager().teleport(playername, loc);

            } else if ("sc:getlocation".equals(channel)) {
                String reason = in.readUTF();
                String sender = in.readUTF();
                UUID playerId = new UUID(in.readLong(), in.readLong());
                Player player = plugin.getServer().getPlayer(playerId);

                ByteArrayDataOutput out = ByteStreams.newDataOutput();

                out.writeUTF(reason);
                out.writeUTF(sender);
                if (player == null) {
                    out.writeUTF("playernotfound");
                } else {
                    Location loc = player.getLocation();
                    out.writeUTF(loc.getWorld().getName());
                    out.writeDouble(loc.getX());
                    out.writeDouble(loc.getY());
                    out.writeDouble(loc.getZ());
                    out.writeFloat(loc.getYaw());
                    out.writeFloat(loc.getPitch());
                }

                receiver.sendPluginMessage(this.plugin, "sc:playerlocation", out.toByteArray());
            } else if ("sc:addtprequest".equals(channel)) {
                UUID playerId = new UUID(in.readLong(), in.readLong());
                plugin.getTeleportManager().addRequest(playerId, System.currentTimeMillis());
            }
        }
    }
}
