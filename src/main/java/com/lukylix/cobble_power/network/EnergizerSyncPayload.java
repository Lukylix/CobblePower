package com.lukylix.cobble_power.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record EnergizerSyncPayload(BlockPos pos, boolean isChargingItem) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("cobble_power", "energizer_sync");


    public static final Type<EnergizerSyncPayload> TYPE =
            new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, EnergizerSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, EnergizerSyncPayload::pos,
                    ByteBufCodecs.BOOL, EnergizerSyncPayload::isChargingItem,
                    EnergizerSyncPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
