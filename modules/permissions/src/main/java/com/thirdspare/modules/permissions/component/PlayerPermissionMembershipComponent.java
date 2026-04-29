package com.thirdspare.modules.permissions.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.thirdspare.modules.permissions.data.PermissionsUserRecord;

import java.util.HashSet;
import java.util.Set;

public class PlayerPermissionMembershipComponent implements Component<EntityStore> {
    private Set<String> groups;
    private long revision;

    public PlayerPermissionMembershipComponent() {
        this.groups = new HashSet<>();
        this.revision = 0L;
    }

    public PlayerPermissionMembershipComponent(PlayerPermissionMembershipComponent other) {
        this.groups = other != null ? new HashSet<>(other.groups) : new HashSet<>();
        this.revision = other != null ? other.revision : 0L;
    }

    @Override
    public Component<EntityStore> clone() {
        return new PlayerPermissionMembershipComponent(this);
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = PermissionsUserRecord.normalizeGroups(groups);
    }

    public long getRevision() {
        return revision;
    }

    public void setRevision(long revision) {
        this.revision = Math.max(0L, revision);
    }

    public static final BuilderCodec<PlayerPermissionMembershipComponent> CODEC =
            BuilderCodec.builder(PlayerPermissionMembershipComponent.class, PlayerPermissionMembershipComponent::new)
                    .append(new KeyedCodec<>("Groups",
                            new SetCodec<>(Codec.STRING, HashSet::new, false)),
                            PlayerPermissionMembershipComponent::setGroups,
                            PlayerPermissionMembershipComponent::getGroups).add()
                    .append(new KeyedCodec<>("Revision", Codec.LONG),
                            PlayerPermissionMembershipComponent::setRevision,
                            PlayerPermissionMembershipComponent::getRevision).add()
                    .build();
}
