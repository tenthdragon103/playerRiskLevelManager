package com.tenth.riskManager;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public class RiskManager extends JavaPlugin implements CommandExecutor {

    private Map<UUID, Integer> riskLevels;
    private File riskFile;

    FileConfiguration riskConfig;

    @Override
    public void onEnable() {
        this.getCommand("riskManager").setExecutor(this);

        setupRiskFile();
        loadRisks();
        //these go last
        getServer().getPluginManager().registerEvents(new RiskManagerListener(this), this);

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
            saveResource("inventories.yml", false);
        }
        riskConfig = YamlConfiguration.loadConfiguration(riskFile); //load the config file for inventories
    }

    public void loadRisks() {

    }

    public void saveRiskData(){

    }

}
