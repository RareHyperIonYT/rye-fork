/*
 * Copyright (c) Rye Client 2024-2025.
 *
 * This file belongs to Rye Client,
 * an open-source Fabric injection client.
 * Rye GitHub: https://github.com/RyeClient/rye-v1.git
 *
 * THIS PROJECT DOES NOT HAVE A WARRANTY.
 *
 * Rye (and subsequently, its files) are all licensed under the MIT License.
 * Rye should have come with a copy of the MIT License.
 * If it did not, you may obtain a copy here:
 * MIT License: https://opensource.org/license/mit
 *
 */

package dev.thoq.event.impl;


import dev.thoq.event.Event;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class PacketSendEvent extends Event {

    private Packet<?> packet;
    private CallbackInfo callbackInfo;

    public PacketSendEvent(Packet<?> packet, CallbackInfo callbackInfo) {
        this.packet = packet;
        this.callbackInfo = callbackInfo;
    }


    public Packet<?> getPacket() {
        return packet;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

    public CallbackInfo getCallbackInfo() {
        return callbackInfo;
    }

    public void setCallbackInfo(CallbackInfo callbackInfo) {
        this.callbackInfo = callbackInfo;
    }
}
