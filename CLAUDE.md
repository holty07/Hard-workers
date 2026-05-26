# Hard Workers — CLAUDE.md

## Project Overview

Hard Workers is a **Minecraft NeoForge 1.21.1 mod** (Java 21) that adds four types of worker blocks — Lumberjack, Miner, Farmer, and Warehouse — each available in five tiers (Wood, Stone, Iron, Diamond, Netherite). Placing a worker block spawns a `PathfinderMob` entity that autonomously performs its task, stores collected items in an internal 27-slot chest, and despawns when the block is removed.

- **Mod ID**: `hardworkers`
- **Group**: `com.hardworkers.hardworkers`
- **NeoForge**: 21.1.172 / Minecraft 1.21.1
- **Java**: 21
- **License**: MIT

---

## Repository Layout

```
Hard-workers/
├── build.gradle                   # NeoGradle build config
├── gradle.properties              # All mod metadata & version numbers
├── settings.gradle                # Foojay + NeoForge Maven repos
├── gradlew / gradlew.bat          # Gradle wrapper
├── .github/workflows/release.yml  # Auto-release CI pipeline
└── src/main/
    ├── java/com/hardworkers/hardworkers/
    │   ├── HardWorkers.java            # @Mod entry point
    │   ├── HardWorkersConfig.java      # Common config
    │   ├── init/                       # Deferred registries
    │   │   ├── ModBlocks.java
    │   │   ├── ModItems.java
    │   │   ├── ModBlockEntities.java
    │   │   └── ModEntities.java
    │   ├── block/                      # Block classes + tier enums
    │   │   ├── LumberjackBlock.java / LumberjackTier.java
    │   │   ├── MinerBlock.java / MinerTier.java
    │   │   ├── FarmerBlock.java / FarmerTier.java
    │   │   └── WarehouseBlock.java / WarehouseTier.java
    │   ├── blockentity/                # Block entity (storage + tick)
    │   │   ├── LumberjackBlockEntity.java
    │   │   ├── MinerBlockEntity.java
    │   │   ├── FarmerBlockEntity.java
    │   │   └── WarehouseBlockEntity.java
    │   ├── entity/                     # PathfinderMob subclasses
    │   │   ├── LumberjackEntity.java
    │   │   ├── MinerEntity.java
    │   │   ├── FarmerEntity.java
    │   │   ├── WarehouseWorkerEntity.java
    │   │   ├── ai/                     # Custom AI goals
    │   │   │   ├── FindTreeGoal.java
    │   │   │   ├── ChopTreeGoal.java
    │   │   │   ├── MineForwardGoal.java
    │   │   │   ├── HarvestCropsGoal.java
    │   │   │   └── CollectItemsGoal.java
    │   │   └── client/                 # Entity renderers
    │   │       ├── LumberjackRenderer.java
    │   │       ├── MinerRenderer.java
    │   │       ├── FarmerRenderer.java
    │   │       └── WarehouseWorkerRenderer.java
    │   ├── events/ModEventSubscriber.java  # Entity attributes, creative tab, caps
    │   └── client/ClientEvents.java        # Renderer registration
    └── resources/
        ├── META-INF/neoforge.mods.toml
        ├── pack.mcmeta
        └── assets/hardworkers/
            ├── blockstates/   (20 JSON — facing rotation per block variant)
            ├── models/block/  (20 JSON — simple cube models)
            ├── models/item/   (20 JSON — item display wrappers)
            ├── textures/block/(100 PNG — 5 materials × 4 sides × 5 block types)
            ├── textures/entity/(4 PNG — lumberjack, miner, farmer, warehouse_worker)
            └── lang/en_us.json
        └── data/hardworkers/
            ├── loot_table/blocks/      (5 JSON — miner tier drops)
            ├── loot_tables/blocks/     (5 JSON — lumberjack tier drops)
            └── recipes/                (20 JSON — crafting recipes)
```

---

## Build & Run Commands

```bash
# Build the mod JAR
./gradlew build

# Launch Minecraft client (for manual testing)
./gradlew runClient

# Launch dedicated server (headless)
./gradlew runServer

# Run data generation (regenerates recipes, loot tables, etc.)
./gradlew runData
```

