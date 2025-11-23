package com.lukylix.cobble_power.screen;

import com.lukylix.cobble_power.CobblePower;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class ModScreenHandlers {

    public static ExtendedScreenHandlerType<EnergizerScreenHandler, BlockPos> ENERGIZER;

    public static void register() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CobblePower.MOD_ID, "energizer_screen_handler");

        ENERGIZER = Registry.register(
                BuiltInRegistries.MENU,
                id,
                new ExtendedScreenHandlerType<>(EnergizerScreenHandler::new, BlockPos.STREAM_CODEC)
        );
    }
}
