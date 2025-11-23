package com.lukylix.cobble_power.behavior.energy;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleSidedEnergyContainer;


public interface EnergyStorageSidedComponent<B extends BlockEntity & IEnergy<B> & EnergyStorage> extends EnergyStorage, IEnergy<B> {

    SimpleSidedEnergyContainer getSidedEnergyStorage();

    B getBlockEntity();

    @Override
    default void setAmount(Direction dir, long amount) {
        setAmount(amount);
    }

    @Override
    default long getAmount(Direction dir) {
        return getAmount();
    }

    @Override
    default long getMaxExtract(Direction dir) {
        return getSidedEnergyStorage().getMaxExtract(dir);
    }

    @Override
    default long getCapacity(Direction dir) {
        return getSidedEnergyStorage().getCapacity();
    }

    @Override
    default long insert(long maxAmount, TransactionContext transaction) {

        return getSidedEnergyStorage().getSideStorage(null).insert(maxAmount, transaction);
    }

    @Override
    default long extract(long maxAmount, TransactionContext transaction) {
        return getSidedEnergyStorage().getSideStorage(null).extract(maxAmount, transaction);
    }

    @Override
    default boolean supportsInsertion() {
        return true;
    }

    @Override
    default boolean supportsExtraction() {
        return true;
    }

    @Override
    default long getAmount() {
        return getSidedEnergyStorage().getSideStorage(null).getAmount();
    }


    default void setAmount(long amount) {
        getSidedEnergyStorage().amount = amount;
    }

    @Override
    default long getCapacity() {
        return getSidedEnergyStorage().getCapacity();
    }

    default long generate(long amount) {
        long space = getCapacity() - getAmount();
        long toInsert = Math.min(space, amount);
        setAmount(toInsert);
        return toInsert;
    }

    default void saveEnergyToNbt(CompoundTag tag) {
        tag.putLong("Energy", getAmount());
    }

    default void loadEnergyFromNbt(CompoundTag tag) {
        this.setAmount(tag.getLong("Energy"));
    }
}
