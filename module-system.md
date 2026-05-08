# TSEssentials Extension Plugin Developer Guide

TSEssentials supports optional features through normal Hytale plugins that declare a dependency on the core TSEssentials plugin. This keeps load ordering, asset loading, and dependency failures in Hytale's supported plugin system instead of a custom module classloader.

## 1. Requirements

-   **Java Development Kit (JDK) 25**: Matches the Hytale and TSEssentials core requirement.
-   **TSEssentials Core Plugin**: Extension plugins must depend on the core `Thirdspare:TSEssentials` plugin.
-   **Hytale Plugin Manifest**: Each extension plugin must include a root `manifest.json`.

## 2. Plugin Dependency

Add TSEssentials as a provided Maven dependency:

```xml
<dependency>
    <groupId>com.thirdspare</groupId>
    <artifactId>TSEssentials</artifactId>
    <version>1.1.0</version>
    <scope>provided</scope>
</dependency>
```

Declare the Hytale plugin dependency in your extension plugin `manifest.json`:

```json
{
  "Group": "Thirdspare",
  "Name": "TSEssentialsExample",
  "Version": "1.0.0",
  "ServerVersion": "2026.03.26-89796e57b",
  "Dependencies": {
    "Thirdspare:TSEssentials": ">=1.1.0"
  },
  "Main": "com.example.tse.ExamplePlugin"
}
```

Set `"IncludesAssetPack": true` when the plugin ships UI documents or other assets under `Common/`.

## 3. Plugin Entry Point

Extension plugins should extend Hytale's `JavaPlugin` directly:

```java
public final class ExamplePlugin extends JavaPlugin {
    @Override
    public void setup() {
        registerCommand(new ExampleCommand());
        eventRegistry().register(ExampleEvent.class, this::handleExampleEvent);
    }

    @Override
    public void start() {
        logger().atInfo().log("TSEssentials example extension enabled.");
    }

    @Override
    public void shutdown() {
        // Clear runtime-only state here when needed.
    }
}
```

Use the plugin's own inherited registration APIs for commands, events, configs, ECS components, entity systems, and asset-backed UI documents.

## 4. Shared TSEssentials API

Core exposes `com.thirdspare.api.TSEssentialsApi` for small shared extension points. Current first-party modules use it to register permission-node metadata so the permissions plugin can display nodes from core and sibling plugins.

Register public permission constants during setup:

```java
TSEssentialsApi.registerPermissionConstants(
    ExamplePermissions.class,
    "Example Plugin",
    "Example command permission"
);
```

## 5. UI Resources

Package UI documents inside the extension plugin JAR:

```text
src/main/resources/Common/UI/Custom/Example.ui
```

With `"IncludesAssetPack": true`, open UI documents through the normal Custom UI append path:

```java
UICommandBuilder builder = new UICommandBuilder();
builder.append("Example.ui");
```

## 6. Build and Deploy

1.  Build the core plugin and extension plugin JARs.
2.  Place `TSEssentials-1.1.0.jar` directly in `UserData/Mods`.
3.  Place extension plugin JARs directly in `UserData/Mods`.
4.  Do not use the old `TSEssentialsModules` folder.
5.  Do not include `META-INF/services/com.thirdspare.modules.api.TSEModule`; that custom loader has been retired.

## 7. Best Practices

-   Keep command parsing thin and place behavior in services or managers.
-   Use each plugin's own `withConfig(...)` registration for authoritative JSON configs.
-   Use ECS components for per-player persistent data when appropriate.
-   Keep permission strings stable after release.
-   Prefer native Hytale plugin dependencies over optional reflection between sibling plugins.
