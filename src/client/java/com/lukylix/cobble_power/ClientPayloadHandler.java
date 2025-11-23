package com.lukylix.cobble_power;

import net.minecraft.client.Minecraft;

/**
 * Functional interface for handling decoded payloads on the client.
 * T: type of the CustomPacketPayload.
 */
@FunctionalInterface
public interface ClientPayloadHandler<T> {

    /**
     * Called on the main client thread when a payload is received.
     *
     * @param payload The decoded payload
     * @param client The Minecraft client instance
     */
    void handle(T payload, Minecraft client);
}
