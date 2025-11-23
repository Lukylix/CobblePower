package com.lukylix.cobble_power.block.blockentity.energizer;

import com.lukylix.cobble_power.behavior.energy.EnergyItemChargerComponent;
import com.lukylix.cobble_power.behavior.energy.EnergyPushToNeighborsComponent;
import com.lukylix.cobble_power.behavior.energy.EnergyStorageComponent;
import com.lukylix.cobble_power.behavior.inventory.ContainerComponent;
import com.lukylix.cobble_power.block.ModBlocks;
import com.lukylix.cobble_power.network.EnergizerSyncPayload;
import com.lukylix.cobble_power.screen.EnergizerScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class EnergizerBlockEntity extends BlockEntity implements EnergyStorageComponent<EnergizerBlockEntity>, ContainerComponent<EnergizerBlockEntity>, EnergyPushToNeighborsComponent, EnergyItemChargerComponent<EnergizerBlockEntity>, ExtendedScreenHandlerFactory<BlockPos> {

    public static final long CAPACITY = 1000;
    public static final long MAX_INSERT = 0;
    public static final long MAX_EXTRACT = 10;

    private final SimpleEnergyStorage simpleEnergyStorage = new SimpleEnergyStorage(CAPACITY, MAX_INSERT, MAX_EXTRACT);


    private final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
    public boolean isCharging = false;


    public EnergizerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ENERGIZER_ENTITY, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EnergizerBlockEntity be) {
        if (level.isClientSide) return;

        // Generate energy
        be.generate(MAX_EXTRACT);

        // Tick inventory (charges items)
        be.tickCharging(be);

        // Push energy to neighbors
        be.pushToNeighbors(be.getSimpleEnergyStorage(), level, pos);
    }

    /**
     * Provide the internal inventory list
     */
    @Override
    public NonNullList<ItemStack> getInventory() {
        return inventory;
    }


    public void applyClientSync(boolean isCharging) {
        this.isCharging = isCharging;
    }


    // --- Screen Handling ---
    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("Energizer");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new EnergizerScreenHandler(syncId, inv, this.getBlockPos());
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.getBlockPos();
    }

    // --- NBT ---
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        saveEnergyToNbt(tag);
        saveInventoryToNbt(tag, provider);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        loadEnergyFromNbt(tag);
        loadInventoryFromNbt(tag, provider);
    }


    @Override
    public void syncIsChargingItem(EnergizerBlockEntity blockEntity) {
        if (blockEntity.getLevel() == null || blockEntity.getLevel().isClientSide) return;
        EnergizerSyncPayload payload = new EnergizerSyncPayload(
                blockEntity.getBlockPos(),
                blockEntity.getIsChargingItem()
        );

        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) return;
        for (ServerPlayer player : serverLevel.players()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    public EnergizerBlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public boolean getIsChargingItem() {
        return isCharging;
    }

    @Override
    public void setIsChargingItem(boolean value) {
        this.isCharging = value;
    }


    @Override
    public SimpleEnergyStorage getSimpleEnergyStorage() {
        return simpleEnergyStorage;
    }

}
