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

    private static Map<UUID, RiskData> riskLevels;
    private File riskFile;

    FileConfiguration riskConfig;

    @Override
    public void onEnable() {
        riskLevels = new HashMap<>();

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
        riskFile = new File(getDataFolder(), "riskData.yml");
        if (!riskFile.exists()) {
            riskFile.getParentFile().mkdirs();
            saveResource("riskData.yml", false);
        }
        riskConfig = YamlConfiguration.loadConfiguration(riskFile);
    }

    public void loadRisks() {
        for (String key : riskConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                int riskLevel = riskConfig.getInt(key + ".riskLevel");
                boolean isFlagged = riskConfig.getBoolean(key + ".isFlagged", false); // Default to false if not set
                riskLevels.put(uuid, new RiskData(riskLevel, isFlagged));
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid UUID in riskData.yml: " + key);
            }
        }
        getLogger().info("Loaded risk data for " + riskLevels.size() + " players.");
    }


    public void saveRiskData() {
        if (riskLevels == null) {
            getLogger().warning("No risk data saved. Data is null.");
            return;
        }

        for (Map.Entry<UUID, RiskData> entry : riskLevels.entrySet()) {
            String key = entry.getKey().toString();
            RiskData data = entry.getValue();
            riskConfig.set(key + ".riskLevel", data.getRiskLevel());
            riskConfig.set(key + ".isFlagged", data.isFlagged());
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
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("riskManager.view")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Not enough arguments.");
        } else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            player.sendMessage(ChatColor.GREEN + "RiskManager v1.0");
            player.sendMessage(ChatColor.GREEN + "Commands:");
            player.sendMessage(ChatColor.YELLOW + "/riskManager get <playerName>");
            player.sendMessage(ChatColor.YELLOW + "/riskManager set <playerName> <riskLevel: 1-7>");
            player.sendMessage(ChatColor.YELLOW + "/riskManager toggleFlag <playerName>");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("get")) {
            String targetName = args[1];
            Player targetPlayer = getServer().getPlayer(targetName);

            if (targetPlayer != null) {
                UUID targetUUID = targetPlayer.getUniqueId();
                RiskData data = riskLevels.get(targetUUID);

                if (data != null) {
                    player.sendMessage(ChatColor.GREEN + targetName + " has a risk level of " + data.getRiskLevel() +
                            " and is " + (data.isFlagged() ? "flagged" : "not flagged") + ".");
                } else {
                    player.sendMessage(ChatColor.RED + targetName + " does not have risk data.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Player " + targetName + " not found.");
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            String targetName = args[1];
            Player targetPlayer = getServer().getPlayer(targetName);

            if (targetPlayer != null) {
                UUID targetUUID = targetPlayer.getUniqueId();
                try {
                    int newRiskLevel = Integer.parseInt(args[2]);

                    if (newRiskLevel < 1 || newRiskLevel > 7) {
                        player.sendMessage(ChatColor.RED + "Risk level must be between 1 and 7.");
                        return true;
                    }

                    RiskData data = riskLevels.getOrDefault(targetUUID, new RiskData(0, false));
                    int previousRiskLevel = data.getRiskLevel();
                    data.setRiskLevel(newRiskLevel);
                    riskLevels.put(targetUUID, data);

                    player.sendMessage(ChatColor.GREEN + "Set " + targetName + "'s risk level to " + newRiskLevel +
                            " (previously: " + previousRiskLevel + ").");

                    saveRiskData(); // Persist the change to file
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid risk level. Please enter a number.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Player " + targetName + " not found.");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("toggleFlag")) {
            String targetName = args[1];
            Player targetPlayer = getServer().getPlayer(targetName);

            if (targetPlayer != null) {
                UUID targetUUID = targetPlayer.getUniqueId();
                RiskData data = riskLevels.getOrDefault(targetUUID, new RiskData(0, false));
                boolean currentFlag = data.isFlagged();
                data.setFlagged(!currentFlag);
                riskLevels.put(targetUUID, data);

                player.sendMessage(ChatColor.GREEN + "Player " + targetName +
                        " is now " + (data.isFlagged() ? "flagged" : "no longer flagged") + ".");

                saveRiskData(); // Persist the change to file
            } else {
                player.sendMessage(ChatColor.RED + "Player " + targetName + " not found.");
            }
        } else {
            player.sendMessage(ChatColor.YELLOW + "Usage: /riskManager <get|set|toggleFlag> <playerName> (riskLevel: 1-7)");
        }

        return true;
    }

    public FileConfiguration getRiskConfig() {
        return riskConfig;
    }

    public Map<UUID, RiskData> getRiskLevels() {
        return riskLevels;
    }

}
