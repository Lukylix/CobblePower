package com.lukylix.cobble_power.block.custom;

import com.lukylix.cobble_power.block.blockentity.energizer.EnergizerBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class EnergizerBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;


    public EnergizerBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public InteractionResult onUse(Player player, Level level, InteractionHand hand, BlockPos blockPos) {
        BlockEntity be = level.getBlockEntity(blockPos);
        if (player.isCrouching()) return InteractionResult.PASS;
        if (be instanceof ExtendedScreenHandlerFactory<?> factory && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(factory);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // Make the front face point toward the player
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }


    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergizerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T>
    getTicker(net.minecraft.world.level.Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null :
                (lvl, pp, st, be) -> {
                    if (be instanceof EnergizerBlockEntity gen)
                        EnergizerBlockEntity.tick(lvl, pp, st, gen);
                };
    }
}
