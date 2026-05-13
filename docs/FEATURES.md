# epgrab Features

## Overview
`epgrab` is a client-side Fabric mod for Hypixel SkyBlock focused on quickly grabbing dungeon utility items, tracking usage, and surfacing lightweight in-game UI for stock and progression.

## Sack Grabbing
The mod can request items directly from sacks with commands or keybinds.

Supported items:
- Ender Pearls
- Spirit Leaps
- Decoys
- Superboom TNT

Default target amounts:
- Ender Pearls: 16
- Spirit Leaps: 16
- Decoys: 64
- Superboom TNT: 64

When a sack transfer succeeds, epgrab can suppress the vanilla sack message and replace it with its own short status feedback.

## Commands
### Main command
- `/ep [amount]`

### Subcommands
- `/ep pearls [amount]`
- `/ep leaps [amount]`
- `/ep decoys [amount]`
- `/ep superboom [amount]`
- `/ep superbooms [amount]`
- `/ep settings`
- `/ep achievements`
- `/ep help`

### Aliases
- `/sl`
- `/de`
- `/sb`

## Keybinds
Separate configurable keybinds exist for:
- grabbing pearls
- grabbing spirit leaps
- grabbing decoys
- grabbing superbooms

These can be changed from the mod menu settings screen.

## Low-Stock Alerts
epgrab watches your inventory for low stock on tracked dungeon items and shows a center-screen popup when supply drops to a low threshold.

Tracked items:
- Ender Pearls
- Spirit Leaps
- Decoys
- Superboom TNT

Current threshold:
- 3 or fewer remaining

## Dungeon Context Gating
The following features are only active in these dungeon contexts:
- item grab tracking
- item use tracking
- low-stock popups

Tracked contexts:
- Dungeon Hub
- The Catacombs

## Achievements
epgrab includes a local-only achievement system.

Tracked achievement groups:
- Pearls grabbed
- Pearls thrown
- Spirit Leaps grabbed
- Spirit Leaps used
- Decoys grabbed
- Decoys used
- Superboom TNT grabbed
- Superboom TNT used
- Kismet Feathers used
- Creator/social achievements

### Kismet Feather tracking
Kismet Feather usage is tracked for both Croesus rerolls and rerolls that happen during a run.

### Achievement UI
The achievements page is built into the mod menu screen and includes:
- unlock count summary
- per-item stat summary
- scrollable achievement list
- filter dropdown by type

Available filters:
- All
- Pearls
- Leaps
- Decoys
- Booms
- Kismets
- Social

### Achievement notifications
When an achievement unlocks, epgrab shows:
- a toast
- a center-screen title/subtitle popup
- a chat message

## Creator Badge
epgrab shows a creator badge next to specific creator names in nametags.

Current supported creators:
- asyncfn

## Really Creative Mind
epgrab shows a painting badge next to supported Really Creative Mind names in nametags.

Current supported Really Creative Mind players:
- asyncfn
- MiloDaMonke

## Creator Achievements
epgrab can unlock special social achievements when you share a lobby with supported players.

Current social achievements:
- `Interdimensionally Bitflipped` — asyncfn
- `idk i just work here` — MiloDaMonke

Secret achievement:
- `Really Creative Mind`

These achievements are **not** dungeon-gated.

## Notes
- The mod is client-side.
- Achievements are local progression only.
- No server-side rewards or syncing are included.
