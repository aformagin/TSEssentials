package com.thirdspare.modules.core;

import java.nio.file.Path;

public record ModuleLoadFailure(Path jarPath, String message, Throwable cause) {
}
