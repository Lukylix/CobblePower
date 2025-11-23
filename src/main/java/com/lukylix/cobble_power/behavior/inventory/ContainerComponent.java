package com.lukylix.cobble_power.behavior.inventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public interface ContainerComponent<B extends BlockEntity & ContainerComponent<B>> extends Container {

    NonNullList<ItemStack> getInventory();

    B getBlockEntity();

    // --- Container methods ---
    @Override
    default boolean isEmpty() {
        return getInventory().stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    default @NotNull ItemStack getItem(int slot) {
        return getInventory().get(slot);
    }

    @Override
    default @NotNull ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(getInventory(), slot, amount);
    }

    @Override
    default boolean stillValid(Player player) {
        return player.distanceToSqr(
                getBlockEntity().getBlockPos().getX() + 0.5,
                getBlockEntity().getBlockPos().getY() + 0.5,
                getBlockEntity().getBlockPos().getZ() + 0.5
        ) <= 64;
    }

    @Override
    default @NotNull ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(getInventory(), slot);
    }

    @Override
    default void setItem(int slot, ItemStack stack) {
        getInventory().set(slot, stack);
    }

    @Override
    default void clearContent() {
        getInventory().clear();
    }

    @Override
    default int getContainerSize() {
        return getInventory().size();
    }

    // --- NBT helpers ---
    default void saveInventoryToNbt(CompoundTag tag, HolderLookup.Provider provider) {
        ContainerHelper.saveAllItems(tag, getInventory(), provider);
    }

    default void loadInventoryFromNbt(CompoundTag tag, HolderLookup.Provider provider) {
        ContainerHelper.loadAllItems(tag, getInventory(), provider);
    }
}
