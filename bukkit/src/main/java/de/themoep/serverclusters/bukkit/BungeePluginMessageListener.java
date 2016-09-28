package de.themoep.serverclusters.bukkit;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

/**
 * Created by Phoenix616 on 08.01.2015.
 */
public class BungeePluginMessageListener implements PluginMessageListener {

    ServerClustersBukkit plugin = null;

    public BungeePluginMessageListener(ServerClustersBukkit plugin) {
        this.plugin = plugin;
    }

    public void onPluginMessageReceived(String channel, Player recevier, byte[] message) {
        if(channel.equals("ServerClusters")) {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String subchannel = in.readUTF();

            if(subchannel.equals("TeleportToPlayer")) {
                String playername = in.readUTF();
                String targetname = in.readUTF();
                plugin.getTeleportManager().teleport(playername, targetname);
            } else if (subchannel.equals("TeleportToLocation")) {
                String playername = in.readUTF();
                String worldname = in.readUTF();
                double x = in.readDouble();
                double y = in.readDouble();
                double z = in.readDouble();
                float yaw = in.readFloat();
                float pitch = in.readFloat();
                Location loc = new Location(Bukkit.getWorld(worldname),x,y,z,yaw,pitch);
                plugin.getTeleportManager().teleport(playername, loc);
            } else if (subchannel.equals("GetPlayerLocation")) {
                String reason = in.readUTF();
                String sender = in.readUTF();
                Location loc = recevier.getLocation();

                ByteArrayDataOutput out = ByteStreams.newDataOutput();

                out.writeUTF("GetPlayerLocation");
                out.writeUTF(reason);
                out.writeUTF(sender);
                out.writeUTF(loc.getWorld().getName());
                out.writeDouble(loc.getX());
                out.writeDouble(loc.getY());
                out.writeDouble(loc.getZ());
                out.writeFloat(loc.getYaw());
                out.writeFloat(loc.getPitch());

                recevier.sendPluginMessage(this.plugin, "ServerClusters", out.toByteArray());
            }
        }
    }
}
