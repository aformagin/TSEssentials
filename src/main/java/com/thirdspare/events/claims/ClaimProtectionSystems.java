package com.thirdspare.events.claims;

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
import com.thirdspare.claims.ClaimManager;
import com.thirdspare.data.claims.Claim;
import org.joml.Vector3i;

public final class ClaimProtectionSystems {
    private ClaimProtectionSystems() {
    }

    private static Query<EntityStore> playerQuery() {
        return Archetype.of(PlayerRef.getComponentType());
    }

    private static boolean denyIfProtected(ClaimManager claimManager, PlayerRef actor, Vector3i targetBlock) {
        if (actor == null || targetBlock == null || actor.getWorldUuid() == null) {
            return false;
        }
        Claim claim = claimManager.findClaimAt(
                actor.getWorldUuid().toString(),
                targetBlock.x,
                targetBlock.y,
                targetBlock.z
        );
        if (claimManager.canAccess(actor, claim)) {
            return false;
        }
        actor.sendMessage(Message.raw("This area belongs to " + claim.getOwnerName() + ".").color("#FF6B6B"));
        return true;
    }

    public static class BreakBlockSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {
        private final ClaimManager claimManager;

        public BreakBlockSystem(ClaimManager claimManager) {
            super(BreakBlockEvent.class);
            this.claimManager = claimManager;
        }

        @Override
        public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store,
                           CommandBuffer<EntityStore> commandBuffer, BreakBlockEvent event) {
            PlayerRef actor = chunk.getComponent(index, PlayerRef.getComponentType());
            if (denyIfProtected(claimManager, actor, event.getTargetBlock())) {
                event.setCancelled(true);
            }
        }

        @Override
        public Query<EntityStore> getQuery() {
            return playerQuery();
        }
    }

    public static class DamageBlockSystem extends EntityEventSystem<EntityStore, DamageBlockEvent> {
        private final ClaimManager claimManager;

        public DamageBlockSystem(ClaimManager claimManager) {
            super(DamageBlockEvent.class);
            this.claimManager = claimManager;
        }

        @Override
        public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store,
                           CommandBuffer<EntityStore> commandBuffer, DamageBlockEvent event) {
            PlayerRef actor = chunk.getComponent(index, PlayerRef.getComponentType());
            if (denyIfProtected(claimManager, actor, event.getTargetBlock())) {
                event.setCancelled(true);
            }
        }

        @Override
        public Query<EntityStore> getQuery() {
            return playerQuery();
        }
    }

    public static class PlaceBlockSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
        private final ClaimManager claimManager;

        public PlaceBlockSystem(ClaimManager claimManager) {
            super(PlaceBlockEvent.class);
            this.claimManager = claimManager;
        }

        @Override
        public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store,
                           CommandBuffer<EntityStore> commandBuffer, PlaceBlockEvent event) {
            PlayerRef actor = chunk.getComponent(index, PlayerRef.getComponentType());
            if (denyIfProtected(claimManager, actor, event.getTargetBlock())) {
                event.setCancelled(true);
            }
        }

        @Override
        public Query<EntityStore> getQuery() {
            return playerQuery();
        }
    }

    public static class UseBlockPreSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {
        private final ClaimManager claimManager;

        public UseBlockPreSystem(ClaimManager claimManager) {
            super(UseBlockEvent.Pre.class);
            this.claimManager = claimManager;
        }

        @Override
        public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store,
                           CommandBuffer<EntityStore> commandBuffer, UseBlockEvent.Pre event) {
            PlayerRef actor = chunk.getComponent(index, PlayerRef.getComponentType());
            if (denyIfProtected(claimManager, actor, event.getTargetBlock())) {
                event.setCancelled(true);
            }
        }

        @Override
        public Query<EntityStore> getQuery() {
            return playerQuery();
        }
    }
}
