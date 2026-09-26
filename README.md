# BoundingBoxOutlineReloaded (BBOR) — 1.21.1 NeoForge

An updated, modernized edition of **BoundingBoxOutlineReloaded (BBOR)** for **Minecraft 1.21.1 (NeoForge)**.

BBOR is an essential utility mod for technical Minecraft players, farm designers, and survival architects. It visualizes bounding boxes of game structures, mob spawning regions, slime chunks, spawn chunks, beacon ranges, and more in customizable colors directly in your world.

---

## Features & Highlights

### 1.21.1 Structure Support
Fully supports modern vanilla Minecraft structures with accurate bounding boxes:
- **Trial Chambers** (including internal chambers, corridors, and combat zones)
- **Ancient Cities** and Nether Remnants (Bastions, Nether Fortresses, Nether Fossils)
- **Trail Ruins**, Ocean Monuments, Witch Huts, Desert and Jungle Temples
- **Strongholds**, End Cities, Woodland Mansions, Pillager Outposts, Mineshafts, and Villages

### Mod Compatibility
- **Shaders & Sodium/Iris**: Built on NeoForge's modern `RenderLevelStageEvent` pipeline. Completely compatible with **Sodium / Embeddium** and **Iris / Oculus** with zero shader pipeline conflicts or broken depth rendering.
- **Create & Heavy Content Mods**: Fully isolated lifecycle loading to avoid registry conflicts during mod initialization.
- **Dynamic Modded Structure Detection**: Automatically scans and registers custom structures added by worldgen mods (e.g., *YUNG's*, *When Dungeons Arise*, *Towns and Towers*).

### In-Game Configuration & Search
- Press **`G`** to open the in-game settings menu.
- **Instant Search**: Type into the top search bar to filter through dozens or hundreds of structure types in real-time.
- **A–Z Grouped Sorting**: All vanilla and modded structures are neatly sorted and grouped by namespace.
- Customizable wireframe colors and face fill opacity with an interactive color picker.

### Multiplayer & Technical Server Ready
- **Optional Payload Architecture**: Safe to connect to vanilla servers without being kicked.
- **Servux Protocol Support**: Seamlessly receives structure data from servers running **Servux** or **QuickCarpet**.
- **Dedicated Server Support**: Cleanly separates client rendering from server-side logic; fully safe for dedicated servers.

---

## Render Capabilities

| Feature | Description |
| :--- | :--- |
| **Structures** | Outlines inner pieces as well as overarching structure bounding boxes. |
| **Outer Boxes Only** | Toggle to simplify complex structures by displaying only their outer perimeter. |
| **Slime Chunks** | Shows slime spawn chunks with height-tracking indicators up to the player's level. |
| **World Spawn & Spawn Chunks** | Visualizes active and lazy spawn chunk boundaries. |
| **Mob Spawners** | Displays spawn volume and dynamic lines indicating player activation distance. |
| **Beacons & Conduits** | Visualizes full effect radius and conduit mob harm zones. |
| **Biome Borders** | Renders 3D boundary walls between biomes. |
| **AFK Spawning Spheres** | Displays 24–128 block spherical mob spawning and despawning boundaries. |
| **Spawnable Blocks** | Highlights light-level sensitive spawnable blocks around the player. |
| **Custom Boxes & Lines** | Create custom user-defined bounding boxes anywhere via commands. |

---

## Controls & Shortcuts

All shortcuts can be reconfigured in the standard Minecraft **Controls** menu:

- **`B`** — Global Toggle (instantly turn all BBOR rendering on/off)
- **`G`** (or `B + G`) — Open the **BBOR Settings Screen**
- **`O`** (or `B + O`) — Toggle **Outer Boxes Only** mode
- **`L`** (or `B + L`) — Open **Load Save Structure Cache** menu

---

## Commands

BBOR features a rich set of client-side and server-friendly commands:

```sh
# Set or synchronize world seed (useful on vanilla servers for slime chunks)
/bbor:seed <seed>

# Add a custom bounding box
/bbor:box add <name> <minX> <minY> <minZ> <maxX> <maxY> <maxZ> [#HEX_COLOR]

# Remove or clear custom bounding boxes
/bbor:box remove <name>
/bbor:box clear

# Spawning sphere visualization
/bbor:spawnsphere set <x> <y> <z>
/bbor:spawnsphere clear
```

---

## Installation

1. Install **[Minecraft 1.21.1](https://www.minecraft.net/)**.
2. Install **[NeoForge 21.1+](https://neoforged.net/)**.
3. Place `BBOutlineReloaded-neoforge-2.6-1.21.1.jar` into your `.minecraft/mods/` directory.
4. Launch the game and enjoy!

---

## Credits & License

- Original mod created by **[Irtimaled](https://github.com/irtimaled/BoundingBoxOutlineReloaded)**.
- Maintained and updated for NeoForge 1.21.1.
- Released under the **[MIT License](LICENSE)**.