The compiled JAR lands in `build/libs/hardworkers-<version>.jar`. There is no test suite; correctness is verified by running the game.

### ASM Version Lock

NeoForge 21.1.x and NeoGradle pull in conflicting ASM versions. `build.gradle` forces all ASM artifacts to `9.7` — do not remove this or the game will fail to launch.

---

## Release Pipeline

Releases are fully automated via `.github/workflows/release.yml`:

1. Triggered when a PR is **merged** into `main`.
2. Increments `mod_version` in `gradle.properties` (minor digit; rolls major when minor reaches 10: `0.9 → 1.0`).
3. Runs `./gradlew build`.
4. Commits the version bump with `[skip ci]`.
5. Creates a GitHub release with auto-generated notes and attaches the JAR.

**Never manually edit `mod_version` in a PR** — the CI handles it.

---

## Architecture & Key Patterns

### Tier System

Every worker type defines a tier enum (`LumberjackTier`, `MinerTier`, `FarmerTier`, `WarehouseTier`) holding performance constants:

| Worker      | Tier constants                                     |
|-------------|-----------------------------------------------------|
| Lumberjack  | `chopInterval` (ticks per log): Wood=40 → Netherite=6 |
| Miner       | `mineInterval` (ticks per block): Wood=40 → Netherite=6 |
| Farmer      | `harvestInterval`, `growthBoostInterval` (0=disabled) |
| Warehouse   | `stacksPerTrip`, `moveSpeed`                        |

Each tier maps to one block, one `BlockItem`, and one `BlockEntity` type that is shared across all five tiers of a worker.

### Block → Entity Lifecycle

1. **Placement** (`onPlace`): spawns entity at `(x+0.5, y+1, z+0.5)`, stores home block pos in entity NBT, calls `setTierEquipment()` to equip the right tool.
2. **Removal** (`onRemove`): drops the block entity inventory as item entities, then kills the corresponding entity within a 3-block radius.
3. **Entity self-check** (every 100 ticks): entity discards itself if home block is air (covers chunk-unload edge cases).

### Storage Pattern

All four block entities expose a 27-slot `Container` + `IItemHandler` (via `InvWrapper`):

- **Two-pass insert**: merge into existing partial stacks first, then fill empty slots.
- **Full check**: workers stop working when `isFull()` returns `true`.
- **Overflow**: any items that don't fit are spawned as item entities at the block position.
- **Hopper/pipe compatible**: `Capabilities.ItemHandler.BLOCK` capability is registered for all four block entity types in `ModEventSubscriber.onRegisterCapabilities`.

### AI Goals

Each entity registers goals in priority order:

```
Priority 1: FloatGoal             (don't drown)
Priority 2: <Primary work goal>   (worker-specific)
Priority 3: RandomStrollInHolderGoal / WaterAvoidingRandomStrollGoal
Priority 4: RandomLookAroundGoal
```

Worker-specific goals:

| Entity              | Goals                            |
|---------------------|----------------------------------|
| LumberjackEntity    | FindTreeGoal → ChopTreeGoal      |
| MinerEntity         | MineForwardGoal                  |
| FarmerEntity        | HarvestCropsGoal                 |
| WarehouseWorkerEntity | CollectItemsGoal               |

### Lumberjack Detail

- **FindTreeGoal**: BFS scan within `LUMBERJACK_SEARCH_RADIUS` (config, default 16) around home block. Identifies log base (log with non-log below). Avoids trees claimed by other lumberjacks. Hands off via `entity.setTargetTree()`.
- **ChopTreeGoal**: BFS collects up to 256 connected logs + 3-block margin for leaves. Chops at `chopInterval` ticks. Plants matching sapling on break. Deposits drops into block entity.

### Miner Detail

- **MineForwardGoal**: Mines a 3-tall (Y, Y+1, Y+2) tunnel in the block's facing direction, advancing from depth 1 to `MAX_DEPTH` (64). Tier gates which blocks can be mined (WOOD can't mine NEEDS_IRON_TOOL blocks, etc.). Persists `currentDepth` in NBT.

### Farmer Detail

