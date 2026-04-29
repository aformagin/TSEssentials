package com.thirdspare.modules.core;

public record PermissionNodeDescriptor(String node, String source, String description) implements Comparable<PermissionNodeDescriptor> {
    public PermissionNodeDescriptor {
        node = node == null ? "" : node.trim();
        source = source == null ? "" : source.trim();
        description = description == null ? "" : description.trim();
    }

    @Override
    public int compareTo(PermissionNodeDescriptor other) {
        return String.CASE_INSENSITIVE_ORDER.compare(node, other.node());
    }
}
