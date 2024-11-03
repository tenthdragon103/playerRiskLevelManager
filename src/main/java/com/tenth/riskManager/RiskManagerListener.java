package com.tenth.riskManager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;


public class RiskManagerListener implements Listener {
    private final RiskManager plugin;

    public RiskManagerListener(RiskManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        //notify staff if high risk
        if (!plugin.getRiskLevels().containsKey(uuid)) {
            plugin.getRiskLevels().put(uuid, 4);
            plugin.getRiskConfig().set(uuid.toString(), 4); // Save to config
            plugin.saveRiskData(); // Save risk data to file
        }

        // Notify staff if player has a high-risk level
        int riskLevel = plugin.getRiskLevels().get(uuid);
        if (riskLevel > 4) { //notify staff in staffchat
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "staffchat A high risk player, " + player.getName() + ", has joined! Keep an eye out on them.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        int riskLevel = plugin.getRiskLevels().get(uuid);
        if (riskLevel > 4) { //notify staff in staffchat
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "staffchat A high risk player, " + player.getName() + ", has left!");
        }
    }
}
