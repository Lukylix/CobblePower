package com.lukylix.cobble_power.event;

import com.lukylix.cobble_power.block.custom.EnergizerBlock;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;

public class ModEventHandler {
    public static void register() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (level.isClientSide()) return InteractionResult.PASS;
            BlockPos blockPos = hitResult.getBlockPos();
            Block block = level.getBlockState(blockPos).getBlock();

            if (block instanceof EnergizerBlock energizerBlock)
                return energizerBlock.onUse(player, level, hand, blockPos);

            return InteractionResult.PASS;
        });
    }
}
