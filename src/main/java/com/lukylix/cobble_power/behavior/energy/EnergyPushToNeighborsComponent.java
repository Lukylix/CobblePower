package com.lukylix.cobble_power.behavior.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;


public interface EnergyPushToNeighborsComponent {
    default void pushToNeighbors(EnergyStorage storage, Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            EnergyStorage target = EnergyStorage.SIDED.find(level, neighborPos, dir.getOpposite());
            if (target != null) {
                EnergyStorageUtil.move(storage, target, storage.getAmount(), null);
            }
        }
    }
}
