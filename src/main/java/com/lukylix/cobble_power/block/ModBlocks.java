package com.lukylix.cobble_power.block;

import com.lukylix.cobble_power.block.blockentity.cable.EnergyCableBlockEntity;
import com.lukylix.cobble_power.block.blockentity.energizer.EnergizerBlockEntity;
import com.lukylix.cobble_power.block.custom.EnergizerBlock;
import com.lukylix.cobble_power.block.custom.EnergyCableBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Objects;

public class ModBlocks {

    public static final String MODID = "cobble_power";

    // --- Creative Tab ---
    public static final ResourceKey<CreativeModeTab> COBBLE_POWER_TAB_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, id("cobble_power_tab"));
    // --- Blocks ---
    public static Block ENERGIZER;
    public static Block ENERGY_CABLE;

    // --- Block Entities ---
    public static BlockEntityType<EnergizerBlockEntity> ENERGIZER_ENTITY;
    public static BlockEntityType<EnergyCableBlockEntity> ENERGY_CABLE_ENTITY;
    public static CreativeModeTab COBBLE_POWER_TAB;
    private static Block lastRegisteredItem = null;

    public static void register() {
        // Initialize blocks first
        ENERGIZER = new EnergizerBlock(BlockBehaviour.Properties.of()
                .strength(3.0f, 5.0f)
                .noOcclusion()
                .sound(SoundType.METAL));

        ENERGY_CABLE = new EnergyCableBlock(BlockBehaviour.Properties.of()
                .strength(0.1f, 3.0f)
                .noOcclusion()
                .sound(SoundType.METAL));

        // Create the creative tab after blocks exist
        COBBLE_POWER_TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.literal("CobblePower"))
                .icon(() -> new ItemStack(ENERGIZER))
                .build();

        // Register Creative Tab
        registerCreativeTab(COBBLE_POWER_TAB_KEY, COBBLE_POWER_TAB);

        // Register blocks/items
        registerBlockWithItem("energizer", ENERGIZER);
        registerBlockWithItem("energy_cable", ENERGY_CABLE);

        // Register block entities
        ENERGIZER_ENTITY = registerBlockEntity("energizer_entity", EnergizerBlockEntity::new, ENERGIZER);
        ENERGY_CABLE_ENTITY = registerBlockEntity("energy_cable_entity", EnergyCableBlockEntity::new, ENERGY_CABLE);
    }


    // --- Helper Methods ---
    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private static void registerCreativeTab(ResourceKey<CreativeModeTab> key, CreativeModeTab tab) {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key.location(), tab);
    }

    private static void registerBlockWithItem(String name, Block block) {
        Registry.register(BuiltInRegistries.BLOCK, id(name), block);
        Registry.register(BuiltInRegistries.ITEM, id(name), new BlockItem(block, new Item.Properties()));

        ItemGroupEvents.modifyEntriesEvent(COBBLE_POWER_TAB_KEY).register(entries -> {
            entries.addAfter(
                    Objects.requireNonNullElse(lastRegisteredItem, Items.STONE),
                    block
            );
            lastRegisteredItem = block;
        });
    }

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(
            String name,
            BlockEntityType.BlockEntitySupplier<T> factory,
            Block... blocks
    ) {
        BlockEntityType<T> type = BlockEntityType.Builder.of(factory, blocks).build(null);
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id(name), type);
    }
}
