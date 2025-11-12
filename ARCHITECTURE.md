# kZones Architecture and Flow

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                       KZonesPlugin                          │
│                    (Main Plugin Class)                      │
│                                                             │
│  • Lifecycle Management (onEnable/onDisable)               │
│  • WorldGuard Integration Check                            │
│  • Component Initialization                                │
└─────────────────┬───────────────────────────────┬───────────┘
                  │                               │
                  │                               │
        ┌─────────▼──────────┐          ┌────────▼──────────┐
        │   ZoneManager      │          │   KZonesCommand   │
        │  (Business Logic)  │          │  (Command Handler)│
        │                    │          │                   │
        │ • Sequence Config  │          │ • /kzones start   │
        │ • Player Tracking  │          │ • /kzones stop    │
        │ • Region Detection │          │ • /kzones next    │
        │ • Progression      │          │ • /kzones status  │
        └────────▲───────────┘          │ • /kzones reload  │
                 │                      └───────────────────┘
                 │
        ┌────────┴────────────┐
        │ PlayerMoveListener  │
        │  (Event Handler)    │
        │                     │
        │ • Movement Tracking │
        │ • Permission Check  │
        │ • Region Validation │
        │ • Teleport Handling │
        └─────────────────────┘
```

## Player Movement Flow

```
┌──────────────┐
│ Player Moves │
└──────┬───────┘
       │
       ▼
┌──────────────────────┐      NO
│ Block Change?        ├──────────► Exit (Skip)
└──────┬───────────────┘
       │ YES
       ▼
┌──────────────────────┐      YES
│ Has Bypass Perm?     ├──────────► Allow Movement
└──────┬───────────────┘
       │ NO
       ▼
┌──────────────────────┐      NO
│ Sequence Active?     ├──────────► Allow Movement
└──────┬───────────────┘
       │ YES
       ▼
┌──────────────────────┐
│ Get Regions at       │
│ FROM and TO location │
└──────┬───────────────┘
       │
       ▼
┌──────────────────────┐      YES   ┌─────────────────┐
│ Entering New Region? ├───────────►│ Check if Allowed│
└──────┬───────────────┘            └────────┬────────┘
       │ NO                                  │
       │                           ┌─────────▼─────────┐
       ▼                           │ Can Enter Region? │
┌──────────────────────┐      NO   └────────┬──────────┘
│ Leaving Region?      │                    │ NO
└──────┬───────────────┘             ┌──────▼──────────┐
       │ YES                          │ Push Player Back│
       ▼                              │ Show Particles  │
┌──────────────────────┐              │ Play Sound      │
│ Check if Can Leave   │              │ Send Message    │
└──────┬───────────────┘              └─────────────────┘
       │ NO                                  │
       ▼                                     │
┌──────────────────────┐                    │
│ Push Player Back     │◄───────────────────┘
│ Show Particles       │
│ Play Sound           │
│ Send Message         │
└──────────────────────┘
```

## Zone Sequence Flow

```
┌─────────────────┐
│ /kzones start   │
└────────┬────────┘
         │
         ▼
┌─────────────────────────┐
│ Load Sequences from     │
│ config.yml              │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Initialize All Players  │
│ at First Zone           │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Players in Zone 1       │
│ • Cannot enter Zone 2   │
│ • Cannot leave Zone 1   │
└────────┬────────────────┘
         │
         │ /kzones next <player>
         ▼
┌─────────────────────────┐
│ Player Progress to      │
│ Zone 2                  │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Players in Zone 2       │
│ • Can leave Zone 1      │
│ • Can enter Zone 2      │
│ • Cannot enter Zone 3   │
│ • Cannot leave Zone 2   │
└────────┬────────────────┘
         │
         │ (Continue...)
         ▼
┌─────────────────────────┐
│ Final Zone Reached      │
│ • Player can move freely│
└─────────────────────────┘
```

## Data Flow

```
config.yml
    │
    ▼
┌──────────────────────────┐
│ ZoneManager              │
│ • zoneSequences Map      │
│   "tutorial" -> [        │
│     "spawn",             │
│     "zone1",             │
│     "zone2"              │
│   ]                      │
└──────────────────────────┘
    │
    ▼
┌──────────────────────────┐
│ PlayerZoneData           │
│ • UUID -> PlayerData     │
│ • sequenceName           │
│ • currentZoneIndex: 0    │
└──────────────────────────┘
    │
    │ /kzones next
    ▼
┌──────────────────────────┐
│ currentZoneIndex: 1      │
└──────────────────────────┘
```

## Permission Flow

```
┌──────────────────┐
│ Player Action    │
└────────┬─────────┘
         │
         ▼
┌──────────────────────┐      YES
│ Has kzones.bypass?   ├─────────► Allow All Movement
└────────┬─────────────┘
         │ NO
         ▼
┌──────────────────────┐      YES    ┌─────────────────┐
│ Command Executed?    ├─────────────►│Has kzones.admin?│
└────────┬─────────────┘              └────────┬────────┘
         │ NO                                  │
         │                            ┌────────▼────────┐
         ▼                            │ Allow Command   │
┌──────────────────────┐      YES    └─────────────────┘
│ Sequence Active?     ├──────────► Apply Restrictions
└────────┬─────────────┘
         │ NO
         ▼
  Allow Movement
```

## Configuration Flow

```
Server Start
    │
    ▼
┌──────────────────────┐
│ KZonesPlugin         │
│ .onEnable()          │
└────────┬─────────────┘
         │
         ▼
┌──────────────────────┐
│ Save Default Config  │
└────────┬─────────────┘
         │
         ▼
┌──────────────────────┐
│ Create ZoneManager   │
└────────┬─────────────┘
         │
         ▼
┌──────────────────────┐
│ Load Sequences       │
│ from config.yml      │
└────────┬─────────────┘
         │
         ▼
┌──────────────────────┐
│ Register Listeners   │
└────────┬─────────────┘
         │
         ▼
┌──────────────────────┐
│ Register Commands    │
└────────┬─────────────┘
         │
         ▼
    Plugin Ready

    │
    │ /kzones reload
    ▼
┌──────────────────────┐
│ reloadConfig()       │
└────────┬─────────────┘
         │
         ▼
┌──────────────────────┐
│ zoneManager.reload() │
└──────────────────────┘
```

## Component Responsibilities

### KZonesPlugin
- Plugin lifecycle management
- Dependency checking (WorldGuard)
- Component initialization
- Config management

### ZoneManager
- Load and store zone sequences
- Track player progression
- Validate region access
- Manage player data
- Interface with WorldGuard API

### PlayerMoveListener
- Monitor player movement
- Check movement validity
- Execute teleportation
- Provide feedback (particles, sound, messages)
- Handle player join events

### KZonesCommand
- Parse command arguments
- Validate permissions
- Execute admin actions
- Provide command feedback
- Tab completion

## Key Features

### Thread Safety
- ConcurrentHashMap for player data
- No race conditions in movement tracking

### Performance
- Block-level movement check (not per-tick)
- Cached region lookups
- Minimal WorldGuard API calls

### User Experience
- Soft teleportation (gentle pushback)
- Visual feedback (particles)
- Audio feedback (sounds)
- Clear text messages
- Color-coded messages

### Flexibility
- Multiple sequence support
- Configurable behavior
- Per-player tracking
- Hot-reload configuration
