package com.lukylix.cobble_power;


import com.lukylix.cobble_power.block.ModBlocks;
import com.lukylix.cobble_power.block.blockentity.energizer.EnergizerBlockEntity;
import com.lukylix.cobble_power.network.EnergizerSyncPayload;
import com.lukylix.cobble_power.screen.EnergizerScreen;
import com.lukylix.cobble_power.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.entity.BlockEntity;

@Environment(EnvType.CLIENT)
public class CobblePowerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientNetworking.registerPayloadCodec(EnergizerSyncPayload.TYPE, EnergizerSyncPayload.STREAM_CODEC);
        ClientNetworking.registerHandler(EnergizerSyncPayload.TYPE, this::handleGeneratorPayload);
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.ENERGY_CABLE, RenderType.cutout());
        MenuScreens.register(ModScreenHandlers.ENERGIZER, EnergizerScreen::new);
    }

    private void handleGeneratorPayload(EnergizerSyncPayload payload, Minecraft client) {
        ClientLevel level = client.level;
        if (level == null) return;

        BlockEntity be = level.getBlockEntity(payload.pos());
        if (be instanceof EnergizerBlockEntity generator) {
            generator.applyClientSync(payload.isChargingItem());
        }
    }
}
