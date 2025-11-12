package com.kanorto.kzones.listeners;

import com.kanorto.kzones.KZonesPlugin;
import com.kanorto.kzones.managers.ZoneManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;

import java.util.Set;

public class PlayerMoveListener implements Listener {
    
    private final KZonesPlugin plugin;
    private final ZoneManager zoneManager;
    
    public PlayerMoveListener(KZonesPlugin plugin) {
        this.plugin = plugin;
        this.zoneManager = plugin.getZoneManager();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Initialize player if sequence is active
        if (zoneManager.isSequenceActive() && !player.hasPermission("kzones.bypass")) {
            zoneManager.initializePlayer(player);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check if player actually moved to a different block
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Skip if player has bypass permission
        if (player.hasPermission("kzones.bypass")) {
            return;
        }
        
        // Skip if sequence is not active
        if (!zoneManager.isSequenceActive()) {
            return;
        }
        
        Location from = event.getFrom();
        Location to = event.getTo();
        
        // Get regions at both locations
        Set<String> fromRegions = zoneManager.getRegionsAt(from);
        Set<String> toRegions = zoneManager.getRegionsAt(to);
        
        // Check if player is entering a new region
        Set<String> enteringRegions = new HashSet<>(toRegions);
        enteringRegions.removeAll(fromRegions);
        
        // Check if player is leaving a region
        Set<String> leavingRegions = new HashSet<>(fromRegions);
        leavingRegions.removeAll(toRegions);
        
        // Handle entering new regions
        for (String region : enteringRegions) {
            if (!zoneManager.canEnterRegion(player, region)) {
                // Player cannot enter this region yet
                teleportPlayerBack(player, from, to);
                sendMessage(player, "wrong-sequence");
                return;
            }
        }
        
        // Handle leaving current regions
        for (String region : leavingRegions) {
            if (!zoneManager.canLeaveRegion(player, region)) {
                // Player cannot leave this region yet
                teleportPlayerBack(player, from, to);
                sendMessage(player, "cannot-leave");
                return;
            }
        }
    }
    
    /**
     * Teleport player back with a soft pushback
     */
    private void teleportPlayerBack(Player player, Location from, Location to) {
        // Calculate direction vector from 'to' back to 'from'
        Vector direction = from.toVector().subtract(to.toVector()).normalize();
        
        // Get pushback distance from config
        double pushbackDistance = plugin.getConfig().getDouble("teleport.pushback-distance", 2.0);
        
        // Calculate the pushback location
        Location pushbackLocation = to.clone().add(direction.multiply(pushbackDistance));
        
        // Ensure the location is safe (same Y level as 'from' to avoid falling)
        pushbackLocation.setY(from.getY());
        pushbackLocation.setYaw(from.getYaw());
        pushbackLocation.setPitch(from.getPitch());
        
        // Teleport player
        player.teleport(pushbackLocation);
        
        // Visual and audio effects
        if (plugin.getConfig().getBoolean("teleport.show-particles", true)) {
            player.getWorld().spawnParticle(
                Particle.PORTAL,
                pushbackLocation,
                20,
                0.5, 0.5, 0.5,
                0.1
            );
        }
        
        if (plugin.getConfig().getBoolean("teleport.play-sound", true)) {
            String soundName = plugin.getConfig().getString("teleport.sound", "ENTITY_ENDERMAN_TELEPORT");
            try {
                Sound sound = Sound.valueOf(soundName);
                player.playSound(pushbackLocation, sound, 0.5f, 1.0f);
            } catch (IllegalArgumentException e) {
                // Invalid sound name, skip
            }
        }
    }
    
    /**
     * Send a message to the player from config
     */
    private void sendMessage(Player player, String messageKey) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6kZones&8]&r ");
        String message = plugin.getConfig().getString("messages." + messageKey, "");
        
        if (!message.isEmpty()) {
            String fullMessage = prefix + message;
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', fullMessage));
        }
    }
}
