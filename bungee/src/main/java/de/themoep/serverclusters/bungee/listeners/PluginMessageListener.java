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
        if (!event.getTag().equals("ServerClusters")) {
            return;
        }

        if (!(event.getReceiver() instanceof ProxiedPlayer)) {
            // plugin message from the client
            String senderName = event.getSender().getAddress().toString();
            if (event.getSender() instanceof ProxiedPlayer) {
                senderName = ((ProxiedPlayer) event.getSender()).getName();
            }
            plugin.getLogger().log(Level.WARNING, senderName + " tried to send plugin message on " + event.getTag() + " channel!");
            event.setCancelled(true);
            return;
        }

        ProxiedPlayer receiver = (ProxiedPlayer) event.getReceiver();
        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String subchannel = in.readUTF();

        if ("RunCommand".equals(subchannel)) {
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
            if (args.length == 1 && args[0].isEmpty()) {
                args = new String[]{};
            }
            plugin.getLogger().log(Level.INFO, receiver.getName() + " received a plugin message on channel ServerClusters/RunCommand/" + command + " '" + argsStr + "' from " + sender + (loc != null ? " at " + loc : ""));

            if (!plugin.getBukkitCommandExecutor().execute(command, sender, loc, args)) {
                plugin.getLogger().log(Level.WARNING, "Error while running ServerClusters/RunCommand/" + command + " from " + sender + "! Command failed to execute?");
            }
        } else if ("CancelTeleport".equals(subchannel)) {
            String playerId = in.readUTF();
            ProxiedPlayer player = plugin.getProxy().getPlayer(playerId);
            if (player != null) {
                plugin.getWarpManager().cancelTeleport(player);
            }
        }
    }
}
