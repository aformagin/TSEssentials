package com.thirdspare.modules.permissions;

import com.hypixel.hytale.server.core.util.Config;
import com.thirdspare.modules.permissions.data.PermissionsGroup;
import com.thirdspare.modules.permissions.data.PermissionsGroupsConfig;
import com.thirdspare.modules.permissions.data.PermissionsUserRecord;
import com.thirdspare.modules.permissions.data.PermissionsUsersConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class PermissionsManager {
    private static final Pattern GROUP_PATTERN = Pattern.compile("^[a-z0-9_-]{2,32}$");
    private static final Pattern NODE_PATTERN = Pattern.compile("^-?(\\*|[a-z0-9_-]+(\\.[a-z0-9_*_-]+)*)$");

    private final Config<PermissionsGroupsConfig> groupsConfig;
    private final Config<PermissionsUsersConfig> usersConfig;
    private PermissionsGroupsConfig groups;
    private PermissionsUsersConfig users;

    public PermissionsManager(Config<PermissionsGroupsConfig> groupsConfig,
                              PermissionsGroupsConfig groups,
                              Config<PermissionsUsersConfig> usersConfig,
                              PermissionsUsersConfig users) {
        this.groupsConfig = groupsConfig;
        this.usersConfig = usersConfig;
        this.groups = groups != null ? groups : new PermissionsGroupsConfig();
        this.users = users != null ? users : new PermissionsUsersConfig();
        ensureDefaults();
    }

    public synchronized boolean ensureDefaults() {
        boolean changed = false;
        if (groups.getSchemaVersion() < 1) {
            groups.setSchemaVersion(1);
            changed = true;
        }
        if (users.getSchemaVersion() < 1) {
            users.setSchemaVersion(1);
            changed = true;
        }
        groups.normalizeKeys();
        users.normalizeKeys();
        boolean foundDefault = false;
        for (PermissionsGroup group : groups.getAllGroups()) {
            if (!group.isDefaultGroup()) {
                continue;
            }
            if (!foundDefault) {
                foundDefault = true;
            } else {
                group.setDefaultGroup(false);
                changed = true;
            }
        }
        if (changed) {
            saveAll();
        }
        return changed;
    }

    public synchronized void reload() {
        groups = groupsConfig.load().join();
        users = usersConfig.load().join();
        ensureDefaults();
    }

    public synchronized PermissionsGroupsConfig groupsConfig() {
        return groups;
    }

    public synchronized PermissionsUsersConfig usersConfig() {
        return users;
    }

    public synchronized List<PermissionsGroup> listGroups() {
        List<PermissionsGroup> result = new ArrayList<>(groups.getAllGroups());
        result.sort(Comparator.comparing(PermissionsGroup::getName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    public synchronized Optional<PermissionsGroup> getGroup(String name) {
        return Optional.ofNullable(groups.getGroup(name));
    }

    public synchronized Optional<PermissionsGroup> getDefaultGroup() {
        return groups.getAllGroups().stream()
                .filter(PermissionsGroup::isDefaultGroup)
                .findFirst();
    }

    public synchronized MutationResult createGroup(String name, String displayName) {
        String normalized = PermissionsGroup.normalizeName(name);
        if (!isValidGroupName(normalized)) {
            return MutationResult.error("Group names must be 2-32 characters using letters, numbers, underscores, or hyphens.");
        }
        if (groups.hasGroup(normalized)) {
            return MutationResult.error("Group already exists: " + normalized);
        }
        groups.setGroup(new PermissionsGroup(normalized, displayName == null || displayName.isBlank() ? normalized : displayName));
        saveGroups();
        return MutationResult.success("Created group " + normalized + ".");
    }

    public synchronized MutationResult deleteGroup(String name) {
        PermissionsGroup group = groups.getGroup(name);
        if (group == null) {
            return MutationResult.error("Unknown group: " + PermissionsGroup.normalizeName(name));
        }
        if (group.isProtectedGroup()) {
            return MutationResult.error("Group is protected: " + group.getName());
        }
        groups.removeGroup(group.getName());
        int removed = 0;
        for (PermissionsUserRecord record : users.getAllUsers()) {
            if (record.getGroups().remove(group.getName())) {
                removed++;
            }
        }
        saveAll();
        return MutationResult.success("Deleted group " + group.getName() + " and updated " + removed + " user record(s).");
    }

    public synchronized MutationResult addNode(String groupName, String node) {
        PermissionsGroup group = groups.getGroup(groupName);
        if (group == null) {
            return MutationResult.error("Unknown group: " + PermissionsGroup.normalizeName(groupName));
        }
        String normalized = normalizeNode(node);
        if (!isValidNode(normalized)) {
            return MutationResult.error("Permission nodes must be dotted lowercase names, wildcard nodes, or negative nodes.");
        }
        if (!group.getPermissionNodes().add(normalized)) {
            return MutationResult.error("Group already has node: " + normalized);
        }
        saveGroups();
        return MutationResult.success("Added " + normalized + " to " + group.getName() + ".");
    }

    public synchronized MutationResult removeNode(String groupName, String node) {
        PermissionsGroup group = groups.getGroup(groupName);
        if (group == null) {
            return MutationResult.error("Unknown group: " + PermissionsGroup.normalizeName(groupName));
        }
        String normalized = normalizeNode(node);
        if (!group.getPermissionNodes().remove(normalized)) {
            return MutationResult.error("Group does not have node: " + normalized);
        }
        saveGroups();
        return MutationResult.success("Removed " + normalized + " from " + group.getName() + ".");
    }

    public synchronized MutationResult setDefaultGroup(String groupName, boolean defaultGroup) {
        PermissionsGroup group = groups.getGroup(groupName);
        if (group == null) {
            return MutationResult.error("Unknown group: " + PermissionsGroup.normalizeName(groupName));
        }
        if (!defaultGroup) {
            if (!group.isDefaultGroup()) {
                return MutationResult.error(group.getName() + " is not the default group.");
            }
            group.setDefaultGroup(false);
            saveGroups();
            return MutationResult.success("Cleared default group.");
        }

        String previous = null;
        for (PermissionsGroup candidate : groups.getAllGroups()) {
            if (candidate.isDefaultGroup() && !candidate.getName().equals(group.getName())) {
                previous = candidate.getName();
                candidate.setDefaultGroup(false);
            }
        }
        if (group.isDefaultGroup() && previous == null) {
            return MutationResult.error(group.getName() + " is already the default group.");
        }
        group.setDefaultGroup(true);
        saveGroups();
        if (previous != null) {
            return MutationResult.success("Changed default group from " + previous + " to " + group.getName() + ".");
        }
        return MutationResult.success("Set " + group.getName() + " as the default group.");
    }

    public synchronized MutationResult addUserToGroup(UUID uuid, String lastKnownUsername, String groupName) {
        if (uuid == null) {
            return MutationResult.error("A UUID is required for offline membership changes.");
        }
        String normalizedGroup = PermissionsGroup.normalizeName(groupName);
        if (!groups.hasGroup(normalizedGroup)) {
            return MutationResult.error("Unknown group: " + normalizedGroup);
        }
        PermissionsUserRecord record = users.ensureUser(uuid, lastKnownUsername);
        if (!record.getGroups().add(normalizedGroup)) {
            return MutationResult.error(displayName(record) + " is already in " + normalizedGroup + ".");
        }
        saveUsers();
        return MutationResult.success("Added " + displayName(record) + " to " + normalizedGroup + ".");
    }

    public synchronized MutationResult removeUserFromGroup(UUID uuid, String groupName) {
        if (uuid == null) {
            return MutationResult.error("A UUID is required.");
        }
        PermissionsUserRecord record = users.getUser(uuid);
        String normalizedGroup = PermissionsGroup.normalizeName(groupName);
        if (record == null || !record.getGroups().remove(normalizedGroup)) {
            return MutationResult.error("User is not in " + normalizedGroup + ".");
        }
        saveUsers();
        return MutationResult.success("Removed " + displayName(record) + " from " + normalizedGroup + ".");
    }

    public synchronized Set<String> groupsForUser(UUID uuid) {
        PermissionsUserRecord record = users.getUser(uuid);
        Set<String> result = new HashSet<>();
        getDefaultGroup().ifPresent(group -> result.add(group.getName()));
        if (record != null) {
            result.addAll(record.getGroups());
        }
        return Set.copyOf(result);
    }

    public synchronized Optional<PermissionsUserRecord> getUser(UUID uuid) {
        return Optional.ofNullable(users.getUser(uuid));
    }

    public synchronized Optional<PermissionsUserRecord> findUser(String target) {
        return Optional.ofNullable(users.findByNameOrUuid(target));
    }

    public synchronized List<PermissionsUserRecord> membersOf(String groupName) {
        String normalized = PermissionsGroup.normalizeName(groupName);
        List<PermissionsUserRecord> result = new ArrayList<>();
        for (PermissionsUserRecord record : users.getAllUsers()) {
            if (record.getGroups().contains(normalized)) {
                result.add(record);
            }
        }
        result.sort(Comparator.comparing(this::displayName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    public CompletableFuture<Void> saveGroups() {
        groups.normalizeKeys();
        return groupsConfig.save();
    }

    public CompletableFuture<Void> saveUsers() {
        users.normalizeKeys();
        return usersConfig.save();
    }

    public CompletableFuture<Void> saveAll() {
        return CompletableFuture.allOf(saveGroups(), saveUsers());
    }

    public static boolean isValidGroupName(String name) {
        return name != null && GROUP_PATTERN.matcher(name).matches();
    }

    public static boolean isValidNode(String node) {
        return node != null && NODE_PATTERN.matcher(node).matches();
    }

    public static String normalizeNode(String node) {
        return node == null ? "" : node.trim().toLowerCase();
    }

    private String displayName(PermissionsUserRecord record) {
        String username = record.getLastKnownUsername();
        return username == null || username.isBlank() ? record.getUuidString() : username;
    }

    public record MutationResult(boolean success, String message) {
        public static MutationResult success(String message) {
            return new MutationResult(true, message);
        }

        public static MutationResult error(String message) {
            return new MutationResult(false, message);
        }
    }
}
