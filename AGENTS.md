# TSEssentials - Hytale Server Plugin Agent Guide

## 0. Agent Operating Rules

- Read this file before making changes.
- Prefer small, focused changes over large rewrites.
- Do not invent Hytale APIs. If unsure, inspect existing source code or research docs first.
- Keep implementation consistent with existing package structure.
- Preserve current working features unless the task explicitly asks to refactor them.
- Update documentation when adding or changing major behavior.
- Before finalizing, run available build/test commands and report results.

---

## 1. Project Overview
TSEssentials is a foundational Hytale server plugin providing essential server management features. The goal is to create a comprehensive "essentials" suite for server administrators.

- **Implemented Features:** Homes, Warps, Spawn, TPA (Teleport Request System).
- **Planned Features:** Economy, Chat Channels, and Land Claims.

This document serves as a guide for AI agents working on the project, providing architectural details, API references, and development patterns.

---

## 2. Planning & Documentation
Detailed planning and design documents for upcoming features can be found in the `Research/Planning Documents` directory. These are the primary source of truth for feature requirements.

-   **[[TSEssentials (Hytale Plugin)]]** - Main project overview and completed features.
-   **[[TSEconomy_Planning]]** - Digital currency and player shop system design.
-   **[[TSChat_Planning]]** - Chat channels, focus modes, and nickname system.
-   **[[TSClaims_Planning]]** - Land protection and trusted member system.

---

## 3. Feature Implementation Workflow

When implementing a feature:

1. Read the relevant planning document in `Research/Planning Documents`.
2. Inspect existing commands, data models, and utilities.
3. Design the smallest change that fits the current architecture.
4. Implement using existing patterns where possible.
5. Add or update persistence models if needed.
6. Validate with Maven.
7. Summarize changed files and behavior.

---

## 4. Architecture

### 4.0. Project File Structure
```
.
├── AGENTS.md
├── Chat Progress Report.md
├── pom.xml
├── README.md
├── .gitignore
├── .gitattributes
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── thirdspare/
│       │           ├── TSEssentials.java
│       │           ├── commands/
│       │           ├── data/
│       │           ├── events/
│       │           ├── tpa/
│       │           └── utils/
│       └── resources/
│           └── manifest.json
└── Research/
    ├── EntityStore-Research.md
    ├── HTDevLib-Research.md
    └── Planning Documents/
        ├── TSChat_Planning.md
        ├── TSClaims_Planning.md
        ├── TSEconomy_Planning.md
        └── TSEssentials (Hytale Plugin).md
```

### 4.1. Package Structure
The project follows a feature-based package structure.

```
com.thirdspare/
├── TSEssentials.java
├── commands/
├── data/
├── tpa/
├── events/
└── utils/
```

---

## 5. Coding Conventions

- Use clear Java class names ending in `Command`, `Config`, `Manager`, or `Component` where appropriate.
- Keep command parsing thin; place reusable logic in helper or manager classes.
- Avoid duplicating teleportation, player lookup, or messaging logic.
- Prefer clean, readable methods over overly clever code.
- Use meaningful error messages for players and admins.

---

## 6. Persistence Decision Rules

| Data Type | Preferred Storage |
|----------|------------------|
| Per-player persistent data | ECS Component |
| Server-wide settings | JSON config using Codec |
| Runtime-only temporary state | In-memory manager |
| TPA requests/timeouts | In-memory |
| Economy balances | ECS Component |
| Homes/chat preferences | ECS Component (migrate when touched) |
| Warps/spawn/claims | JSON config |

---

## 7. Hytale API & Development Patterns

### 7.1. Custom ECS Components (for Per-Player Data)

Use ECS components for per-player persistent data.

**Advantages:**
- Automatic persistence
- Efficient runtime access

Follow existing patterns for CODEC-based components and registration.

### 7.2. World & Spawn Management

Use `GlobalSpawnProvider` instead of manual spawn handling.

---

## 8. Build & Validation

Use Maven:

```bash
mvn clean package
```

Before completing a task:
1. Run the build
2. Fix compile errors
3. Report if build could not be executed

---

## 9. Do Not

- Do not replace working systems with speculative rewrites.
- Do not add external dependencies without justification.
- Do not hardcode player UUIDs, world names, or file paths.
- Do not silently swallow exceptions.
- Do not migrate persistence formats without a compatibility plan.
- Do not assume Minecraft/Paper APIs apply to Hytale.

---

## 10. Final Response Format for Agents

When completing a task, respond with:

- Summary of changes
- Files modified
- Build/test results
- Known limitations
- Suggested next steps

---

