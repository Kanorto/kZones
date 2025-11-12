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
    private String targetZone;
    private boolean targetEnabled;
    
    public ZoneManager(KZonesPlugin plugin) {
        this.plugin = plugin;
        this.zoneSequences = new HashMap<>();
        this.playerData = new ConcurrentHashMap<>();
        this.sequenceActive = false;
        this.targetZone = null;
        this.targetEnabled = false;
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
        
        // Load target zone settings
        targetEnabled = plugin.getConfig().getBoolean("target.enabled", false);
        targetZone = plugin.getConfig().getString("target.zone", "");
        
        if (targetEnabled && !targetZone.isEmpty()) {
            plugin.getLogger().info("Target zone enabled: " + targetZone);
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
        
        // Check if trying to enter a previous zone (backward movement)
        if (plugin.getConfig().getBoolean("restrictions.prevent-backward-movement", true)) {
            int enteringZoneIndex = data.getZoneIndex(regionName);
            if (enteringZoneIndex >= 0 && enteringZoneIndex < data.getCurrentZoneIndex()) {
                // Trying to go backward
                return false;
            }
        }
        
        // Check if the region matches the expected zone
        boolean canEnter = regionName.equalsIgnoreCase(expectedZone);
        
        // If target is enabled and player reached it, allow free movement
        if (targetEnabled && !targetZone.isEmpty() && data.hasReachedTarget()) {
            if (plugin.getConfig().getBoolean("restrictions.free-movement-after-target", true)) {
                return true;
            }
        }
        
        return canEnter;
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
        
        // Check config setting for leaving current zone
        if (!plugin.getConfig().getBoolean("restrictions.prevent-leaving-current-zone", true)) {
            return true; // Allow leaving if setting is disabled
        }
        
        // If target is enabled and player reached it, allow free movement
        if (targetEnabled && !targetZone.isEmpty() && data.hasReachedTarget()) {
            if (plugin.getConfig().getBoolean("restrictions.free-movement-after-target", true)) {
                return true;
            }
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
     * Remove player's zone data (called on disconnect to prevent memory leaks)
     */
    public void removePlayerData(UUID playerId) {
        playerData.remove(playerId);
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
     * Set target zone for all players
     */
    public boolean setTargetZone(String zoneName) {
        // Verify the zone exists in a sequence
        boolean found = false;
        for (List<String> sequence : zoneSequences.values()) {
            if (sequence.stream().anyMatch(z -> z.equalsIgnoreCase(zoneName))) {
                found = true;
                break;
            }
        }
        
        if (!found) {
            return false;
        }
        
        targetZone = zoneName;
        targetEnabled = true;
        
        // Update config
        plugin.getConfig().set("target.enabled", true);
        plugin.getConfig().set("target.zone", zoneName);
        plugin.saveConfigSync();
        
        // Update all player data with target
        for (PlayerZoneData data : playerData.values()) {
            data.setTargetZone(zoneName);
        }
        
        return true;
    }
    
    /**
     * Clear target zone
     */
    public void clearTargetZone() {
        targetZone = null;
        targetEnabled = false;
        
        plugin.getConfig().set("target.enabled", false);
        plugin.getConfig().set("target.zone", "");
        plugin.saveConfigSync();
        
        for (PlayerZoneData data : playerData.values()) {
            data.setTargetZone(null);
        }
    }
    
    /**
     * Get current target zone
     */
    public String getTargetZone() {
        return targetZone;
    }
    
    /**
     * Check if target is enabled
     */
    public boolean isTargetEnabled() {
        return targetEnabled && targetZone != null && !targetZone.isEmpty();
    }
    
    /**
     * Inner class to track player zone progress
     */
    public static class PlayerZoneData {
        private final String sequenceName;
        private final List<String> sequence;
        private int currentZoneIndex;
        private String targetZone;
        private boolean reachedTarget;
        
        public PlayerZoneData(String sequenceName, List<String> sequence) {
            this.sequenceName = sequenceName;
            this.sequence = new ArrayList<>(sequence);
            this.currentZoneIndex = 0;
            this.targetZone = null;
            this.reachedTarget = false;
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
                checkIfReachedTarget();
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
        
        /**
         * Get the index of a zone in the sequence
         */
        public int getZoneIndex(String zoneName) {
            for (int i = 0; i < sequence.size(); i++) {
                if (sequence.get(i).equalsIgnoreCase(zoneName)) {
                    return i;
                }
            }
            return -1;
        }
        
        /**
         * Set target zone for this player
         */
        public void setTargetZone(String targetZone) {
            this.targetZone = targetZone;
            this.reachedTarget = false;
            checkIfReachedTarget();
        }
        
        /**
         * Check if player has reached target zone
         */
        private void checkIfReachedTarget() {
            if (targetZone != null && !targetZone.isEmpty()) {
                String currentZone = getCurrentZone();
                if (currentZone != null && currentZone.equalsIgnoreCase(targetZone)) {
                    reachedTarget = true;
                }
                // Also check if we've passed it
                int targetIndex = getZoneIndex(targetZone);
                if (targetIndex >= 0 && currentZoneIndex > targetIndex) {
                    reachedTarget = true;
                }
            }
        }
        
        /**
         * Check if target has been reached
         */
        public boolean hasReachedTarget() {
            return reachedTarget;
        }
        
        /**
         * Get target zone
         */
        public String getTargetZone() {
            return targetZone;
        }
    }
}
