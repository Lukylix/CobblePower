package com.lukylix.cobble_power.behavior.energy;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.reborn.energy.api.EnergyStorage;

public interface IEnergy<B extends BlockEntity & IEnergy<B> & EnergyStorage> {
    default EnergyStorage getEnergyStorage() {
        return getBlockEntity();
    }

    B getBlockEntity();

    void setAmount(Direction dir, long amount);

    long getAmount(Direction dir);

    long getMaxExtract(Direction dir);

    long getCapacity(Direction dir);
}
