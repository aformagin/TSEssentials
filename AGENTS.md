# TSEssentials - Hytale Server Plugin

## Project Overview
Essential commands plugin for Hytale servers providing home, warp, spawn, TPA, economy, chat management, and land claims functionality.

## Planning & Documentation
Detailed planning and design documents can be found in the `Research/Planning Documents` directory:
- [[TSEssentials (Hytale Plugin)]] - Main project overview and feature roadmap.
- [[TSEconomy_Planning]] - Economy system and player shop design.
- [[TSChat_Planning]] - Chat channel and nickname system design.
- [[TSClaims_Planning]] - Land protection and claim management design.

## Architecture

### Package Structure
```
com.thirdspare/
├── TSEssentials.java          # Main plugin entry point
├── commands/                   # Command implementations
│   ├── HomeCommand.java
│   ├── SetHomeCommand.java
│   ├── WarpCommand.java
│   ├── SetWarpCommand.java
│   ├── SpawnCommand.java
│   ├── SetSpawnCommand.java
│   ├── TpaCommand.java
│   ├── TpaHereCommand.java
│   ├── TpAcceptCommand.java
│   ├── TpDenyCommand.java
│   └── TpHereCommand.java
├── data/                       # Data models & persistence (codec-based JSON)
│   ├── PlayerDataConfig.java  # Player homes storage
│   ├── PlayerHomeData.java
│   ├── WarpConfig.java        # Server warps storage
│   ├── WarpData.java
│   ├── SpawnConfig.java       # Server spawn storage
│   └── SpawnData.java
├── tpa/                        # Teleport request system
│   ├── TeleportRequest.java
│   ├── TeleportRequestManager.java
│   └── TeleportRequestType.java
├── events/
│   └── ExampleEvent.java
└── utils/
    ├── StaticVariables.java   # Icon constants
    ├── PlayerLookup.java      # Player name lookup
    ├── Teleportation.java     # Teleport helper
    └── RespawnUtil.java       # Player respawn point utilities
```

### Data Persistence
Uses Hytale's codec system for automatic JSON serialization:
- `player_data.json` - Player homes, balances, and chat settings.
- `warp_data.json` - Server warps.
- `spawn_data.json` - Server spawn point.
- `chat_channels.json` - Chat channel configurations.
- `claims_data.json` - Land claim boundaries and ownership.
- `shop_data.json` - Player shop listings and item storage.

---

## Hytale API Reference

### Player Respawn Point Data

**Discovery**: The Hytale API exposes player respawn points (bed spawns) via `PlayerRespawnPointData`.

**Access Chain**:
```
Player → getPlayerConfigData() → getPerWorldData(worldName) → getRespawnPoints()
```

**Key Classes**:
```java
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerConfigData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerRespawnPointData;
```

**PlayerRespawnPointData Fields**:
| Field | Type | Description |
|-------|------|-------------|
| `blockPosition` | `Vector3i` | Block where respawn is anchored (bed location) |
| `respawnPosition` | `Vector3d` | Actual spawn coordinates |
| `name` | `String` | Respawn point identifier |

**Utility Class**: `RespawnUtil.java` provides helper methods:
- `hasRespawnPoint(player, worldName)` - Check if player has bed spawn
- `getRespawnPoints(player, worldName)` - Get all respawn points
- `getPrimaryRespawnPoint(player, worldName)` - Get first respawn point
- `getRespawnPosition(player, worldName)` - Get spawn coordinates
- `getRespawnBlockPosition(player, worldName)` - Get bed block position
- `getRespawnPointName(player, worldName)` - Get respawn point name
- `getRespawnPointCount(player, worldName)` - Count respawn points

### Player Death Data

**Related Classes**:
```java
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerDeathPositionData;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
```

**PlayerDeathPositionData Fields**:
- `markerId` - Unique ID for map marker
- `transform` - Death position transform
- `day` - In-game day of death

### ECS Component Access Pattern

Hytale uses Entity-Component-System architecture:
```java
// Get component from store
Player player = store.getComponent(ref, Player.getComponentType());

// Or via CommandBuffer in systems
PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
```

### World Spawn Provider

