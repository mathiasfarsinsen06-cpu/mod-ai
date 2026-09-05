package com.mathiasfarsinsen.modai.city;

/**
 * A simple engine-agnostic 3D integer coordinate, mirroring Minecraft's
 * {@code BlockPos} without depending on it. Used to store a city's center
 * point (e.g. its bell/meeting point) for teleportation and reporting.
 */
public final class Position {

    private final int x;
    private final int y;
    private final int z;

    public Position(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
