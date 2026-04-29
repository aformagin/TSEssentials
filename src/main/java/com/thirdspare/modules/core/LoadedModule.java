package com.thirdspare.modules.core;

import com.thirdspare.modules.api.TSEModule;
import com.thirdspare.modules.api.TSEModuleDescriptor;

import java.io.Closeable;
import java.nio.file.Path;

public record LoadedModule(
        TSEModule module,
        TSEModuleDescriptor descriptor,
        Path jarPath,
        Closeable classLoader
) {
}
