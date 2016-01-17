package de.themoep.serverclusters.bungee.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import de.themoep.serverclusters.bungee.ServerClusters;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.event.EventHandler;
import org.apache.commons.lang.StringUtils;

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
                    Double x = in.readDouble();
                    Double y = in.readDouble();
                    Double z = in.readDouble();
                    Float yaw = in.readFloat();
                    Float pitch = in.readFloat();
                    plugin.getLogger().log(Level.INFO, receiver.getName() + " received a plugin message on channel ServerClusters/GetPlayerLocation/" + reason + " from " + sender);
                    if(reason.equals("Info")) {

                    } else if(reason.equals("SetHome")) {

                    } else if(reason.equals("SetWarp")) {

                    } else if(reason.equals("SetSpawn")) {

                    }
                } else if(subchannel.equals("RunCommand")) {
                    String sender = in.readUTF();
                    String command = in.readUTF();
                    String argsStr = in.readUTF();
                    String[] args = StringUtils.split(argsStr, ' ');
                    plugin.getLogger().log(Level.INFO, receiver.getName() + " received a plugin message on channel ServerClusters/RunCommand/" + command + " '" + argsStr + "' from " + sender);

                    plugin.getBukkitCommandExecutor().execute(command, sender, args);
                }
            }
        }
    }
}
