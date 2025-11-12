# Setup Guide for kZones

This guide will help you set up kZones on your Minecraft server from start to finish.

## Prerequisites

Before installing kZones, make sure you have:

1. **Minecraft Server** running version 1.21.4 or compatible
2. **Java 21** or higher installed
3. **WorldGuard** plugin installed and configured
4. **WorldEdit** plugin installed (required by WorldGuard)
5. **Op or Admin permissions** to execute commands

## Installation Steps

### Step 1: Install Dependencies

1. Download and install [WorldEdit](https://dev.bukkit.org/projects/worldedit)
2. Download and install [WorldGuard](https://dev.bukkit.org/projects/worldguard)
3. Restart your server to load these plugins

### Step 2: Build kZones

```bash
# Clone the repository
git clone https://github.com/Kanorto/kZones.git
cd kZones

# Build with Maven (requires internet connection)
mvn clean package

# The JAR will be created at: target/kZones-1.0.0.jar
```

### Step 3: Install kZones

1. Copy `kZones-1.0.0.jar` to your server's `plugins/` folder
2. Restart your server
3. The plugin will create a `plugins/kZones/` folder with default `config.yml`

### Step 4: Create WorldGuard Regions

Before configuring kZones, you need to create the regions using WorldGuard:

```
# Example: Creating a tutorial path
/rg define spawn
/rg define tutorial_1
/rg define tutorial_2
/rg define arena
```

Make sure each region is properly defined with boundaries.

### Step 5: Configure Zone Sequences

1. Open `plugins/kZones/config.yml`
2. Define your zone sequences:

```yaml
zone-sequences:
  tutorial_path:
    - "spawn"          # First zone - players start here
    - "tutorial_1"     # Second zone
    - "tutorial_2"     # Third zone
    - "arena"          # Final zone
```

3. Save the file

### Step 6: Reload Configuration

In-game, run:
```
/kzones reload
```

### Step 7: Test Your Setup

1. **Check Status**: `/kzones status`
   - Verify your sequences are loaded correctly

2. **Start Sequence**: `/kzones start`
   - This activates zone restrictions for all players

3. **Test Movement**:
   - Try entering zones out of sequence (should be blocked)
   - Try leaving your current zone (should be blocked)

4. **Progress Players**: `/kzones next <playername>`
   - Allows player to move to the next zone

5. **Stop Sequence**: `/kzones stop`
   - Deactivates all restrictions

## Common Use Cases

### Tutorial System

Perfect for guiding new players through a structured tutorial:

```yaml
zone-sequences:
  tutorial:
    - "welcome_area"
    - "movement_tutorial"
    - "crafting_tutorial"
    - "combat_tutorial"
    - "completion_area"
```

**Setup:**
1. Create regions for each tutorial section
2. Start sequence when players join: `/kzones start`
3. Progress players as they complete sections: `/kzones next <player>`

### Quest Lines

Control access to quest areas in sequence:

```yaml
zone-sequences:
  main_quest:
    - "quest_giver"
    - "forest_entrance"
    - "cave_entrance"
    - "boss_room"
    - "treasure_room"
```

**Setup:**
1. Players must complete quests in order
2. Use `/kzones next <player>` when quest objectives are met

### Event Management

Guide players through event stages:

```yaml
zone-sequences:
  pvp_event:
    - "lobby"
    - "warmup_arena"
    - "main_arena"
    - "final_arena"
    - "victory_platform"
```

**Setup:**
1. Start event: `/kzones start`
2. Progress all at once or individually
3. Stop when event ends: `/kzones stop`

## Advanced Configuration

### Custom Messages

Modify messages in `config.yml`:

```yaml
messages:
  wrong-sequence: "&c&lSTOP! &rYou need to complete previous zones first!"
  cannot-leave: "&e&lWait! &rComplete this zone before moving on!"
  prefix: "&8[&b&lZONES&8]&r "
```

### Teleport Behavior

Adjust how players are pushed back:

```yaml
teleport:
  pushback-distance: 3.0    # Stronger pushback
  show-particles: true       # Visual feedback
  play-sound: true          # Audio feedback
  sound: "BLOCK_NOTE_BLOCK_PLING"  # Different sound
```

### Multiple Sequences

You can have multiple sequences for different purposes:

```yaml
zone-sequences:
  tutorial:
    - "tut_1"
    - "tut_2"
  
  pvp_path:
    - "pvp_1"
    - "pvp_2"
  
  quest_line:
    - "quest_1"
    - "quest_2"
```

Note: Currently, players follow the first sequence. Future versions may support selecting sequences.

## Permissions

Grant permissions in your permissions plugin:

```yaml
# Full admin access
kzones.admin: true

# Bypass all restrictions (for staff)
kzones.bypass: true
```

## Troubleshooting

### Players can enter any region

**Solution:**
1. Verify sequence is started: `/kzones status`
2. If not active: `/kzones start`
3. Check player doesn't have bypass: remove `kzones.bypass` permission

### Players get stuck

**Solution:**
1. Progress them manually: `/kzones next <player>`
2. Or stop sequence: `/kzones stop`

### Regions not detected

**Solution:**
1. Verify WorldGuard is installed: `/wg version`
2. Check region exists: `/rg info <region_name>`
3. Ensure region names in config.yml match exactly (case-sensitive!)

### Plugin not loading

**Solution:**
1. Check server version is compatible (1.21.4 or similar)
2. Verify WorldGuard is installed and enabled
3. Check console for error messages
4. Ensure Java 21 or higher is being used

## Best Practices

1. **Test First**: Test your zone sequences in a test environment before production
2. **Clear Communication**: Tell players about zone restrictions before starting
3. **Backup Configs**: Keep backups of your `config.yml`
4. **Monitor Progress**: Use `/kzones status` regularly to check system state
5. **Gradual Rollout**: Start with simple sequences, then expand

## Command Reference

| Command | Description | Permission |
|---------|-------------|------------|
| `/kzones start` | Start zone sequence | `kzones.admin` |
| `/kzones stop` | Stop zone sequence | `kzones.admin` |
| `/kzones next <player>` | Move player to next zone | `kzones.admin` |
| `/kzones status` | Show current status | `kzones.admin` |
| `/kzones reload` | Reload configuration | `kzones.admin` |

## Getting Help

If you encounter issues:

1. Check this setup guide
2. Review the [README.md](README.md) for detailed documentation
3. Look at [config.example.yml](config.example.yml) for configuration examples
4. Open an issue on [GitHub](https://github.com/Kanorto/kZones/issues)

## Next Steps

Once kZones is set up:

1. Create your region sequences
2. Design your progression system
3. Integrate with other plugins (quests, economy, etc.)
4. Monitor player feedback and adjust as needed

Happy zoning! 🎮
