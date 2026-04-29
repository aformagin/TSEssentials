package com.thirdspare.modules.api;

public record TSEModuleDescriptor(
        String id,
        String displayName,
        String version,
        String minCoreVersion,
        String maxCoreVersion
) {
}
