package com.lukylix.cobble_power.behavior.energy;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;


public interface EnergyStorageComponent<B extends BlockEntity & IEnergy<B> & EnergyStorage> extends EnergyStorage, IEnergy<B> {

    default void setAmount(Direction dir, long amount) {
        setAmount(amount);
    }

    SimpleEnergyStorage getSimpleEnergyStorage();


    @Override
    default long insert(long maxAmount, TransactionContext transaction) {
        return getSimpleEnergyStorage().insert(maxAmount, transaction);
    }

    @Override
    default long extract(long maxAmount, TransactionContext transaction) {
        return getSimpleEnergyStorage().extract(maxAmount, transaction);
    }

    @Override
    default boolean supportsInsertion() {
        return getSimpleEnergyStorage().supportsInsertion();
    }

    @Override
    default boolean supportsExtraction() {
        return getSimpleEnergyStorage().supportsExtraction();
    }

    @Override
    default long getAmount() {
        return getSimpleEnergyStorage().getAmount();
    }

    default void setAmount(long amount) {
        getSimpleEnergyStorage().amount = amount;
    }

    @Override
    default long getCapacity() {
        return getSimpleEnergyStorage().getCapacity();
    }

    default long generate(long amount) {
        long space = getCapacity() - getAmount();
        long toAdd = Math.min(space, amount);
        getSimpleEnergyStorage().amount += toAdd;
        return toAdd;
    }

    default long getMaxExtract() {
        return getSimpleEnergyStorage().maxExtract;
    }

    default long getMaxInsert() {
        return getSimpleEnergyStorage().maxInsert;
    }

    default void saveEnergyToNbt(CompoundTag tag) {
        tag.putLong("Energy", getAmount());
    }

    default void loadEnergyFromNbt(CompoundTag tag) {
        this.setAmount(tag.getLong("Energy"));
    }

    default long getAmount(Direction dir) {
        return getAmount();
    }

    default long getMaxExtract(Direction dir) {
        return getMaxExtract();
    }

    default long getCapacity(Direction dir) {
        return getCapacity();
    }
}
