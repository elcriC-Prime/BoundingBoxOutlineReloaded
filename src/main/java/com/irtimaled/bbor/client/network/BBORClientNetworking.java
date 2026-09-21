package com.irtimaled.bbor.client.network;

import com.irtimaled.bbor.client.events.InitializeClientReceived;
import com.irtimaled.bbor.common.BBORCustomPayload;
import com.irtimaled.bbor.common.EventBus;
import com.irtimaled.bbor.common.messages.AddBoundingBox;
import com.irtimaled.bbor.common.messages.InitializeClient;
import com.irtimaled.bbor.common.messages.PayloadReader;
import com.irtimaled.bbor.common.messages.StructureListSync;
import com.irtimaled.bbor.common.messages.SubscribeToServer;
import com.irtimaled.bbor.common.messages.protocols.PacketSplitter;
import com.irtimaled.bbor.client.messages.servux.ServuxStructurePackets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class BBORClientNetworking {

    public static void handleClient(BBORCustomPayload payload, IPayloadContext context) {
        String idStr = payload.id().toString();
        if (idStr.equals(InitializeClient.NAME)) {
            PayloadReader reader = new PayloadReader(payload);
            InitializeClientReceived event = InitializeClient.getEvent(reader);
            com.irtimaled.bbor.Logger.info("BBOR: Client received InitializeClient (seed: %d, spawnX: %d, spawnZ: %d)", event.getSeed(), event.getSpawnX(), event.getSpawnZ());
            EventBus.publish(event);
        } else if (idStr.equals(AddBoundingBox.NAME)) {
            PayloadReader reader = new PayloadReader(payload);
            var event = AddBoundingBox.getEvent(reader);
            if (event != null) {
                com.irtimaled.bbor.Logger.info("BBOR: Client received AddBoundingBox (dim: %s, type: %s, boxes: %d)", event.getDimensionId(), event.getKey().getType(), event.getBoundingBoxes().size());
                EventBus.publish(event);
            }
        } else if (idStr.equals(StructureListSync.NAME)) {
            com.irtimaled.bbor.Logger.info("BBOR: Client received StructureListSync");
            PayloadReader reader = new PayloadReader(payload);
            StructureListSync.handleEvent(reader);
        } else if (idStr.equals("servux:structures")) {
            PacketByteBuf data = null;
            try {
                if (context.listener() instanceof ClientPlayPacketListener listener) {
                    data = PacketSplitter.receive(listener, payload);
                    if (data != null) {
                        PayloadReader reader = new PayloadReader(data);
                        ServuxStructurePackets.handleEvent(reader);
                    }
                }
            } catch (Throwable ignored) {
            } finally {
                if (data != null) {
                    data.release();
                }
            }
        }
    }
}
