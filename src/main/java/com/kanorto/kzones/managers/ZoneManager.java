package com.kanorto.kzones.managers;

import com.kanorto.kzones.KZonesPlugin;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ZoneManager {
    
    private final KZonesPlugin plugin;
    private final Map<String, List<String>> zoneSequences;
    private final Map<UUID, PlayerZoneData> playerData;
    private boolean sequenceActive;
    
    public ZoneManager(KZonesPlugin plugin) {
        this.plugin = plugin;
        this.zoneSequences = new HashMap<>();
        this.playerData = new ConcurrentHashMap<>();
        this.sequenceActive = false;
        loadSequences();
    }
    
    /**
     * Load zone sequences from config
     */
    public void loadSequences() {
        zoneSequences.clear();
        
        ConfigurationSection sequencesSection = plugin.getConfig().getConfigurationSection("zone-sequences");
        if (sequencesSection != null) {
            for (String sequenceName : sequencesSection.getKeys(false)) {
                List<String> regions = sequencesSection.getStringList(sequenceName);
                if (!regions.isEmpty()) {
                    zoneSequences.put(sequenceName, regions);
                    plugin.getLogger().info("Loaded sequence '" + sequenceName + "' with " + regions.size() + " zones");
                }
            }
        }
        
        if (zoneSequences.isEmpty()) {
            plugin.getLogger().warning("No zone sequences configured! Please configure sequences in config.yml");
        }
    }
    
    /**
     * Start the zone sequence for all online players
     */
    public void startSequence() {
        if (zoneSequences.isEmpty()) {
            plugin.getLogger().warning("Cannot start sequence: No sequences configured!");
            return;
        }
        
        sequenceActive = true;
        playerData.clear();
        
        // Initialize all online players at the start of the first sequence
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!player.hasPermission("kzones.bypass")) {
                initializePlayer(player);
            }
        }
        
        plugin.getLogger().info("Zone sequence started for all players");
    }
    
    /**
     * Stop the zone sequence
     */
    public void stopSequence() {
        sequenceActive = false;
        playerData.clear();
        plugin.getLogger().info("Zone sequence stopped");
    }
    
    /**
     * Stop all sequences (used on plugin disable)
     */
    public void stopAllSequences() {
        stopSequence();
    }
    
    /**
     * Initialize a player's zone data
     */
    public void initializePlayer(Player player) {
        if (zoneSequences.isEmpty()) {
            return;
        }
        
        // Use the first sequence by default
        String firstSequenceName = zoneSequences.keySet().iterator().next();
        List<String> sequence = zoneSequences.get(firstSequenceName);
        
        PlayerZoneData data = new PlayerZoneData(firstSequenceName, sequence);
        playerData.put(player.getUniqueId(), data);
    }
    
    /**
     * Move player to the next zone in sequence
     */
    public boolean moveToNextZone(UUID playerId) {
        PlayerZoneData data = playerData.get(playerId);
        if (data == null) {
            return false;
        }
        
        return data.moveToNext();
    }
    
    /**
     * Check if a player can enter a specific region
     */
    public boolean canEnterRegion(Player player, String regionName) {
        if (!sequenceActive) {
            return true; // No restrictions if sequence is not active
        }
        
        if (player.hasPermission("kzones.bypass")) {
            return true; // Bypass permission
        }
        
        PlayerZoneData data = playerData.get(player.getUniqueId());
        if (data == null) {
            // Player not initialized, allow entry
            return true;
        }
        
        // Get current expected zone
        String expectedZone = data.getCurrentZone();
        if (expectedZone == null) {
            // Player has completed sequence
            return true;
        }
        
        // Check if the region matches the expected zone
        return regionName.equalsIgnoreCase(expectedZone);
    }
    
    /**
     * Check if a player can leave their current region
     */
    public boolean canLeaveRegion(Player player, String regionName) {
        if (!sequenceActive) {
            return true;
        }
        
        if (player.hasPermission("kzones.bypass")) {
            return true;
        }
        
        PlayerZoneData data = playerData.get(player.getUniqueId());
        if (data == null) {
            return true;
        }
        
        String currentZone = data.getCurrentZone();
        if (currentZone == null) {
            return true;
        }
        
        // Don't allow leaving the current zone
        return !regionName.equalsIgnoreCase(currentZone);
    }
    
    /**
     * Get regions at a specific location
     */
    public Set<String> getRegionsAt(org.bukkit.Location location) {
        Set<String> regions = new HashSet<>();
        
        try {
            RegionManager regionManager = WorldGuard.getInstance()
                    .getPlatform()
                    .getRegionContainer()
                    .get(BukkitAdapter.adapt(location.getWorld()));
            
            if (regionManager != null) {
                Location loc = BukkitAdapter.adapt(location);
                ApplicableRegionSet set = regionManager.getApplicableRegions(loc.toVector().toBlockPoint());
                
                for (ProtectedRegion region : set) {
                    regions.add(region.getId());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting regions at location: " + e.getMessage());
        }
        
        return regions;
    }
    
    /**
     * Get player's zone data
     */
    public PlayerZoneData getPlayerData(UUID playerId) {
        return playerData.get(playerId);
    }
    
    /**
     * Check if sequence is active
     */
    public boolean isSequenceActive() {
        return sequenceActive;
    }
    
    /**
     * Reload manager
     */
    public void reload() {
        loadSequences();
    }
    
    /**
     * Get all zone sequences
     */
    public Map<String, List<String>> getZoneSequences() {
        return Collections.unmodifiableMap(zoneSequences);
    }
    
    /**
     * Inner class to track player zone progress
     */
    public static class PlayerZoneData {
        private final String sequenceName;
        private final List<String> sequence;
        private int currentZoneIndex;
        
        public PlayerZoneData(String sequenceName, List<String> sequence) {
            this.sequenceName = sequenceName;
            this.sequence = new ArrayList<>(sequence);
            this.currentZoneIndex = 0;
        }
        
        public String getCurrentZone() {
            if (currentZoneIndex >= sequence.size()) {
                return null; // Sequence completed
            }
            return sequence.get(currentZoneIndex);
        }
        
        public boolean moveToNext() {
            if (currentZoneIndex < sequence.size() - 1) {
                currentZoneIndex++;
                return true;
            }
            return false; // Already at the end
        }
        
        public int getCurrentZoneIndex() {
            return currentZoneIndex;
        }
        
        public List<String> getSequence() {
            return Collections.unmodifiableList(sequence);
        }
        
        public String getSequenceName() {
            return sequenceName;
        }
        
        public boolean isCompleted() {
            return currentZoneIndex >= sequence.size();
        }
    }
}
