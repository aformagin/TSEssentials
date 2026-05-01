package com.thirdspare.data.claims;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class CuboidRegion {
    private String worldUUID;
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;

    public CuboidRegion() {
        this.worldUUID = "";
    }

    public CuboidRegion(String worldUUID, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.worldUUID = worldUUID != null ? worldUUID : "";
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public boolean contains(String worldUUID, int x, int y, int z) {
        return this.worldUUID.equals(worldUUID)
                && x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    public boolean overlaps(CuboidRegion other) {
        if (other == null || !worldUUID.equals(other.worldUUID)) {
            return false;
        }
        return minX <= other.maxX && maxX >= other.minX
                && minY <= other.maxY && maxY >= other.minY
                && minZ <= other.maxZ && maxZ >= other.minZ;
    }

    public boolean isZeroVolume() {
        return minX == maxX || minY == maxY || minZ == maxZ;
    }

    public String getWorldUUID() {
        return worldUUID;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public String describeBounds() {
        return "(" + minX + ", " + minY + ", " + minZ + ") to (" + maxX + ", " + maxY + ", " + maxZ + ")";
    }

    public static final BuilderCodec<CuboidRegion> CODEC = BuilderCodec.builder(CuboidRegion.class, CuboidRegion::new)
            .append(new KeyedCodec<>("WorldUUID", Codec.STRING),
                    (region, value) -> region.worldUUID = value != null ? value : "",
                    region -> region.worldUUID).add()
            .append(new KeyedCodec<>("MinX", Codec.INTEGER),
                    (region, value) -> region.minX = value,
                    region -> region.minX).add()
            .append(new KeyedCodec<>("MinY", Codec.INTEGER),
                    (region, value) -> region.minY = value,
                    region -> region.minY).add()
            .append(new KeyedCodec<>("MinZ", Codec.INTEGER),
                    (region, value) -> region.minZ = value,
                    region -> region.minZ).add()
            .append(new KeyedCodec<>("MaxX", Codec.INTEGER),
                    (region, value) -> region.maxX = value,
                    region -> region.maxX).add()
            .append(new KeyedCodec<>("MaxY", Codec.INTEGER),
                    (region, value) -> region.maxY = value,
                    region -> region.maxY).add()
            .append(new KeyedCodec<>("MaxZ", Codec.INTEGER),
                    (region, value) -> region.maxZ = value,
                    region -> region.maxZ).add()
            .build();
}
