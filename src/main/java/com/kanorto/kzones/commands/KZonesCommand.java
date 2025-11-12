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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                
            case "target":
                handleTarget(sender, args);
                break;
                
            case "toggle":
                handleToggle(sender, args);
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
        sendConfigMessageKey(sender, "sequence-started");
        
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
        sendConfigMessageKey(sender, "sequence-stopped");
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
            sendConfigMessageKey(sender, "next-zone");
            sendConfigMessageKey(target, "next-zone");
            
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
        
        if (zoneManager.isTargetEnabled()) {
            sendMessage(sender, "&eTarget Zone: &f" + zoneManager.getTargetZone());
        }
        
        // Show restriction settings
        sendMessage(sender, "&eRestrictions:");
        sendMessage(sender, "  &7- Prevent Backward: &f" + plugin.getConfig().getBoolean("restrictions.prevent-backward-movement", true));
        sendMessage(sender, "  &7- Prevent Leaving: &f" + plugin.getConfig().getBoolean("restrictions.prevent-leaving-current-zone", true));
        sendMessage(sender, "  &7- Free After Target: &f" + plugin.getConfig().getBoolean("restrictions.free-movement-after-target", true));
        
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
    
    private void handleTarget(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /kzones target <set|clear|info> [zone]");
            return;
        }
        
        String action = args[1].toLowerCase();
        
        switch (action) {
            case "set":
                if (args.length < 3) {
                    sendMessage(sender, "&cUsage: /kzones target set <zone>");
                    return;
                }
                String zoneName = args[2];
                if (zoneManager.setTargetZone(zoneName)) {
                    String message = plugin.getConfig().getString("messages.target-set", "&aTarget zone set to: &e{zone}");
                    message = message.replace("{zone}", zoneName);
                    sendConfigMessage(sender, message);
                } else {
                    sendMessage(sender, "&cZone '" + zoneName + "' not found in any sequence!");
                }
                break;
                
            case "clear":
                zoneManager.clearTargetZone();
                sendMessage(sender, "&aTarget zone cleared.");
                break;
                
            case "info":
                if (zoneManager.isTargetEnabled()) {
                    sendMessage(sender, "&eTarget Zone: &f" + zoneManager.getTargetZone());
                } else {
                    sendMessage(sender, "&eNo target zone is set.");
                }
                break;
                
            default:
                sendMessage(sender, "&cUsage: /kzones target <set|clear|info> [zone]");
                break;
        }
    }
    
    private void handleToggle(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /kzones toggle <backward|leaving|freeafter>");
            return;
        }
        
        String setting = args[1].toLowerCase();
        
        switch (setting) {
            case "backward":
                boolean currentBackward = plugin.getConfig().getBoolean("restrictions.prevent-backward-movement", true);
                plugin.getConfig().set("restrictions.prevent-backward-movement", !currentBackward);
                plugin.saveConfig();
                sendMessage(sender, "&aPrevent backward movement: &f" + !currentBackward);
                break;
                
            case "leaving":
                boolean currentLeaving = plugin.getConfig().getBoolean("restrictions.prevent-leaving-current-zone", true);
                plugin.getConfig().set("restrictions.prevent-leaving-current-zone", !currentLeaving);
                plugin.saveConfig();
                sendMessage(sender, "&aPrevent leaving current zone: &f" + !currentLeaving);
                break;
                
            case "freeafter":
                boolean currentFree = plugin.getConfig().getBoolean("restrictions.free-movement-after-target", true);
                plugin.getConfig().set("restrictions.free-movement-after-target", !currentFree);
                plugin.saveConfig();
                sendMessage(sender, "&aFree movement after target: &f" + !currentFree);
                break;
                
            default:
                sendMessage(sender, "&cUsage: /kzones toggle <backward|leaving|freeafter>");
                break;
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
        sendMessage(sender, "&e/kzones target <set|clear|info> [zone] &7- Manage target zone");
        sendMessage(sender, "&e/kzones toggle <setting> &7- Toggle restriction settings");
        sendMessage(sender, "&e/kzones status &7- Show current status");
        sendMessage(sender, "&e/kzones reload &7- Reload configuration");
    }
    
    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
    
    private void sendConfigMessage(CommandSender sender, String message) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6kZones&8]&r ");
        String fullMessage = prefix + message;
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', fullMessage));
    }
    
    private void sendConfigMessageKey(CommandSender sender, String messageKey) {
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
            List<String> subCommands = Arrays.asList("start", "stop", "next", "target", "toggle", "status", "reload");
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
        } else if (args.length == 2 && args[0].equalsIgnoreCase("target")) {
            // Tab complete target subcommands
            List<String> targetCommands = Arrays.asList("set", "clear", "info");
            String input = args[1].toLowerCase();
            for (String cmd : targetCommands) {
                if (cmd.startsWith(input)) {
                    completions.add(cmd);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            // Tab complete toggle options
            List<String> toggleOptions = Arrays.asList("backward", "leaving", "freeafter");
            String input = args[1].toLowerCase();
            for (String opt : toggleOptions) {
                if (opt.startsWith(input)) {
                    completions.add(opt);
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("target") && args[1].equalsIgnoreCase("set")) {
            // Tab complete zone names for target set
            String input = args[2].toLowerCase();
            Set<String> uniqueZones = new HashSet<>();
            for (Map.Entry<String, List<String>> entry : zoneManager.getZoneSequences().entrySet()) {
                for (String zone : entry.getValue()) {
                    if (zone.toLowerCase().startsWith(input)) {
                        uniqueZones.add(zone);
                    }
                }
            }
            completions.addAll(uniqueZones);
        }
        
        return completions;
    }
}
