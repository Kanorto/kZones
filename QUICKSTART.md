# Quick Start Guide

Get kZones running on your server in 5 minutes!

## 1. Prerequisites Check

Before starting, ensure you have:
- [ ] Minecraft Server 1.21.4 (or compatible)
- [ ] Java 21 installed
- [ ] WorldGuard plugin installed
- [ ] WorldEdit plugin installed
- [ ] Op/Admin permissions

## 2. Build the Plugin

```bash
# Clone and build
git clone https://github.com/Kanorto/kZones.git
cd kZones
mvn clean package

# Find the JAR at: target/kZones-1.1.0.jar
```

## 3. Install

```bash
# Copy to your server
cp target/kZones-1.1.0.jar /path/to/server/plugins/

# Restart server
# Plugin will create: plugins/kZones/config.yml
```

## 4. Create Regions

Use WorldGuard to define your zones:

```
/rg define spawn
/rg define zone1
/rg define zone2
/rg define zone3
```

## 5. Configure Sequences

Edit `plugins/kZones/config.yml`:

```yaml
zone-sequences:
  my_path:
    - "spawn"
    - "zone1"
    - "zone2"
    - "zone3"
```

## 6. Reload and Start

```
/kzones reload
/kzones start
```

## 7. Test It!

1. Walk into "spawn" region
2. Try to enter "zone2" - **You'll be blocked!**
3. Run `/kzones next <yourname>`
4. Now you can enter "zone1"
5. Run `/kzones next <yourname>` again
6. Now you can enter "zone2"

## Done! 🎉

Your zone system is now active!

## Essential Commands

| Command | What It Does |
|---------|--------------|
| `/kzones start` | Activate zone restrictions |
| `/kzones stop` | Deactivate restrictions |
| `/kzones next <player>` | Let player move to next zone |
| `/kzones target set <zone>` | Set target zone for all players |
| `/kzones toggle backward` | Toggle backward movement prevention |
| `/kzones status` | Check current status |
| `/kzones reload` | Reload config changes |

## Advanced Features

### Set a Target Zone
```
/kzones target set zone2
```
All players must reach `zone2`. After that, they can move freely!

### Toggle Restrictions
```
/kzones toggle backward    # Allow/prevent going backward
/kzones toggle leaving     # Allow/prevent leaving current zone
/kzones toggle freeafter   # Allow/prevent free movement after target
```

## Common Issues

**Problem:** Players can enter any zone
- **Fix:** Run `/kzones start`

**Problem:** Region names don't work
- **Fix:** Check spelling matches exactly (case-sensitive!)

**Problem:** Plugin won't load
- **Fix:** Ensure WorldGuard is installed first

## Need More Help?

- **Full Guide:** See [SETUP.md](SETUP.md)
- **All Features:** See [README.md](README.md)
- **Technical Details:** See [ARCHITECTURE.md](ARCHITECTURE.md)
- **Issues:** [GitHub Issues](https://github.com/Kanorto/kZones/issues)

## Example Use Case: Tutorial System

```yaml
# 1. Create regions
/rg define welcome
/rg define basics
/rg define combat
/rg define complete

# 2. Configure (config.yml)
zone-sequences:
  tutorial:
    - "welcome"
    - "basics"
    - "combat"
    - "complete"

# 3. Start system
/kzones reload
/kzones start

# 4. Progress players as they complete sections
/kzones next PlayerName
```

## Pro Tips

💡 **Tip 1:** Give staff the bypass permission to move freely
```
/lp user StaffName permission set kzones.bypass true
```

💡 **Tip 2:** Use debug mode to troubleshoot
```yaml
debug: true  # in config.yml
```

💡 **Tip 3:** Customize messages for your server's theme
```yaml
messages:
  prefix: "&8[&cMyServer&8]&r "
  wrong-sequence: "&cHey! Complete the previous area first!"
```

💡 **Tip 4:** Adjust pushback for different server styles
```yaml
teleport:
  pushback-distance: 1.0  # Gentle
  pushback-distance: 3.0  # Strong
```

## What Next?

Once your basic setup works:

1. 📚 Read [SETUP.md](SETUP.md) for advanced configurations
2. 🎨 Customize messages and effects in config.yml
3. 🎮 Design your zone progression system
4. 👥 Train your staff on the commands
5. 🚀 Go live!

Happy zoning! 🎮✨
