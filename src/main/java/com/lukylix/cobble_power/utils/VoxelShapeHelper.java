package com.lukylix.cobble_power.utils;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VoxelShapeHelper {

    /**
     * Rotates a VoxelShape defined for NORTH to match the given direction.
     */
    public static VoxelShape rotateShapeForDirection(VoxelShape shape, Direction dir) {
        if (dir == Direction.NORTH) return shape;

        VoxelShape rotated = Shapes.empty();

        // Only iterate over basic AABBs
        for (var box : shape.toAabbs()) {
            double minX = box.minX, minY = box.minY, minZ = box.minZ;
            double maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ;

            // Rotate each box individually
            VoxelShape rotatedBox;
            switch (dir) {
                case SOUTH -> rotatedBox = Shapes.box(minX, minY, 1 - maxZ, maxX, maxY, 1 - minZ);
                case EAST -> rotatedBox = Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX);
                case WEST -> rotatedBox = Shapes.box(minZ, minY, 1 - maxX, maxZ, maxY, 1 - minX);
                case UP -> rotatedBox = Shapes.box(minX, 1 - maxZ, minY, maxX, 1 - minZ, maxY);
                case DOWN -> rotatedBox = Shapes.box(minX, minZ, minY, maxX, maxZ, maxY);
                default -> rotatedBox = Shapes.empty();
            }

            rotated = Shapes.or(rotated, rotatedBox);
        }

        return rotated;
    }

}