## 11. Useful Resources

- [Hytale Server Unpacked](https://github.com/Ranork/Hytale-Server-Unpacked) - Decompiled API reference
- [Hytale ECS Guide](https://hytalemodding.dev/en/docs/guides/ecs/hytale-ecs) - Entity Component System docs
- [Storing Persistent Data on Players](https://hytalemodding.dev/en/docs/guides/plugin/store-persistant-data) - Custom component persistence guide
- [ECS Components Reference](https://hytale-docs.pages.dev/modding/ecs/components/) - Component definition and codec patterns
- [Player Persistence](https://hytale-docs.pages.dev/modding/ecs/player-persistence/) - Player data save/load lifecycle
- [World Configuration](https://hytale-docs.pages.dev/server/world-config/) - SpawnProvider and world settings
- [Corpse Plugin](https://github.com/Leclowndu93150/Corpse) - Death handling example
- [Economy Plugin](https://github.com/Ryukazan/Economy) - Custom component example (MoneyComponent)
- [Essentials Plugin](https://github.com/nhulston/Essentials) - Full essentials plugin with GlobalSpawnProvider usage

---

## 12. UI Styling Paradigms

All UI documents (`.ui` files) should follow these styling and layout conventions to ensure a consistent look and feel across the plugin.

### 12.1. Panel Sizing & Layout
- **Standard Admin/View Panel:** `Width: 760, Height: 560`
- **Large Management Panel:** `Width: 1120, Height: 720`
- **Main Overlay Background:** `#000000(0.35)`
- **Main Panel Background:** `#111111(0.90)`
- **Outlines:** `OutlineSize: 1`, `OutlineColor: #ffffff(0.18)`
- **Padding:** Standard `Full: 18` for main containers.

### 12.2. Typography & Colors
- **Main Titles:** `FontSize: 24`, `TextColor: #ffffff` or `#f1ba50` (Gold), `RenderBold: true`.
- **Section Titles:** `FontSize: 14`, `TextColor: #f1ba50`, `RenderBold: true`, `RenderUppercase: true`.
- **Field Labels:** `FontSize: 12`, `TextColor: #bbbbbb`, `RenderUppercase: true`.
- **Body Text:** `FontSize: 15`, `TextColor: #ffffff`.
- **Status/Success Messages:** `TextColor: #bdf2c3`.
- **Error Messages:** `TextColor: #ffb4b4`.

### 12.3. Common Background Colors
- **Header:** `#343434(0.92)`
- **Sidebar/Left Pane:** `#252525(0.92)`
- **Content/Right Pane:** `#414141(0.92)`

---

## 13. Command Structure Standards

To ensure type safety and avoid parameter naming mix-ups, all future commands and subcommands MUST follow this structure:

1. **Argument Declaration:** Declare all `RequiredArg` and `OptionalArg` as `private final` fields.
2. **Initialization:** Initialize arguments in the constructor using `withRequiredArg(name, description, type)` or `withOptionalArg(...)`.
3. **Retrieval:** ALWAYS use the argument object to retrieve values from the `CommandContext`. Never use string literals.

**Example:**
```java
public class ExampleSubCommand extends AbstractCommand {
    private final RequiredArg<String> nameArg;

    public ExampleSubCommand() {
        super("create", "Create something");
        // Initialize argument
        this.nameArg = withRequiredArg("name", "Name of the object", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext context) {
        // Retrieve value using the field
        String name = context.get(nameArg);
        // ...
        return CompletableFuture.completedFuture(null);
    }
}
```

---

## 14. Modular System Design

TSEssentials uses a "drop-in" module system allowing optional features to be added as separate JAR files.

### 14.1. Architecture
- **API Entry Point:** `com.thirdspare.modules.api.TSEModule`
- **Discovery:** Uses Java's `ServiceLoader`. Modules must provide a service file in `META-INF/services/com.thirdspare.modules.api.TSEModule`.
- **Isolation:** Each module is loaded into its own classloader to prevent dependency conflicts.

### 14.2. Lifecycle Hooks
- `register(TSEModuleContext context)`: Primary setup phase. Register configs, commands, and ECS components here.
- `enable()`: Called after all modules are registered. Activate main logic/listeners here.
- `disable()`: Cleanup phase. Unregister listeners and clear temporary state.

### 14.3. Module Packaging
- JAR files must be placed in the `TSEssentialsModules` directory.
- JAR names should follow the pattern `TSEssentials-<ModuleName>-<Version>.jar`.
- Module configurations are automatically isolated to `UserData/ModData/TSEssentials/modules/<module-id>/`.

