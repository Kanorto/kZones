package com.kanorto.kzones.commands;

import com.kanorto.kzones.KZonesPlugin;
import com.kanorto.kzones.managers.ZoneManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class KZonesCommand implements CommandExecutor, TabCompleter {
    
    private final KZonesPlugin plugin;
    private final ZoneManager zoneManager;
    
    public KZonesCommand(KZonesPlugin plugin) {
        this.plugin = plugin;
        this.zoneManager = plugin.getZoneManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("kzones.admin")) {
            sendMessage(sender, "&cYou don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "start":
                handleStart(sender);
                break;
                
            case "stop":
                handleStop(sender);
                break;
                
            case "next":
                handleNext(sender, args);
                break;
                
            case "status":
                handleStatus(sender);
                break;
                
            case "reload":
                handleReload(sender);
                break;
                
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void handleStart(CommandSender sender) {
        if (zoneManager.isSequenceActive()) {
            sendMessage(sender, "&eZone sequence is already active!");
            return;
        }
        
        zoneManager.startSequence();
        sendConfigMessage(sender, "sequence-started");
        
        if (plugin.getConfig().getBoolean("debug", false)) {
            sender.sendMessage(ChatColor.GRAY + "Debug: Sequence started for all online players");
        }
    }
    
    private void handleStop(CommandSender sender) {
        if (!zoneManager.isSequenceActive()) {
            sendMessage(sender, "&eZone sequence is not active!");
            return;
        }
        
        zoneManager.stopSequence();
        sendConfigMessage(sender, "sequence-stopped");
    }
    
    private void handleNext(CommandSender sender, String[] args) {
        if (!zoneManager.isSequenceActive()) {
            sendMessage(sender, "&cZone sequence is not active! Use '/kzones start' first.");
            return;
        }
        
        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /kzones next <player>");
            return;
        }
        
        String playerName = args[1];
        Player target = plugin.getServer().getPlayer(playerName);
        
        if (target == null) {
            sendMessage(sender, "&cPlayer '" + playerName + "' not found!");
            return;
        }
        
        boolean moved = zoneManager.moveToNextZone(target.getUniqueId());
        
        if (moved) {
            sendConfigMessage(sender, "next-zone");
            sendConfigMessage(target, "next-zone");
            
            ZoneManager.PlayerZoneData data = zoneManager.getPlayerData(target.getUniqueId());
            if (data != null) {
                String currentZone = data.getCurrentZone();
                if (currentZone != null) {
                    sendMessage(sender, "&aPlayer " + target.getName() + " can now proceed to: &e" + currentZone);
                } else {
                    sendMessage(sender, "&aPlayer " + target.getName() + " has completed the sequence!");
                }
            }
        } else {
            sendMessage(sender, "&ePlayer " + target.getName() + " is already at the end of the sequence!");
        }
    }
    
    private void handleStatus(CommandSender sender) {
        sendMessage(sender, "&6=== kZones Status ===");
        sendMessage(sender, "&eSequence Active: &f" + (zoneManager.isSequenceActive() ? "Yes" : "No"));
        
        Map<String, List<String>> sequences = zoneManager.getZoneSequences();
        sendMessage(sender, "&eConfigured Sequences: &f" + sequences.size());
        
        for (Map.Entry<String, List<String>> entry : sequences.entrySet()) {
            sendMessage(sender, "  &7- &e" + entry.getKey() + "&7: &f" + String.join(" -> ", entry.getValue()));
        }
        
        if (zoneManager.isSequenceActive()) {
            int activePlayers = 0;
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (zoneManager.getPlayerData(player.getUniqueId()) != null) {
                    activePlayers++;
                }
            }
            sendMessage(sender, "&eActive Players: &f" + activePlayers);
        }
    }
    
    private void handleReload(CommandSender sender) {
        plugin.reloadPlugin();
        sendMessage(sender, "&aConfiguration reloaded successfully!");
    }
    
    private void sendHelp(CommandSender sender) {
        sendMessage(sender, "&6=== kZones Commands ===");
        sendMessage(sender, "&e/kzones start &7- Start the zone sequence");
        sendMessage(sender, "&e/kzones stop &7- Stop the zone sequence");
        sendMessage(sender, "&e/kzones next <player> &7- Move player to next zone");
        sendMessage(sender, "&e/kzones status &7- Show current status");
        sendMessage(sender, "&e/kzones reload &7- Reload configuration");
    }
    
    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
    
    private void sendConfigMessage(CommandSender sender, String messageKey) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6kZones&8]&r ");
        String message = plugin.getConfig().getString("messages." + messageKey, "");
        
        if (!message.isEmpty()) {
            String fullMessage = prefix + message;
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', fullMessage));
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("kzones.admin")) {
            return completions;
        }
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("start", "stop", "next", "status", "reload");
            String input = args[0].toLowerCase();
            
            for (String subCmd : subCommands) {
                if (subCmd.startsWith(input)) {
                    completions.add(subCmd);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("next")) {
            // Tab complete player names for 'next' command
            String input = args[1].toLowerCase();
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(input)) {
                    completions.add(player.getName());
                }
            }
        }
        
        return completions;
    }
}
