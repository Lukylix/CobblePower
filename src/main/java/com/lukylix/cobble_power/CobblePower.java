package com.lukylix.cobble_power;

import com.lukylix.cobble_power.block.ModBlocks;
import com.lukylix.cobble_power.block.blockentity.cable.EnergyCableBlockEntity;
import com.lukylix.cobble_power.block.blockentity.energizer.EnergizerBlockEntity;
import com.lukylix.cobble_power.event.ModEventHandler;
import com.lukylix.cobble_power.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.reborn.energy.api.EnergyStorage;

public class CobblePower implements ModInitializer {
    public static final String MOD_ID = "cobble_power";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {

        ModBlocks.register();
        ModScreenHandlers.register();
        ModEventHandler.register();

        EnergyStorage.SIDED.registerForBlocks(
                (world, pos, state, blockEntity, direction) -> {
                    if (blockEntity instanceof EnergyCableBlockEntity cable)
                        return cable.getEnergyStorage();
                    return null;
                },
                ModBlocks.ENERGY_CABLE
        );
        EnergyStorage.SIDED.registerForBlocks(
                (world, pos, state, blockEntity, direction) -> {
                    if (blockEntity instanceof EnergizerBlockEntity cable)
                        return cable.getEnergyStorage();
                    return null;
                },
                ModBlocks.ENERGIZER
        );
    }
}