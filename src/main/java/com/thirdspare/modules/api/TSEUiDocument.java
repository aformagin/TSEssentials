package com.thirdspare.modules.api;

import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;

import java.util.Objects;

public final class TSEUiDocument {
    private final String documentName;
    private final String resourcePath;

    public TSEUiDocument(String documentName, String resourcePath) {
        this.documentName = requireText(documentName, "documentName");
        this.resourcePath = requireText(resourcePath, "resourcePath");
    }

    public String documentName() {
        return documentName;
    }

    public String resourcePath() {
        return resourcePath;
    }

    public UICommandBuilder appendTo(UICommandBuilder builder) {
        return appendTo(builder, null);
    }

    public UICommandBuilder appendTo(UICommandBuilder builder, String selector) {
        Objects.requireNonNull(builder, "builder");
        if (selector == null || selector.isBlank()) {
            return builder.append(documentName);
        }
        return builder.append(selector, documentName);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return value;
    }
}
