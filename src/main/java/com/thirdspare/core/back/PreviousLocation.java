package com.thirdspare.core.back;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;

import java.time.Instant;
import java.util.UUID;

public record PreviousLocation(
        UUID worldUuid,
        Vector3d position,
        Vector3f rotation,
        Instant timestamp,
        String reason
) {
}
