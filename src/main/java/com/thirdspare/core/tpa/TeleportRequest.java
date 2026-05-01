package com.thirdspare.core.tpa;

import java.util.UUID;

public class TeleportRequest {
    private static final long EXPIRATION_SECONDS = 120;

    private final UUID requesterUUID;
    private final UUID targetUUID;
    private final TeleportRequestType type;
    private final long createdAt;
    private final String requesterName;

    public TeleportRequest(UUID requesterUUID, UUID targetUUID, TeleportRequestType type, String requesterName) {
        this.requesterUUID = requesterUUID;
        this.targetUUID = targetUUID;
        this.type = type;
        this.createdAt = System.currentTimeMillis();
        this.requesterName = requesterName;
    }

    public UUID getRequesterUUID() {
        return requesterUUID;
    }

    public UUID getTargetUUID() {
        return targetUUID;
    }

    public TeleportRequestType getType() {
        return type;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public boolean isExpired() {
        long elapsedSeconds = (System.currentTimeMillis() - createdAt) / 1000;
        return elapsedSeconds >= EXPIRATION_SECONDS;
    }

    public long getRemainingSeconds() {
        long elapsedSeconds = (System.currentTimeMillis() - createdAt) / 1000;
        long remaining = EXPIRATION_SECONDS - elapsedSeconds;
        return Math.max(0, remaining);
    }
}
