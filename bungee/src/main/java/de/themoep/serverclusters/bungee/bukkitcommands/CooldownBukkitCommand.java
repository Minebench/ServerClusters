package de.themoep.serverclusters.bungee.bukkitcommands;

import de.themoep.serverclusters.bungee.ServerClusters;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class CooldownBukkitCommand extends BukkitCommand {

    protected Map<UUID, Long> cooldowns = new HashMap<>();

    public CooldownBukkitCommand(ServerClusters plugin, String name, String permission, String... aliases) {
        super(plugin, name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            int cooldown = getCooldown((ProxiedPlayer) sender);
            if (cooldown != 0 && !sender.hasPermission("serverclusters.bypass.cooldown")) {
                sender.sendMessage(ChatColor.RED + "Du musst noch " + ChatColor.YELLOW + cooldown + ChatColor.RED + " Sekunden warten bevor du diesen Befehl wieder benutzen kannst!");
                return;
            }
            cooldowns.put(((ProxiedPlayer) sender).getUniqueId(), System.currentTimeMillis());
            super.execute(sender, args);
        }
    }

    public int getCooldown(ProxiedPlayer player) {
        if (cooldowns.containsKey(player.getUniqueId())) {
            int cooldown = (int) (plugin.getCommandCooldown() - (System.currentTimeMillis() - cooldowns.get(player.getUniqueId())) / 1000);
            if (cooldown > 0) {
                return cooldown;
            }
            cooldowns.remove(player.getUniqueId());
            return 0;
        }
        return 0;
    }
}
