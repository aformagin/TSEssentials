package com.thirdspare.modules.core;

import com.thirdspare.TSEssentials;

import java.nio.file.Path;

public final class ModulePaths {
    public static final String MODULES_DIRECTORY_NAME = "TSEssentialsModules";

    private ModulePaths() {
    }

    public static Path defaultModulesDirectory(TSEssentials core) {
        Path coreFile = core.getFile();
        if (coreFile != null && coreFile.getParent() != null) {
            return coreFile.getParent().resolve(MODULES_DIRECTORY_NAME);
        }
        Path baseDirectory = core.getDataDirectory().getParent() != null ? core.getDataDirectory().getParent() : core.getDataDirectory();
        return baseDirectory.resolve(MODULES_DIRECTORY_NAME);
    }

    public static Path dataDirectory(TSEssentials core, String moduleId) {
        return core.getDataDirectory().resolve("modules").resolve(moduleId);
    }
}
