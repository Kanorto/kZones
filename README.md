# kZones

Zones minecraft plugin to manage player movement between WorldGuard regions. This plugin allows you to create sequential zone paths that players must follow in order, preventing them from skipping zones or leaving zones until permitted.

## Features

- **Zone Sequences**: Configure sequences of WorldGuard regions that players must follow in order
- **Movement Control**: Prevent players from entering zones out of sequence
- **Exit Restrictions**: Lock players in their current zone until they're allowed to proceed
- **Soft Teleportation**: Gently push players back if they try to violate the sequence
- **Visual/Audio Feedback**: Configurable particles and sounds when restricting movement
- **Commands**: Full command system to start/stop sequences and move players to next zones
- **Permissions**: Bypass permission for administrators
- **Multi-Sequence Support**: Configure multiple zone sequences
- **Per-Player Tracking**: Each player's progress is tracked individually

## Requirements

- Minecraft Server 1.21.4 (or compatible version)
- Java 21 or higher
- [WorldGuard](https://enginehub.org/worldguard) plugin
- [WorldEdit](https://enginehub.org/worldedit) plugin (required by WorldGuard)

## Installation

1. Build the plugin using Maven: `mvn clean package`
2. Copy `target/kZones-1.0.0.jar` to your server's `plugins/` folder
3. Ensure WorldGuard and WorldEdit are installed
4. Start/restart your server
5. Configure zone sequences in `plugins/kZones/config.yml`
6. Reload the plugin: `/kzones reload`

## Configuration

Edit `plugins/kZones/config.yml` to configure your zone sequences:

```yaml
zone-sequences:
  main_path:
    - "spawn"
    - "tutorial_1"
    - "tutorial_2"
    - "arena"
  
  alternative_path:
    - "spawn"
    - "quest_zone_1"
    - "quest_zone_2"

teleport:
  pushback-distance: 2.0
  show-particles: true
  play-sound: true
  sound: "ENTITY_ENDERMAN_TELEPORT"

messages:
  wrong-sequence: "&cYou cannot enter this region yet! Follow the correct sequence."
  cannot-leave: "&cYou must complete this zone before moving on!"
  sequence-started: "&aZone sequence started! Follow the path."
  sequence-stopped: "&eZone sequence stopped."
  next-zone: "&aYou can now proceed to the next zone!"
  prefix: "&8[&6kZones&8]&r "

debug: false
```

## Commands

All commands require the `kzones.admin` permission (default: op).

- `/kzones start` - Start the zone sequence for all players
- `/kzones stop` - Stop the zone sequence
- `/kzones next <player>` - Allow a specific player to move to the next zone in their sequence
- `/kzones status` - Display current status and configured sequences
- `/kzones reload` - Reload the plugin configuration

**Aliases**: `/kz`, `/zones`

## Permissions

- `kzones.admin` - Access to all commands (default: op)
- `kzones.bypass` - Bypass all zone restrictions (default: false)

## Usage Example

1. Create WorldGuard regions for your zones:
   ```
   /rg define spawn
   /rg define tutorial_1
   /rg define tutorial_2
   /rg define arena
   ```

2. Configure the sequence in `config.yml`:
   ```yaml
   zone-sequences:
     tutorial:
       - "spawn"
       - "tutorial_1"
       - "tutorial_2"
       - "arena"
   ```

3. Reload the plugin: `/kzones reload`

4. Start the sequence: `/kzones start`

5. Players will now be restricted:
   - They must start in the "spawn" region
   - They cannot enter "tutorial_1" until allowed
   - They cannot leave their current zone until permitted

6. Progress players through zones: `/kzones next <player>`

7. Stop the sequence when done: `/kzones stop`

## How It Works

1. **Sequence Start**: When you run `/kzones start`, all online players are initialized with the first zone in the sequence
2. **Movement Tracking**: The plugin monitors player movement between regions
3. **Entry Control**: Players can only enter the next region in their sequence
4. **Exit Control**: Players cannot leave their current required zone
5. **Soft Pushback**: If a player tries to violate the rules, they're gently teleported back with visual/audio effects
6. **Progression**: Use `/kzones next <player>` to allow individual players to proceed to the next zone

## Building from Source

```bash
# Clone the repository
git clone https://github.com/Kanorto/kZones.git
cd kZones

# Build with Maven
mvn clean package

# The compiled JAR will be in target/kZones-1.0.0.jar
```

## Support

For issues, questions, or contributions, please visit the [GitHub repository](https://github.com/Kanorto/kZones).

## License

This project is licensed under the GPL-3.0 License - see the [LICENSE](LICENSE) file for details.
