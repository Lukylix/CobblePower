package com.lukylix.cobble_power.block.custom;


import com.lukylix.cobble_power.block.blockentity.cable.EnergyCableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;

import static com.lukylix.cobble_power.utils.VoxelShapeHelper.rotateShapeForDirection;

public class EnergyCableBlock extends Block implements EntityBlock {

    private static final EnumMap<Direction, BooleanProperty> CONNECTIONS = new EnumMap<>(Direction.class);
    private static final EnumMap<Direction, BooleanProperty> OUTLETS = new EnumMap<>(Direction.class);
    private static final VoxelShape CORE = Block.box(6, 6, 6, 10, 10, 10);
    private static final EnumMap<Direction, VoxelShape> ARMS = new EnumMap<>(Direction.class);

    static {
        // Initialize properties
        for (Direction dir : Direction.values()) {
            CONNECTIONS.put(dir, BooleanProperty.create(dir.getName()));
            OUTLETS.put(dir, BooleanProperty.create(dir.getName() + "_outlet"));
        }

        // Base arm along NORTH (negative Z)
        VoxelShape baseArm = Block.box(6, 6, 0, 10, 10, 6);

        // Generate rotated shapes for each direction
        for (Direction dir : Direction.values()) {
            ARMS.put(dir, rotateShapeForDirection(baseArm, dir));
        }
    }

    public EnergyCableBlock(Properties settings) {
        super(settings);

        BlockState defaultState = this.defaultBlockState();
        for (Direction dir : Direction.values()) {
            defaultState = defaultState.setValue(CONNECTIONS.get(dir), false);
            defaultState = defaultState.setValue(OUTLETS.get(dir), false);
        }
        this.registerDefaultState(defaultState);
    }

    public static BooleanProperty getPropertyForDirection(Direction dir) {
        return CONNECTIONS.get(dir);
    }

    public static BooleanProperty getOutletPropertyForDirection(Direction dir) {
        return OUTLETS.get(dir);
    }


    // --- BlockEntity creation ---
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyCableBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null :
                (lvl, pos, st, be) -> {
                    if (be instanceof EnergyCableBlockEntity cable) {
                        cable.tick(lvl, pos, st, cable);
                    }
                };
    }

    // --- Update connections when placed ---
    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, world, pos, oldState, isMoving);
        if (!world.isClientSide()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof EnergyCableBlockEntity cable) {
                cable.updateConnections();
            }
        }
    }

    // --- Update connections when neighbors change ---
    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
        if (!world.isClientSide()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof EnergyCableBlockEntity cable) {
                cable.updateConnections();
            }
        }
    }

    // --- Register blockstate properties ---
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CONNECTIONS.values().toArray(new BooleanProperty[0]));
        builder.add(OUTLETS.values().toArray(new BooleanProperty[0]));
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE;

        for (Direction dir : Direction.values()) {
            if (state.getValue(CONNECTIONS.get(dir)) || state.getValue(OUTLETS.get(dir))) {
                shape = Shapes.or(shape, ARMS.get(dir));
            }
        }

        return shape;
    }


}
