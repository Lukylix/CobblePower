package com.lukylix.cobble_power.behavior.energy;

import com.lukylix.cobble_power.behavior.inventory.ContainerComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;


public interface EnergyItemChargerComponent<B extends BlockEntity & EnergyItemChargerComponent<B> & IEnergy<B> & EnergyStorage & ContainerComponent<B>> {


    void syncIsChargingItem(B blockEntity);

    B getBlockEntity();

    default IEnergy<B> getIEnergy() {
        return getBlockEntity();
    }

    default ContainerComponent<B> getIContainer() {
        return getBlockEntity();
    }

    void setChanged();

    default boolean getIsChargingItem() {
        return false;
    }

    default void setIsChargingItem(boolean value) {
    }

    default void tickCharging(B blockEntity) {
        ItemStack stack = getIContainer().getItem(0);
        if (stack.isEmpty()) return;
        boolean itemNeedCharging = blockEntity.itemNeedCharging();
        this.chargeItem(stack);
        // Sync charging state if changed
        if (itemNeedCharging != blockEntity.getIsChargingItem()) {
            blockEntity.setIsChargingItem(itemNeedCharging);
            blockEntity.syncIsChargingItem(blockEntity);
        }
    }


    default boolean itemNeedCharging() {
        ItemStack stack = getIContainer().getItem(0);
        if (stack.isEmpty()) return false;

        var energyItem = EnergyStorage.ITEM.find(stack, null);
        return energyItem != null && getIEnergy().getEnergyStorage().getAmount() > 0 && energyItem.getAmount() < energyItem.getCapacity();
    }

    default void chargeItem(ItemStack stack) {
        if (itemNeedCharging()) {
            var energyItem = EnergyStorage.ITEM.find(stack, null);
            long transferred = EnergyStorageUtil.move(
                    getIEnergy().getEnergyStorage(),
                    energyItem,
                    getIEnergy().getMaxExtract(null),
                    null
            );
            if (transferred > 0) setChanged();
        }
    }
}
