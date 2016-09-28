package de.themoep.serverclusters.bungee.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import de.themoep.serverclusters.bungee.LocationInfo;
import de.themoep.serverclusters.bungee.ServerClusters;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.event.EventHandler;

import java.util.logging.Level;

/**
 * Created by Phoenix616 on 11.01.2015.
 */
public class PluginMessageListener {


    private ServerClusters plugin = null;

    public PluginMessageListener(ServerClusters plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPluginMessageReceive(PluginMessageEvent event) {
        if(event.getTag().equals("ServerClusters")) {
            ProxiedPlayer receiver = (ProxiedPlayer) event.getReceiver();
            if (receiver != null) {
                ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
                String subchannel = in.readUTF();
                if(subchannel.equals("GetPlayerLocation")) {
                    String reason = in.readUTF();
                    String sender = in.readUTF();

                    String server = receiver.getServer().getInfo().getName();
                    String world = in.readUTF();
                    double x = in.readDouble();
                    double y = in.readDouble();
                    double z = in.readDouble();
                    float yaw = in.readFloat();
                    float pitch = in.readFloat();
                    plugin.getLogger().log(Level.INFO, receiver.getName() + " received a plugin message on channel ServerClusters/GetPlayerLocation/" + reason + " from " + sender);
                    if(reason.equals("Info")) {

                    } else if(reason.equals("SetHome")) {

                    } else if(reason.equals("SetWarp")) {

                    } else if(reason.equals("SetSpawn")) {

                    }
                } else if(subchannel.equals("RunCommand")) {
                    String sender = in.readUTF();
                    String command = in.readUTF();
                    String locStr = in.readUTF();
                    String[] locParts = locStr.split(" ");
                    LocationInfo loc = null;
                    if (locParts.length == 6) {
                        ProxiedPlayer player = plugin.getProxy().getPlayer(sender);
                        if (player != null) {
                            try {
                                loc = new LocationInfo(
                                        player.getServer().getInfo().getName(),
                                        locParts[0],
                                        Double.parseDouble(locParts[1]),
                                        Double.parseDouble(locParts[2]),
                                        Double.parseDouble(locParts[3]),
                                        Float.parseFloat(locParts[4]),
                                        Float.parseFloat(locParts[5])
                                );
                            } catch (NumberFormatException e) {
                                plugin.getLogger().log(Level.WARNING, receiver.getName() + " received an invalid plugin message on channel ServerClusters/RunCommand/" + command + " from " + sender + "! Invalid location string: " + locStr);
                            }
                        }
                    }
                    String argsStr = in.readUTF();
                    String[] args = argsStr.split(" ");
                    plugin.getLogger().log(Level.INFO, receiver.getName() + " received a plugin message on channel ServerClusters/RunCommand/" + command + " '" + argsStr + "' from " + sender + (loc != null ? " at " + loc : ""));

                    plugin.getBukkitCommandExecutor().execute(command, sender, loc, args);
                }
            }
        }
    }
}
