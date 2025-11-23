package com.lukylix.cobble_power;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Helper for modular client-side payload handling.
 */
public class ClientNetworking {

    /**
     * Registers the codec for a CustomPacketPayload type.
     */
    public static <T extends CustomPacketPayload> void registerPayloadCodec(
            T.Type<T> type,
            StreamCodec<RegistryFriendlyByteBuf, T> codec
    ) {
        PayloadTypeRegistry.playS2C().register(type, codec);
    }


    /**
     * Registers a handler for a CustomPacketPayload.
     *
     * @param type    The payload type
     * @param handler The client-side handler
     */
    public static <T extends CustomPacketPayload> void registerHandler(
            T.Type<T> type,
            ClientPayloadHandler<T> handler
    ) {
        ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
            // Run handler on main client thread
            Minecraft.getInstance().execute(() -> handler.handle(payload, Minecraft.getInstance()));
        });
    }
}
