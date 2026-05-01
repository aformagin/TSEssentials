package com.thirdspare.modules.claims.events;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.thirdspare.modules.claims.ClaimsService;
import com.thirdspare.modules.claims.data.ClaimDefinition;
import com.hypixel.hytale.math.vector.Vector3i;

import java.util.Optional;

public final class ClaimsProtectionSystems {
    private ClaimsProtectionSystems() {
    }

    private static Query<EntityStore> playerQuery() {
        return Archetype.of(PlayerRef.getComponentType());
    }

    private static boolean denyIfProtected(ClaimsService claimsService,
                                           PlayerRef actor, Vector3i targetBlock) {
        if (actor == null || targetBlock == null || actor.getWorldUuid() == null) return false;
        if (claimsService.canModify(actor, actor.getWorldUuid(),
                targetBlock.x, targetBlock.y, targetBlock.z)) {
            return false;
        }
        Optional<ClaimDefinition> claim = claimsService.getClaimAt(
                actor.getWorldUuid(), targetBlock.x, targetBlock.y, targetBlock.z);
        String owner = claim.map(ClaimDefinition::getOwnerName).orElse("someone");
        actor.sendMessage(Message.raw("This area belongs to " + owner + ".").color("#FF6B6B"));
        return true;
    }

    public static class BreakBlockSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {
        private final ClaimsService claimsService;

        public BreakBlockSystem(ClaimsService claimsService) {
            super(BreakBlockEvent.class);
            this.claimsService = claimsService;
        }

        @Override
        public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store,
                           CommandBuffer<EntityStore> commandBuffer, BreakBlockEvent event) {
            PlayerRef actor = chunk.getComponent(index, PlayerRef.getComponentType());
            if (denyIfProtected(claimsService, actor, event.getTargetBlock())) {
                event.setCancelled(true);
            }
        }

        @Override
        public Query<EntityStore> getQuery() {
            return playerQuery();
        }
    }

    public static class DamageBlockSystem extends EntityEventSystem<EntityStore, DamageBlockEvent> {
        private final ClaimsService claimsService;

        public DamageBlockSystem(ClaimsService claimsService) {
            super(DamageBlockEvent.class);
            this.claimsService = claimsService;
        }

        @Override
        public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store,
                           CommandBuffer<EntityStore> commandBuffer, DamageBlockEvent event) {
            PlayerRef actor = chunk.getComponent(index, PlayerRef.getComponentType());
            if (denyIfProtected(claimsService, actor, event.getTargetBlock())) {
                event.setCancelled(true);
            }
        }

        @Override
        public Query<EntityStore> getQuery() {
            return playerQuery();
        }
    }

    public static class PlaceBlockSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
        private final ClaimsService claimsService;

        public PlaceBlockSystem(ClaimsService claimsService) {
            super(PlaceBlockEvent.class);
            this.claimsService = claimsService;
        }

        @Override
        public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store,
                           CommandBuffer<EntityStore> commandBuffer, PlaceBlockEvent event) {
            PlayerRef actor = chunk.getComponent(index, PlayerRef.getComponentType());
            if (denyIfProtected(claimsService, actor, event.getTargetBlock())) {
                event.setCancelled(true);
            }
        }

        @Override
        public Query<EntityStore> getQuery() {
            return playerQuery();
        }
    }

    public static class UseBlockPreSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {
        private final ClaimsService claimsService;

        public UseBlockPreSystem(ClaimsService claimsService) {
            super(UseBlockEvent.Pre.class);
            this.claimsService = claimsService;
        }

        @Override
        public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store,
                           CommandBuffer<EntityStore> commandBuffer, UseBlockEvent.Pre event) {
            PlayerRef actor = chunk.getComponent(index, PlayerRef.getComponentType());
            if (denyIfProtected(claimsService, actor, event.getTargetBlock())) {
                event.setCancelled(true);
            }
        }

        @Override
        public Query<EntityStore> getQuery() {
            return playerQuery();
        }
    }
}
