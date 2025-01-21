package com.tenth.riskManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

import static org.bukkit.Bukkit.getOnlinePlayers;


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
            plugin.getRiskLevels().put(uuid, new RiskData(4, false));
            plugin.getRiskConfig().set(uuid.toString(), 4); // Save to config
            plugin.saveRiskData(); // Save risk data to file
        }

        // Notify staff if player has a high-risk level
        boolean isRisk = plugin.getRiskLevels().get(uuid).isFlagged();
        if (isRisk) { //notify staff
            for (Player p : getOnlinePlayers()) {
                if (p.hasPermission("riskManager.view")) {
                    p.sendMessage("Player " + player.getName() + " is marked as high risk. Keep an eye out on them.");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        boolean isRisk = plugin.getRiskLevels().get(uuid).isFlagged();
        if (isRisk) { //notify staff
            for (Player p : getOnlinePlayers()) {
                if (p.hasPermission("riskManager.view")) {
                    p.sendMessage("High risk player " + player.getName() + " has left");
                }
            }
        }
    }
}
