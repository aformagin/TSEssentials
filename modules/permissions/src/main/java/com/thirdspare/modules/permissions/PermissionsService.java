package com.thirdspare.modules.permissions;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.thirdspare.modules.core.PermissionNodeDescriptor;
import com.thirdspare.modules.permissions.component.PlayerPermissionMembershipComponent;
import com.thirdspare.modules.permissions.data.PermissionsGroup;
import com.thirdspare.modules.permissions.data.PermissionsUserRecord;
import com.thirdspare.utils.PlayerLookup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PermissionsService {
    private final PermissionsManager manager;
    private final ComponentType<EntityStore, PlayerPermissionMembershipComponent> membershipComponentType;
    private final PermissionNodeRegistry nodeRegistry;
    private final ConcurrentHashMap<UUID, PlayerPermissionMembershipComponent> runtimeMemberships = new ConcurrentHashMap<>();
    private final AtomicLong revision = new AtomicLong();

    public PermissionsService(PermissionsManager manager,
                              ComponentType<EntityStore, PlayerPermissionMembershipComponent> membershipComponentType,
                              PermissionNodeRegistry nodeRegistry) {
        this.manager = manager;
        this.membershipComponentType = membershipComponentType;
        this.nodeRegistry = nodeRegistry;
    }

    public PermissionsManager getManager() {
        return manager;
    }

    public void onPlayerReady(PlayerRef player) {
        if (player == null) {
            return;
        }
        synchronized (manager.usersConfig()) {
            manager.usersConfig().ensureUser(player.getUuid(), player.getUsername());
            manager.saveUsers();
        }
        syncOnlineSnapshot(player);
    }

    public void syncOnlineSnapshot(PlayerRef player) {
        World world = Universe.get().getWorld(player.getWorldUuid());
        if (world == null) {
            return;
        }
        Set<String> groups = resolveGroups(player.getUuid());
        long nextRevision = revision.incrementAndGet();
        world.execute(() -> {
            if (!player.isValid()) {
                return;
            }
            try {
                Ref<EntityStore> ref = player.getReference();
                Store<EntityStore> store = ref.getStore();
                PlayerPermissionMembershipComponent stored = store.ensureAndGetComponent(ref, membershipComponentType);
                stored.setGroups(groups);
                stored.setRevision(nextRevision);
                runtimeMemberships.put(player.getUuid(), new PlayerPermissionMembershipComponent(stored));
            } catch (IllegalStateException ignored) {
                PlayerPermissionMembershipComponent runtime = new PlayerPermissionMembershipComponent();
                runtime.setGroups(groups);
                runtime.setRevision(nextRevision);
                runtimeMemberships.put(player.getUuid(), runtime);
            }
        });
    }

    public boolean hasPermission(UUID uuid, String node) {
        return evaluate(effectiveNodes(uuid), PermissionsManager.normalizeNode(node));
    }

    public Set<String> resolveGroups(UUID uuid) {
        return manager.groupsForUser(uuid);
    }

    public Set<String> effectiveNodes(UUID uuid) {
        Set<String> nodes = new HashSet<>();
        for (String groupName : resolveGroups(uuid)) {
            manager.getGroup(groupName).ifPresent(group -> nodes.addAll(group.getPermissionNodes()));
        }
        return nodes;
    }

    public List<PermissionNodeDescriptor> listKnownNodes() {
        return nodeRegistry.listNodes();
    }

    public List<PermissionsGroup> listGroups() {
        return manager.listGroups();
    }

    public Optional<PermissionsGroup> getGroup(String name) {
        return manager.getGroup(name);
    }

    public Optional<PermissionsGroup> getDefaultGroup() {
        return manager.getDefaultGroup();
    }

    public List<PermissionsUserRecord> membersOf(String groupName) {
        return manager.membersOf(groupName);
    }

    public PermissionsManager.MutationResult createGroup(String name, String displayName) {
        return manager.createGroup(name, displayName);
    }

    public PermissionsManager.MutationResult deleteGroup(String name) {
        PermissionsManager.MutationResult result = manager.deleteGroup(name);
        if (result.success()) {
            resyncLoadedPlayers();
        }
        return result;
    }

    public PermissionsManager.MutationResult addNode(String groupName, String node) {
        return manager.addNode(groupName, node);
    }

    public PermissionsManager.MutationResult removeNode(String groupName, String node) {
        return manager.removeNode(groupName, node);
    }

    public PermissionsManager.MutationResult setDefaultGroup(String groupName, boolean defaultGroup) {
        PermissionsManager.MutationResult result = manager.setDefaultGroup(groupName, defaultGroup);
        if (result.success()) {
            resyncLoadedPlayers();
        }
        return result;
    }

    public PermissionsManager.MutationResult addUserToGroup(String target, String groupName) {
        Target resolved = resolveTarget(target);
        if (resolved == null) {
            return PermissionsManager.MutationResult.error("Unknown player. Use an online name, known username, or UUID.");
        }
        PermissionsManager.MutationResult result = manager.addUserToGroup(resolved.uuid(), resolved.displayName(), groupName);
        if (result.success() && resolved.onlinePlayer() != null) {
            syncOnlineSnapshot(resolved.onlinePlayer());
        }
        return result;
    }

    public PermissionsManager.MutationResult removeUserFromGroup(String target, String groupName) {
        Target resolved = resolveTarget(target);
        if (resolved == null) {
            return PermissionsManager.MutationResult.error("Unknown player. Use an online name, known username, or UUID.");
        }
        PermissionsManager.MutationResult result = manager.removeUserFromGroup(resolved.uuid(), groupName);
        if (result.success() && resolved.onlinePlayer() != null) {
            syncOnlineSnapshot(resolved.onlinePlayer());
        }
        return result;
    }

    public Optional<TargetView> getTargetView(String target) {
        Target resolved = resolveTarget(target);
        if (resolved == null) {
            return Optional.empty();
        }
        return Optional.of(new TargetView(
                resolved.uuid(),
                resolved.displayName(),
                resolved.onlinePlayer() != null,
                manager.groupsForUser(resolved.uuid())
        ));
    }

    public void reload() {
        manager.reload();
        resyncLoadedPlayers();
    }

    public static boolean evaluate(Set<String> nodes, String requested) {
        if (requested == null || requested.isBlank()) {
            return false;
        }
        boolean allowed = false;
        for (String node : nodes) {
            if (node == null || node.isBlank()) {
                continue;
            }
            boolean negative = node.startsWith("-");
            String pattern = negative ? node.substring(1) : node;
            if (matches(pattern, requested)) {
                if (negative) {
                    return false;
                }
                allowed = true;
            }
        }
        return allowed;
    }

    static boolean matches(String pattern, String requested) {
        if ("*".equals(pattern)) {
            return true;
        }
        if (pattern.endsWith(".*")) {
            String prefix = pattern.substring(0, pattern.length() - 1);
            return requested.startsWith(prefix);
        }
        return pattern.equals(requested);
    }

    private void resyncLoadedPlayers() {
        List<UUID> uuids = new ArrayList<>(runtimeMemberships.keySet());
        for (UUID uuid : uuids) {
            PlayerRef player = Universe.get().getPlayer(uuid);
            if (player != null && player.isValid()) {
                syncOnlineSnapshot(player);
            }
        }
    }

    private Target resolveTarget(String target) {
        if (target == null || target.isBlank()) {
            return null;
        }
        PlayerRef online = PlayerLookup.findPlayerByName(target).orElse(null);
        if (online != null) {
            return new Target(online.getUuid(), online.getUsername(), online);
        }
        try {
            UUID uuid = UUID.fromString(target.trim());
            PlayerRef player = Universe.get().getPlayer(uuid);
            if (player != null && player.isValid()) {
                return new Target(player.getUuid(), player.getUsername(), player);
            }
            return manager.getUser(uuid)
                    .map(record -> new Target(uuid, displayName(record), null))
                    .orElse(new Target(uuid, uuid.toString(), null));
        } catch (IllegalArgumentException ignored) {
        }
        return manager.findUser(target)
                .map(record -> new Target(record.getUuid(), displayName(record), null))
                .orElse(null);
    }

    private String displayName(PermissionsUserRecord record) {
        String username = record.getLastKnownUsername();
        return username == null || username.isBlank() ? record.getUuidString() : username;
    }

    private record Target(UUID uuid, String displayName, PlayerRef onlinePlayer) {
    }

    public record TargetView(UUID uuid, String displayName, boolean online, Set<String> groups) {
    }
}