- **HarvestCropsGoal**: Scans a 5×5 (radius=2) area. Harvests mature crops and replants. Supports: wheat, carrots, potatoes, beetroot, nether wart, cocoa, sweet berries, melons, pumpkins.
- **FarmerBlockEntity server tick**: every 20 ticks hydrates farmland (radius=3, moisture=7); every `growthBoostInterval` ticks applies random-tick boosts to crops in the farm area.

### Warehouse Detail

- **CollectItemsGoal**: Scans 32-block radius for any non-empty `LumberjackBlockEntity`, `MinerBlockEntity`, or `FarmerBlockEntity`. Collects up to `stacksPerTrip` stacks per journey, then returns home to deposit into `WarehouseBlockEntity`.
- **WarehouseBlockEntity**: implements `MenuProvider` to open a vanilla `ChestMenu.threeRows` GUI on right-click.

---

## Adding a New Worker Type

1. Create `NewWorkerTier.java` enum in `block/` with tier constants.
2. Create `NewWorkerBlock.java` extending `BaseEntityBlock`; follow existing block pattern (FACING property, `onPlace`/`onRemove`, `useWithoutItem`).
3. Create `NewWorkerBlockEntity.java` with 27-slot storage + `IItemHandler`.
4. Create `NewWorkerEntity.java` extending `PathfinderMob`; set attributes via `createAttributes()`.
5. Create an AI goal in `entity/ai/`.
6. Create `NewWorkerRenderer.java` in `entity/client/`.
7. Register blocks in `ModBlocks`, items in `ModItems`, block entity in `ModBlockEntities`, entity in `ModEntities`.
8. Add entity attributes in `ModEventSubscriber.onEntityAttributeCreation`.
9. Add renderer in `ClientEvents`.
10. Add blockstate, model, item model, textures, lang entries, recipes, loot tables.

---

## Adding a New Tier to an Existing Worker

1. Add the enum constant with its performance values to the tier enum.
2. Add the block registration in `ModBlocks`.
3. Add the item registration in `ModItems`.
4. Add blockstate JSON, block model JSON, item model JSON.
5. Add 5 texture PNGs (top, bottom, front, back, side).
6. Add lang entry in `en_us.json`.
7. Add crafting recipe JSON in `data/hardworkers/recipes/`.
8. Add loot table JSON in `data/hardworkers/loot_tables/blocks/` (or `loot_table/`).

---

## Configuration

Defined in `HardWorkersConfig.java` and registered as `COMMON` config:

| Key                        | Default | Range   | Effect                                |
|----------------------------|---------|---------|---------------------------------------|
| `lumberjackSearchRadius`   | 16      | 4–64    | Block radius lumberjack scans for trees |

Config file: `config/hardworkers-common.toml` (in the Minecraft instance directory).

---

## Resource Naming Convention

All resource names follow `{workertype}_{tier}` — e.g.:
- `lumberjack_wood`, `miner_iron`, `farmer_diamond`, `warehouse_netherite`

Block entity registry names use the same pattern. Texture files use the same stem.

---

## Key Files for Common Tasks

| Task                              | File(s)                                                    |
|-----------------------------------|------------------------------------------------------------|
| Change worker speed/intervals     | `block/*Tier.java`                                         |
| Add a new crop type               | `entity/ai/HarvestCropsGoal.java`                          |
| Change storage size               | `blockentity/*BlockEntity.java` (INVENTORY_SIZE constant)  |
| Change mining depth               | `entity/ai/MineForwardGoal.java` (MAX_DEPTH)               |
| Change tree search radius         | `HardWorkersConfig.java` + `entity/ai/FindTreeGoal.java`   |
| Change entity model/texture       | `entity/client/*Renderer.java`                             |
| Add item to creative tab          | `events/ModEventSubscriber.java` (onBuildCreativeTab)      |
| Add hopper support to new BE      | `events/ModEventSubscriber.java` (onRegisterCapabilities)  |
| Change crafting recipe            | `src/main/resources/data/hardworkers/recipes/*.json`       |
| Change block drops                | `src/main/resources/data/hardworkers/loot_table*/blocks/`  |

---

## Version Management

`mod_version` in `gradle.properties` is the single source of truth. It is substituted into `neoforge.mods.toml` at build time via `ProcessResources`. The CI increments it automatically on every merged PR — do not increment it manually in PRs.
