package com.lukylix.cobble_power.block.blockentity.cable;

import com.lukylix.cobble_power.behavior.energy.EnergyStorageSidedComponent;
import com.lukylix.cobble_power.block.ModBlocks;
import com.lukylix.cobble_power.block.custom.EnergyCableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleSidedEnergyContainer;

import java.util.List;

public class EnergyCableBlockEntity extends BlockEntity implements BlockEntityTicker<EnergyCableBlockEntity>, EnergyStorageSidedComponent<EnergyCableBlockEntity> {

    public static final long CAPACITY = 1000;
    private final CableType cableType = CableType.IRON; // default
    long lastTick = 0;
    private boolean ioBlocked = false;
    private int blockedSides = 0;
    // --- Energy Container ---
    final SimpleSidedEnergyContainer energyContainer = new SimpleSidedEnergyContainer() {
        @Override
        public long getCapacity() {
            return CAPACITY;
        }

        @Override
        public long getMaxInsert(Direction side) {
            if (ioBlocked || (side != null && (blockedSides & (1 << side.ordinal())) != 0)) return 0;
            return cableType.getTransferRate();
        }

        @Override
        public long getMaxExtract(Direction side) {
            if (ioBlocked || (side != null && (blockedSides & (1 << side.ordinal())) != 0)) return 0;
            return cableType.getTransferRate();
        }
    };


    public EnergyCableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ENERGY_CABLE_ENTITY, pos, state);
    }

    public void setIoBlocked(boolean value) {
        this.ioBlocked = value;
    }

    private EnergyStorage getAdjacentEnergy(Direction dir) {
        if (level == null) return null;
        BlockEntity be = level.getBlockEntity(worldPosition.relative(dir));
        if (be == null) return null;
        return EnergyStorage.SIDED.find(level, worldPosition.relative(dir), dir.getOpposite());
    }

    private boolean isAdjacentFaceFull(Direction dir) {
        if (level == null) return false;
        BlockPos pos = worldPosition.relative(dir);
        if (!level.isLoaded(pos)) return false;
        BlockState state = level.getBlockState(pos);
        return state.isFaceSturdy(level, pos, dir.getOpposite());
    }

    public void collectTargets(List<CableTickManager.OfferedEnergyStorage> out) {
        for (Direction dir : Direction.values()) {
            EnergyStorage storage = getAdjacentEnergy(dir);
            if (storage != null && storage != this.energyContainer) {
                out.add(new CableTickManager.OfferedEnergyStorage(this, dir, storage));
            }
        }
    }

    public void blockSide(Direction direction) {
        blockedSides |= 1 << direction.ordinal();
    }

    public CableType getCableType() {
        return cableType;
    }


    @Override
    public void tick(Level world, BlockPos pos, BlockState state, EnergyCableBlockEntity be) {
        if (world.isClientSide()) return;
        CableTickManager.handleCableTick(this);
    }

    // --- NBT Sync ---
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    @Nullable
    public BlockEntity getAdjacentBlockEntity(Direction direction) {
        if (level == null) return null;
        return level.getBlockEntity(worldPosition.relative(direction));
    }

    @Override
    public SimpleSidedEnergyContainer getSidedEnergyStorage() {
        return energyContainer;
    }

    @Override
    public EnergyCableBlockEntity getBlockEntity() {
        return this;
    }


    public void updateConnections() {
        if (level == null) return;

        BlockState state = getBlockState();
        boolean changed = false;

        // Iterate over all six directions
        for (Direction dir : Direction.values()) {
            BooleanProperty propCableDirection = EnergyCableBlock.getPropertyForDirection(dir);
            BooleanProperty propOutletDirection = EnergyCableBlock.getOutletPropertyForDirection(dir);
            EnergyStorage adjacentEnergy = getAdjacentEnergy(dir);
            boolean isFullFace = isAdjacentFaceFull(dir);

            boolean shouldConnect = adjacentEnergy != null;
            boolean currentCableDirection = state.getValue(propCableDirection);
            boolean shouldOutlet = shouldConnect && isFullFace;
            boolean currentShouldOutlet = state.getValue(propOutletDirection);
            boolean shouldConnectCable = shouldConnect && !shouldOutlet;
            if (currentShouldOutlet != shouldOutlet) {
                state = state.setValue(propOutletDirection, shouldOutlet);
                changed = true;
            }
            if ((currentCableDirection != shouldConnectCable)) {
                state = state.setValue(propCableDirection, shouldConnectCable);
                changed = true;
            }
        }

        // Only update the block if something changed to reduce unnecessary updates
        if (changed) {
            level.setBlock(worldPosition, state, 3); // flags 3 = block update + notify neighbors
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        saveEnergyToNbt(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        loadEnergyFromNbt(tag);
    }
}