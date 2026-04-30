# TSEssentials Module System Developer Guide

TSEssentials features a dynamic module system that allows developers to extend the plugin's functionality without modifying the core codebase. This guide outlines the requirements and steps to create your own "drop-in" modules.

## 1. Requirements

-   **Java Development Kit (JDK) 25**: Matches the Hytale and TSEssentials core requirement.
-   **TSEssentials Core**: Your module must depend on the `TSEssentials` JAR to access the API.
-   **ServiceLoader**: Discovery is handled via Java's native `ServiceLoader` mechanism.

## 2. Core API Components

### `TSEModule`
The entry point for your module. Every module must implement this interface.

-   `descriptor()`: Returns metadata about the module.
-   `register(TSEModuleContext context)`: Called during discovery. Use this to register configs, commands, and components.
-   `enable()`: Called after registration to activate module logic.
-   `disable()`: Called during server shutdown for cleanup.
-   `onPlayerReady(PlayerRef player)`: Optional hook for per-player initialization.

### `TSEModuleDescriptor`
A record containing module metadata:
-   `id`: Unique identifier (e.g., `"my-cool-feature"`).
-   `displayName`: Human-readable name.
-   `version`: Module version string.
-   `minCoreVersion`/`maxCoreVersion`: Compatibility range for the TSEssentials core.

### `TSEModuleContext`
Provides your module with a bridge to the core plugin's services:
-   `registerConfig(key, codec)`: Creates a persistent JSON configuration file.
-   `registerCommand(command)`: Registers a Hytale command.
-   `registerComponent(type, id, codec)`: Registers a custom ECS component for player/entity data.
-   `eventRegistry()`: Access to Hytale's global event system.
-   `logger()`: A pre-configured Hytale logger for your module.

## 3. Creating Your First Module

### Step 1: Project Setup (Maven Example)
Add TSEssentials as a provided dependency in your `pom.xml`:

```xml
<dependency>
    <groupId>com.thirdspare</groupId>
    <artifactId>TSEssentials</artifactId>
    <version>1.1.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

### Step 2: Implement `TSEModule`
Create your module class:

```java
public class MyModule implements TSEModule {
    private TSEModuleContext context;

    @Override
    public TSEModuleDescriptor descriptor() {
        return new TSEModuleDescriptor(
            "my_module", "My Custom Module", "1.0.0", "1.1.0", "1.2.0"
        );
    }

    @Override
    public void register(TSEModuleContext context) throws Exception {
        this.context = context;
        // Register commands or configs here
        context.registerCommand(new MyCustomCommand());
    }

    @Override
    public void enable() throws Exception {
        context.logger().atInfo().log("My Module Enabled!");
    }

    @Override
    public void disable() throws Exception {
        // Cleanup
    }
}
```

### Step 3: Register the Service
To allow TSEssentials to discover your module, you must create a service provider file:
1.  Create the directory: `src/main/resources/META-INF/services/`
2.  Create a file named: `com.thirdspare.modules.api.TSEModule`
3.  Inside the file, put the fully qualified name of your implementation class (e.g., `com.example.MyModule`).

### Step 4: Build and Deploy
1.  Package your module as a plain JAR (e.g., `mvn package`).
2.  Name the JAR starting with `TSEssentials-` (e.g., `TSEssentials-MyModule.jar`).
3.  Place the JAR in the `TSEssentialsModules` folder on your Hytale server.

## 4. Best Practices

-   **Isolation**: Modules are loaded into individual classloaders. Avoid relying on classes from other optional modules unless you have verified their presence.
-   **Configuration**: Use the `TSEModuleContext#registerConfig` method. This ensures your configuration is stored in the correct module-specific subdirectory (`UserData/ModData/TSEssentials/modules/<module-id>/`).
-   **UI Resources**: Custom UI documents should be packaged in the core plugin if possible, as document loading can be strict across different classloaders.
-   **Clean Shutdown**: Always use the `disable()` method to close files, unregister listeners, or stop background tasks to prevent memory leaks.
