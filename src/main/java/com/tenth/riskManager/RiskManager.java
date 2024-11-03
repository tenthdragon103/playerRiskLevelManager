package com.tenth.riskManager;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RiskManager extends JavaPlugin implements CommandExecutor {

    private Map<UUID, Integer> riskLevels;
    private File riskFile;

    FileConfiguration riskConfig;

    @Override
    public void onEnable() {
        this.riskLevels = new HashMap<>();

        setupRiskFile(); //ensure file exists, create if it doesnt
        loadRisks(); //bring file data into cache

        this.getCommand("riskManager").setExecutor(this); //register commands

        getServer().getPluginManager().registerEvents(new RiskManagerListener(this), this); //register listener

        getLogger().info(ChatColor.GREEN + "Risk Manager Enabled.");
    }

    @Override
    public void onDisable() {
        saveRiskData();
    }

    public void setupRiskFile() {
        riskFile = new File(getDataFolder(), "riskData.yml"); //riskData must be in same directory as the plugin jar
        if (!riskFile.exists()) {
            riskFile.getParentFile().mkdirs();
            saveResource("riskData.yml", false);
        }
        riskConfig = YamlConfiguration.loadConfiguration(riskFile); //idk im ngl chat gpt wrote this idk why its like this
    }

    public void loadRisks() {
        for (String key : riskConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                int riskLevel = riskConfig.getInt(key);
                riskLevels.put(uuid, riskLevel);
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid UUID in riskData.yml: " + key);
            }
        }
        getLogger().info("Loaded risk levels for " + riskLevels.size() + " players.");
    }

    public void saveRiskData() {
        if (riskLevels == null) {
            getLogger().warning("No risk data saved. Data is null.");
            return;
        }

        for (Map.Entry<UUID, Integer> entry : riskLevels.entrySet()) {
            riskConfig.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            riskConfig.save(riskFile);
        } catch (IOException e) {
            getLogger().severe("Could not save riskData.yml!");
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;

        if (!player.hasPermission("riskManager.view")) {
            player.sendMessage(ChatColor.RED + "You dont have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Not enough arguments.");
        } else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            player.sendMessage(ChatColor.GREEN + "RiskManager v1.0");
            player.sendMessage(ChatColor.GREEN + "Commands:");
            player.sendMessage(ChatColor.YELLOW + "/riskManager get <playerName>");
            player.sendMessage(ChatColor.YELLOW + "/riskManager set <playerName> <riskLevel: 1-7>");
        } else if (args.length > 3) {
            player.sendMessage(ChatColor.RED + "Too many arguments.");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("get")) {
            //get risk from riskData.yml for player with the name in args[1]. Autofill/suggest any currently online
            // players' names into the commandline if possible (when the user presses tab to go through options).
            String targetName = args[1];
            Player targetPlayer = getServer().getPlayer(targetName);

            if (targetPlayer != null) {
                UUID targetUUID = targetPlayer.getUniqueId();
                Integer riskLevel = getRiskLevels().get(targetUUID);
                player.sendMessage(ChatColor.GREEN + targetName + " has a risk level of " + riskLevel);
            } else {
                player.sendMessage(ChatColor.RED + "Player " + targetName + " not found.");
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            //get the player uuid like the "get" command and store the previous risk level, then set the player's (args[1]) risk to the integer risk given (args[2]).
            // Set risk level for the player in args[1] to the risk level in args[2]
            String targetName = args[1];
            Player targetPlayer = getServer().getPlayer(targetName);

            if (targetPlayer != null) {
                UUID targetUUID = targetPlayer.getUniqueId();
                try {
                    int newRiskLevel = Integer.parseInt(args[2]);

                    Integer previousRiskLevel = getRiskLevels().put(targetUUID, newRiskLevel);
                    player.sendMessage(ChatColor.GREEN + "Set " + targetName + "'s Risk Level to " + newRiskLevel +
                            " (previously: " + previousRiskLevel + ")");

                    saveRiskData(); // Persist the change to file

                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid risk level. Please enter a number.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Player " + targetName + " not found.");
            }
        } else {
            player.sendMessage(ChatColor.YELLOW + "Usage: /riskManager <get|set> <playerName> (riskLevel: 1-7)");
        }

        return true;
    }

    public FileConfiguration getRiskConfig() {
        return riskConfig;
    }

    public Map<UUID, Integer> getRiskLevels() {
        return riskLevels;
    }

}
