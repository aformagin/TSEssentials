package com.thirdspare.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import java.util.UUID;

/**
 * Data model for the server spawn location
 * Stores a single global spawn point that all players can teleport to
 */
public class SpawnData {
    private String worldUUID;
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;
    private float roll;

    /**
     * Default no-arg constructor required for codec deserialization
     */
    public SpawnData() {
    }

    public SpawnData(String worldUUID, double x, double y, double z, float pitch, float yaw, float roll) {
        this.worldUUID = worldUUID;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }

    public UUID getWorldUUID() {
        return UUID.fromString(worldUUID);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getRoll() {
        return roll;
    }

    /**
     * Codec for serializing/deserializing SpawnData to JSON
     */
    public static final BuilderCodec<SpawnData> CODEC = BuilderCodec.builder(SpawnData.class, SpawnData::new)
            .append(new KeyedCodec<>("WorldUUID", Codec.STRING),
                    (data, val) -> data.worldUUID = val,
                    data -> data.worldUUID).add()
            .append(new KeyedCodec<>("X", Codec.DOUBLE),
                    (data, val) -> data.x = val,
                    data -> data.x).add()
            .append(new KeyedCodec<>("Y", Codec.DOUBLE),
                    (data, val) -> data.y = val,
                    data -> data.y).add()
            .append(new KeyedCodec<>("Z", Codec.DOUBLE),
                    (data, val) -> data.z = val,
                    data -> data.z).add()
            .append(new KeyedCodec<>("Pitch", Codec.FLOAT),
                    (data, val) -> data.pitch = val,
                    data -> data.pitch).add()
            .append(new KeyedCodec<>("Yaw", Codec.FLOAT),
                    (data, val) -> data.yaw = val,
                    data -> data.yaw).add()
            .append(new KeyedCodec<>("Roll", Codec.FLOAT),
                    (data, val) -> data.roll = val,
                    data -> data.roll).add()
            .build();
}