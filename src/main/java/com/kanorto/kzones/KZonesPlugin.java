package com.kanorto.kzones;

import com.kanorto.kzones.commands.KZonesCommand;
import com.kanorto.kzones.listeners.PlayerMoveListener;
import com.kanorto.kzones.managers.ZoneManager;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

public class KZonesPlugin extends JavaPlugin {
    
    private static KZonesPlugin instance;
    private ZoneManager zoneManager;
    
    @Override
    public void onLoad() {
        instance = this;
    }
    
    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Check if WorldGuard is available
        if (!getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            getLogger().severe("WorldGuard not found! This plugin requires WorldGuard to work.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Initialize managers
        zoneManager = new ZoneManager(this);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        
        // Register commands
        KZonesCommand commandHandler = new KZonesCommand(this);
        getCommand("kzones").setExecutor(commandHandler);
        getCommand("kzones").setTabCompleter(commandHandler);
        
        getLogger().info("kZones has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Clean up
        if (zoneManager != null) {
            zoneManager.stopAllSequences();
        }
        
        getLogger().info("kZones has been disabled!");
    }
    
    /**
     * Reload the plugin configuration
     */
    public void reloadPlugin() {
        reloadConfig();
        zoneManager.reload();
        getLogger().info("kZones configuration reloaded!");
    }
    
    /**
     * Save configuration asynchronously to avoid blocking the main thread
     */
    public void saveConfigAsync() {
        new BukkitRunnable() {
            @Override
            public void run() {
                saveConfig();
            }
        }.runTaskAsynchronously(this);
    }
    
    // Getters
    public static KZonesPlugin getInstance() {
        return instance;
    }
    
    public ZoneManager getZoneManager() {
        return zoneManager;
    }
}
