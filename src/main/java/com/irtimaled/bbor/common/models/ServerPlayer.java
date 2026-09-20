package com.irtimaled.bbor.common.models;

import com.irtimaled.bbor.common.messages.PayloadBuilder;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Consumer;

public class ServerPlayer {
    private final DimensionId dimensionId;
    private final Consumer<Packet<?>> packetConsumer;
    private final ServerPlayerEntity player;

    public ServerPlayer(ServerPlayerEntity player) {
        this.player = player;
        this.dimensionId = DimensionId.from(player.getEntityWorld().getRegistryKey());
        this.packetConsumer = player.networkHandler::send;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    public DimensionId getDimensionId() {
        return dimensionId;
    }

    public void sendPacket(PayloadBuilder payloadBuilder) {
        try {
            packetConsumer.accept(payloadBuilder.build());
        } catch (Throwable t) {
            System.err.println("[BBOR] Failed to send packet: " + t.getMessage());
        }
    }
}
