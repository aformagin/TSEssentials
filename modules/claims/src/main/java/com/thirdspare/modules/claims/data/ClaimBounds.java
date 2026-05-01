package com.thirdspare.modules.claims.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class ClaimBounds {
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;

    public ClaimBounds() {
    }

    public ClaimBounds(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    public boolean overlaps(ClaimBounds other) {
        if (other == null) {
            return false;
        }
        return minX <= other.maxX && maxX >= other.minX
                && minY <= other.maxY && maxY >= other.minY
                && minZ <= other.maxZ && maxZ >= other.minZ;
    }

    public boolean isZeroVolume() {
        return minX == maxX || minY == maxY || minZ == maxZ;
    }

    public String describeBounds() {
        return "(" + minX + "," + minY + "," + minZ + ") to (" + maxX + "," + maxY + "," + maxZ + ")";
    }

    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }

    public static final BuilderCodec<ClaimBounds> CODEC = BuilderCodec.builder(ClaimBounds.class, ClaimBounds::new)
            .append(new KeyedCodec<>("MinX", Codec.INTEGER),
                    (b, v) -> b.minX = v != null ? v : 0,
                    b -> b.minX).add()
            .append(new KeyedCodec<>("MinY", Codec.INTEGER),
                    (b, v) -> b.minY = v != null ? v : 0,
                    b -> b.minY).add()
            .append(new KeyedCodec<>("MinZ", Codec.INTEGER),
                    (b, v) -> b.minZ = v != null ? v : 0,
                    b -> b.minZ).add()
            .append(new KeyedCodec<>("MaxX", Codec.INTEGER),
                    (b, v) -> b.maxX = v != null ? v : 0,
                    b -> b.maxX).add()
            .append(new KeyedCodec<>("MaxY", Codec.INTEGER),
                    (b, v) -> b.maxY = v != null ? v : 0,
                    b -> b.maxY).add()
            .append(new KeyedCodec<>("MaxZ", Codec.INTEGER),
                    (b, v) -> b.maxZ = v != null ? v : 0,
                    b -> b.maxZ).add()
            .build();
}
