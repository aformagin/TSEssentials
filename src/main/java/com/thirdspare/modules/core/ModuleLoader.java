package com.thirdspare.modules.core;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.thirdspare.modules.api.TSEModule;
import com.thirdspare.modules.api.TSEModuleContext;
import com.thirdspare.modules.api.TSEModuleDescriptor;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.BiFunction;
import java.util.logging.Level;

public final class ModuleLoader {
    private final Path modulesDirectory;
    private final ClassLoader parentClassLoader;
    private final HytaleLogger logger;
    private final BiFunction<Path, TSEModuleDescriptor, TSEModuleContext> contextFactory;
    private final List<LoadedModule> loadedModules = new ArrayList<>();
    private final List<ModuleLoadFailure> failures = new ArrayList<>();

    public ModuleLoader(Path modulesDirectory,
                        ClassLoader parentClassLoader,
                        HytaleLogger logger,
                        BiFunction<Path, TSEModuleDescriptor, TSEModuleContext> contextFactory) {
        this.modulesDirectory = modulesDirectory;
        this.parentClassLoader = parentClassLoader;
        this.logger = logger;
        this.contextFactory = contextFactory;
    }

    public List<LoadedModule> discoverAndRegister() {
        loadedModules.clear();
        failures.clear();
        log(Level.INFO, "Scanning optional TSEssentials modules from " + modulesDirectory);
        if (modulesDirectory == null || !Files.isDirectory(modulesDirectory)) {
            log(Level.INFO, "Optional module directory not found: " + modulesDirectory);
            return List.copyOf(loadedModules);
        }

        List<Path> jars;
        try (var stream = Files.list(modulesDirectory)) {
            jars = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith("TSEssentials-"))
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        } catch (IOException ex) {
            recordFailure(modulesDirectory, "Unable to scan optional module directory.", ex);
            return List.copyOf(loadedModules);
        }

        for (Path jar : jars) {
            log(Level.INFO, "Inspecting optional module candidate " + jar.getFileName());
            loadJar(jar);
        }
        if (loadedModules.isEmpty()) {
            log(Level.INFO, "No optional TSEssentials modules loaded.");
        }
        return List.copyOf(loadedModules);
    }

    public void enableAll() {
        for (LoadedModule loaded : loadedModules) {
            try {
                loaded.module().enable();
                log(Level.INFO, "Enabled optional module " + loaded.descriptor().displayName());
            } catch (Exception ex) {
                recordFailure(loaded.jarPath(), "Unable to enable module " + loaded.descriptor().id() + ".", ex);
            }
        }
    }

    public void disableAll() {
        for (LoadedModule loaded : loadedModules.reversed()) {
            try {
                loaded.module().disable();
            } catch (Exception ex) {
                recordFailure(loaded.jarPath(), "Unable to disable module " + loaded.descriptor().id() + ".", ex);
            }
            try {
                loaded.classLoader().close();
            } catch (IOException ex) {
                recordFailure(loaded.jarPath(), "Unable to close module classloader.", ex);
            }
        }
    }

    public void onPlayerReady(PlayerRef player) {
        for (LoadedModule loaded : loadedModules) {
            try {
                loaded.module().onPlayerReady(player);
            } catch (RuntimeException ex) {
                recordFailure(loaded.jarPath(), "Module player-ready hook failed for " + loaded.descriptor().id() + ".", ex);
            }
        }
    }

    public Optional<LoadedModule> getModule(String moduleId) {
        return loadedModules.stream()
                .filter(loaded -> loaded.descriptor().id().equalsIgnoreCase(moduleId))
                .findFirst();
    }

    public <T extends TSEModule> Optional<T> getModule(String moduleId, Class<T> type) {
        return getModule(moduleId)
                .map(LoadedModule::module)
                .filter(type::isInstance)
                .map(type::cast);
    }

    public List<ModuleLoadFailure> failures() {
        return List.copyOf(failures);
    }

    private void loadJar(Path jar) {
        URLClassLoader classLoader = null;
        try {
            URL url = jar.toUri().toURL();
            classLoader = new URLClassLoader(new URL[]{url}, parentClassLoader);
            ServiceLoader<TSEModule> serviceLoader = ServiceLoader.load(TSEModule.class, classLoader);
            int found = 0;
            for (TSEModule module : serviceLoader) {
                found++;
                TSEModuleDescriptor descriptor = module.descriptor();
                if (descriptor == null || descriptor.id() == null || descriptor.id().isBlank()) {
                    throw new IllegalStateException("Module descriptor id is required.");
                }
                log(Level.INFO, "Registering optional module descriptor " + descriptor.id() +
                        " (" + descriptor.displayName() + ") from " + jar.getFileName());
                module.register(contextFactory.apply(jar, descriptor));
                loadedModules.add(new LoadedModule(module, descriptor, jar, classLoader));
                log(Level.INFO, "Discovered optional module " + descriptor.displayName() + " from " + jar.getFileName());
            }
            if (found == 0) {
                classLoader.close();
            }
        } catch (Throwable ex) {
            if (classLoader != null) {
                try {
                    classLoader.close();
                } catch (IOException closeEx) {
                    ex.addSuppressed(closeEx);
                }
            }
            recordFailure(jar, "Unable to load optional module.", ex);
        }
    }

    private void recordFailure(Path jarPath, String message, Throwable cause) {
        failures.add(new ModuleLoadFailure(jarPath, message, cause));
        log(Level.WARNING, message + " " + jarPath + " - " + cause.getMessage());
    }

    private void log(Level level, String message) {
        if (logger != null) {
            logger.at(level).log(message);
        }
    }
}
