package de.themoep.serverclusters.bungee.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import de.themoep.serverclusters.bungee.LocationInfo;
import de.themoep.serverclusters.bungee.ServerClusters;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.logging.Level;

/**
 * Created by Phoenix616 on 11.01.2015.
 */
public class PluginMessageListener implements Listener {


    private ServerClusters plugin = null;

    public PluginMessageListener(ServerClusters plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPluginMessageReceive(PluginMessageEvent event) {
        if (event.getTag().equals("ServerClusters")) {
            if (event.getReceiver() instanceof ProxiedPlayer) {
                ProxiedPlayer receiver = (ProxiedPlayer) event.getReceiver();
                ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
                String subchannel = in.readUTF();
                if (subchannel.equals("GetPlayerLocation")) {
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
                    if (reason.equals("Info")) {
                        // /getpos command or something like that
                    } else if (reason.equals("SetHome")) {
                        // set a home, not implemented yet and I'm not even sure if this is the correct way to do it
                    } else if (reason.equals("SetWarp")) {
                        // set warp, currently not possible, may get implemented otherwise
                    } else if (reason.equals("SetSpawn")) {
                        // look above
                    }
                } else if (subchannel.equals("RunCommand")) {
                    String sender = in.readUTF();
                    String command = in.readUTF();
                    LocationInfo loc = null;

                    // Sender is player
                    if (in.readBoolean()) {
                        ProxiedPlayer player = plugin.getProxy().getPlayer(sender);
                        if (player == null) {
                            plugin.getLogger().log(Level.WARNING, receiver.getName() + " received an invalid plugin message on channel ServerClusters/RunCommand/" + command + " from " + sender + "! Could not find player for that sender?");
                            ((ProxiedPlayer) event.getReceiver()).sendMessage(ChatColor.RED + "Something went wrong while processing this command! Please contact an admin!");
                            return;
                        }

                        loc = new LocationInfo(
                                player.getServer().getInfo().getName(),
                                in.readUTF(),
                                in.readDouble(),
                                in.readDouble(),
                                in.readDouble(),
                                in.readFloat(),
                                in.readFloat()
                        );
                    }

                    String argsStr = in.readUTF();
                    String[] args = argsStr.split(" ");
                    plugin.getLogger().log(Level.INFO, receiver.getName() + " received a plugin message on channel ServerClusters/RunCommand/" + command + " '" + argsStr + "' from " + sender + (loc != null ? " at " + loc : ""));

                    if (!plugin.getBukkitCommandExecutor().execute(command, sender, loc, args)) {
                        plugin.getLogger().log(Level.WARNING, "Error while running ServerClusters/RunCommand/" + command + " from " + sender + "! Command failed to execute?");
                    }
                }
            } else {
                // plugin message from the client
                event.setCancelled(true);
            }
        }
    }
}