The native spawn system uses `GlobalSpawnProvider` to control where players spawn (new joins, death without bed, map marker):
```java
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;

Vector3d position = new Vector3d(x, y, z);
Vector3f rotation = new Vector3f(0, yaw, 0);
Transform spawnTransform = new Transform(position, rotation);
world.getWorldConfig().setSpawnProvider(new GlobalSpawnProvider(spawnTransform));
```

**Provider Types**:
- `GlobalSpawnProvider` - Single fixed spawn for all players
- `IndividualSpawnProvider` - Multiple spawn points assigned per-player UUID
- `FitToHeightMapSpawnProvider` - Wraps another provider, auto-adjusts Y to terrain

**Important**: Create new `Vector3d`/`Vector3f` instances rather than reusing references from `TransformComponent`, because `Transform` stores references not copies.

---

## Custom ECS Components (Persistent Player Data)

### Overview

Hytale supports registering custom components on player entities that **persist automatically** across sessions. This is the native, preferred approach for storing per-player data (balances, stats, preferences, etc.) rather than managing external JSON files.

**Two registration modes**:
- **With Codec (persistent)**: Data saved to disk automatically with the player
- **Without Codec (runtime-only)**: Data lost on disconnect/restart

### Step 1: Define the Component Class

Components must implement `Component<EntityStore>`, provide a no-arg constructor, a copy constructor, `clone()`, and a `BuilderCodec` for serialization.

```java
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.codec.BuilderCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class BalanceComponent implements Component<EntityStore> {

    public static final BuilderCodec<BalanceComponent> CODEC =
        BuilderCodec.builder(BalanceComponent.class, BalanceComponent::new)
            .append(new KeyedCodec<>("Balance", Codec.LONG),
                (c, v) -> c.balance = v,
                c -> c.balance)
            .add()
            .build();

    private long balance;

    public BalanceComponent() {
        this.balance = 0;
    }

    public BalanceComponent(BalanceComponent other) {
        this.balance = other.balance;
    }

    @Override
    public Component<EntityStore> clone() {
        return new BalanceComponent(this);
    }

    public long getBalance() { return balance; }
    public void setBalance(long balance) { this.balance = balance; }
}
```

**Codec rules**:
- Keys in `KeyedCodec` MUST start with a capital letter (e.g., `"Balance"` not `"balance"`)
- Available types: `Codec.INTEGER`, `Codec.LONG`, `Codec.FLOAT`, `Codec.DOUBLE`, `Codec.STRING`, `Codec.BOOLEAN`
- For maps: `new MapCodec<>(valueCodec, HashMap::new, false)`
- For lists: use appropriate collection codecs from `com.hypixel.hytale.codec`
- See `Server.jar/com/hypixel/hytale/codec/` for the full set of available codecs

### Step 2: Register in Plugin Setup

Register the component via `getEntityStoreRegistry()` in the plugin's `setup()` method. Store the returned `ComponentType` for runtime access.

```java
public class TSEssentials extends JavaPlugin {
    private ComponentType<EntityStore, BalanceComponent> balanceComponentType;

    @Override
    protected void setup() {
        // Persistent (saved to disk with player)
        this.balanceComponentType = this.getEntityStoreRegistry()
            .registerComponent(
                BalanceComponent.class,
                "TSEssentials_Balance",
                BalanceComponent.CODEC
            );

        // Runtime-only (not saved)
        // this.getEntityStoreRegistry().registerComponent(
        //     TempComponent.class,
        //     TempComponent::new
        // );
    }

    public ComponentType<EntityStore, BalanceComponent> getBalanceComponentType() {
        return balanceComponentType;
    }
}
```

### Step 3: Access Components at Runtime

**From `AbstractPlayerCommand`** (preferred -- gives Store/Ref directly):
```java
public class BalanceCommand extends AbstractPlayerCommand {
    @Override
    protected void execute(CommandContext ctx, Store<EntityStore> store,
            Ref<EntityStore> ref, PlayerRef playerRef, World world) {
        // Auto-creates component with defaults if absent
        BalanceComponent data = store.ensureAndGetComponent(
            ref, plugin.getBalanceComponentType());
        // data is now guaranteed non-null
    }
}
```

