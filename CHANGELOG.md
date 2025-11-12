# Changelog

All notable changes to the kZones project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2025-11-12

### Added

#### Backward Movement Prevention
- Prevent players from returning to previous zones in the sequence
- Configurable via `restrictions.prevent-backward-movement` in config.yml
- Toggle at runtime with `/kzones toggle backward` command
- Custom message `cannot-go-backward` for backward movement attempts
- Zone index tracking to detect backward movement

#### Target Zone System
- Set specific target zones that all players must reach
- `/kzones target set <zone>` - Set target zone for all players
- `/kzones target clear` - Clear current target zone
- `/kzones target info` - Display information about current target
- Per-player tracking of target completion status
- Free movement after reaching target (configurable)
- Target zone validation against configured sequences
- `target.enabled`, `target.zone`, and `target.auto-progress-to-target` config options

#### Enhanced Configurability
- Runtime toggle commands for all restriction settings
- `/kzones toggle backward` - Toggle backward movement prevention
- `/kzones toggle leaving` - Toggle leaving current zone prevention  
- `/kzones toggle freeafter` - Toggle free movement after target
- Settings persist to config.yml automatically
- No server restart required for toggle changes
- Enhanced `/kzones status` showing all restriction settings

#### GitHub Actions CI/CD
- Automatic builds on push to main/master branches
- Automatic version number increment (patch version)
- Automatic GitHub release creation with JAR artifacts
- Maven dependency caching for faster builds
- Java 21 build environment
- Version update commits after successful releases

#### Tab Completion
- Tab completion for `/kzones target` subcommands (set, clear, info)
- Tab completion for `/kzones toggle` options (backward, leaving, freeafter)
- Tab completion for zone names in `/kzones target set <zone>`

### Changed
- Enhanced `ZoneManager` with target zone logic and backward movement detection
- Updated `PlayerZoneData` class with new methods: `getZoneIndex()`, `setTargetZone()`, `hasReachedTarget()`
- Improved `canEnterRegion()` method to check for backward movement
- Enhanced `canLeaveRegion()` method to respect target completion
- Updated `PlayerMoveListener` to provide specific messages for backward movement
- Expanded command system with target and toggle handlers
- Updated documentation to reflect new features

### Configuration Changes
New config sections:
```yaml
restrictions:
  prevent-backward-movement: true
  prevent-leaving-current-zone: true
  free-movement-after-target: true

target:
  enabled: false
  zone: ""
  auto-progress-to-target: false
```

New messages:
- `cannot-go-backward` - Message when trying to go backward
- `target-set` - Message when target zone is set
- `target-reached` - Message when target is reached

## [1.0.0] - 2025-11-12

### Initial Release

Complete implementation of kZones plugin for Minecraft 1.21.4.

### Added

#### Core Functionality
- Zone sequence configuration system via YAML
- Per-player progression tracking through zone sequences
- WorldGuard region integration for zone detection
- Movement restriction system preventing unauthorized zone entry/exit
- Soft teleportation with configurable pushback distance
- Visual feedback system with particle effects
- Audio feedback system with configurable sounds
- Color-coded message system with full customization

#### Commands
- `/kzones start` - Activate zone sequences for all players
- `/kzones stop` - Deactivate zone sequences
- `/kzones next <player>` - Progress specific player to next zone
- `/kzones status` - Display current status and configured sequences
- `/kzones reload` - Hot-reload configuration without server restart
- Command aliases: `/kz` and `/zones`
- Full tab completion for all commands and player names

#### Permissions
- `kzones.admin` - Full administrative access (default: op)
- `kzones.bypass` - Bypass all zone restrictions (default: false)

#### Configuration
- Default `config.yml` with comprehensive settings
- Multiple zone sequence support
- Customizable teleport behavior (distance, particles, sounds)
- Fully customizable messages with color codes
- Debug mode for troubleshooting

#### Documentation
- `README.md` - Complete feature documentation
- `QUICKSTART.md` - 5-minute setup guide
- `SETUP.md` - Comprehensive setup instructions
- `ARCHITECTURE.md` - Technical architecture with flowcharts
- `CONTRIBUTING.md` - Contribution guidelines
- `config.example.yml` - Example configurations for common use cases

#### Technical Features
- Thread-safe implementation using ConcurrentHashMap
- Efficient block-level movement tracking
- Proper lifecycle management (onEnable/onDisable)
- Dependency checking for WorldGuard
- Resource filtering in Maven build
- Maven shade plugin for JAR packaging

#### Security
- Input validation on all commands
- Permission checks enforced throughout
- Safe UUID-based player tracking
- No SQL injection risks
- No command injection risks
- CodeQL security scan passed (0 vulnerabilities)

### Project Structure
- 4 Java source files (708 lines of code)
- 2 resource files (plugin.yml, config.yml)
- 5 documentation files (25,000+ words)
- 1 example configuration file
- Maven build configuration with Java 21

### Dependencies
- Paper/Spigot API 1.21.3+ (provided)
- WorldGuard 7.0.11 (provided)
- WorldEdit 7.3.6 (provided)
- Java 21 runtime

### Tested With
- Minecraft 1.21.4
- Paper Server
- WorldGuard 7.0.11
- WorldEdit 7.3.6
- Java 21

---

## Release Notes

### Version 1.0.0 - Initial Release

This is the first stable release of kZones, providing complete functionality for managing player movement between WorldGuard regions.

**What's Included:**
- Full zone sequence management system
- Comprehensive command interface
- Flexible configuration options
- Complete documentation suite
- Production-ready code quality

**Use Cases:**
- Tutorial systems for new players
- Quest line progression control
- PvP ranking and arena access
- Event management and staging
- Progressive world exploration

**Getting Started:**
1. See [QUICKSTART.md](QUICKSTART.md) for 5-minute setup
2. Read [SETUP.md](SETUP.md) for detailed instructions
3. Check [README.md](README.md) for complete documentation

**Support:**
- GitHub Issues: https://github.com/Kanorto/kZones/issues
- Documentation: All guides included in repository

---

[1.0.0]: https://github.com/Kanorto/kZones/releases/tag/v1.0.0
