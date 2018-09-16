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


    private ServerClusters plugin;

    public PluginMessageListener(ServerClusters plugin) {
        this.plugin = plugin;
        plugin.getProxy().registerChannel("sc:runcommand");
        plugin.getProxy().registerChannel("sc:cancelteleport");
    }

    @EventHandler
    public void onPluginMessageReceive(PluginMessageEvent event) {
        if (!event.getTag().startsWith("sc:")) {
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

        if ("sc:runcommand".equals(event.getTag())) {
            String sender = in.readUTF();
            String command = in.readUTF();
            LocationInfo loc = null;

            // Location was send
            if (in.readBoolean()) {
                loc = new LocationInfo(
                        receiver.getServer().getInfo().getName(),
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
        } else if ("sc:cancelteleport".equals(event.getTag())) {
            String playerName = in.readUTF();
            ProxiedPlayer player = plugin.getProxy().getPlayer(playerName);
            if (player != null) {
                plugin.getTeleportManager().cancelTeleport(player);
            }
            plugin.getLogger().log(Level.INFO, receiver.getName() + " received a plugin message on channel ServerClusters/CancelTeleport '" + playerName + "'");
        }
    }
}