**From `AbstractCommand`** (current TSEssentials pattern):
```java
// Get the player's entity store and ref
PlayerRef playerRef = Universe.get().getPlayer(playerUUID);
Store<EntityStore> store = playerRef.getReference().getStore();
Ref<EntityStore> ref = playerRef.getReference();

// Read component (returns null if absent)
BalanceComponent data = store.getComponent(ref, plugin.getBalanceComponentType());

// Read or auto-create
BalanceComponent data = store.ensureAndGetComponent(
    ref, plugin.getBalanceComponentType());

// Write component
store.addComponent(ref, plugin.getBalanceComponentType(), new BalanceComponent());
```

**Thread-safe via CommandBuffer** (for use inside systems):
```java
commandBuffer.addComponent(ref, componentType, new BalanceComponent());
commandBuffer.removeComponent(ref, componentType);
BalanceComponent comp = commandBuffer.getComponent(ref, componentType);
```

### Step 4: Marker Components (Boolean Flags)

For components with no data (just presence/absence), use a singleton pattern:

```java
public class GodModeMarker implements Component<EntityStore> {
    public static final GodModeMarker INSTANCE = new GodModeMarker();

    public static final BuilderCodec<GodModeMarker> CODEC =
        BuilderCodec.builder(GodModeMarker.class, () -> INSTANCE).build();

    private GodModeMarker() {}

    @Override
    public Component<EntityStore> clone() { return INSTANCE; }
}
```

### Persistence Behavior

- Components registered **with a CODEC** are automatically serialized when the player saves and deserialized when they load. No manual file I/O needed.
- `cloneSerializable()` can be overridden to exclude transient fields from persistence while keeping them in the runtime `clone()`.
- The `PlayerConfigData` system also stores native player data (recipes, zones, respawn points) and can be accessed via `player.getPlayerConfigData()`.
- Call `config.markChanged()` on `PlayerConfigData` after modifications to trigger a save.

### Implementation Outline: Migrating TSEssentials to ECS Components

**Per-player data (homes) -- migrate to ECS component:**

1. Create `PlayerHomesComponent implements Component<EntityStore>` with a `Map<String, HomeEntry>` field
2. Define a nested `HomeEntry` with fields: worldUUID (String), x/y/z (double), pitch/yaw/roll (float)
3. Build a `CODEC` using `MapCodec` for the homes map
4. Register in `setup()` via `getEntityStoreRegistry().registerComponent()`
5. Update `HomeCommand`/`SetHomeCommand` to read/write from the player's ECS component instead of the JSON config
6. Remove `PlayerDataConfig.java`, `PlayerHomeData.java`, and the `player_data.json` config

**Server-wide data (warps, spawn) -- keep as config files:**

Warps and spawn are not per-entity data. They are server-global settings, so `withConfig()` + codec-based JSON is the correct approach. No migration needed.

**Future per-player data (balances, stats, etc.):**

Follow the same pattern as player homes: define a component class with CODEC, register it, access via `store.ensureAndGetComponent()`. Each logically distinct data domain should be its own component (e.g., `BalanceComponent`, `StatsComponent`, `PreferencesComponent`).

---

## Useful Resources

- [Hytale Server Unpacked](https://github.com/Ranork/Hytale-Server-Unpacked) - Decompiled API reference
- [Hytale ECS Guide](https://hytalemodding.dev/en/docs/guides/ecs/hytale-ecs) - Entity Component System docs
- [Storing Persistent Data on Players](https://hytalemodding.dev/en/docs/guides/plugin/store-persistant-data) - Custom component persistence guide
- [ECS Components Reference](https://hytale-docs.pages.dev/modding/ecs/components/) - Component definition and codec patterns
- [Player Persistence](https://hytale-docs.pages.dev/modding/ecs/player-persistence/) - Player data save/load lifecycle
- [World Configuration](https://hytale-docs.pages.dev/server/world-config/) - SpawnProvider and world settings
- [Corpse Plugin](https://github.com/Leclowndu93150/Corpse) - Death handling example
- [Economy Plugin](https://github.com/Ryukazan/Economy) - Custom component example (MoneyComponent)
- [Essentials Plugin](https://github.com/nhulston/Essentials) - Full essentials plugin with GlobalSpawnProvider usage