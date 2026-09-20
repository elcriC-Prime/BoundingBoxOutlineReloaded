package com.irtimaled.bbor.common.network;

import com.irtimaled.bbor.client.network.BBORClientNetworking;
import com.irtimaled.bbor.common.BBORCustomPayload;
import com.irtimaled.bbor.common.interop.CommonInterop;
import com.irtimaled.bbor.common.messages.AddBoundingBox;
import com.irtimaled.bbor.common.messages.InitializeClient;
import com.irtimaled.bbor.common.messages.StructureListSync;
import com.irtimaled.bbor.common.messages.SubscribeToServer;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class BBORNetworking {

    public static final CustomPayload.Id<BBORCustomPayload> INITIALIZE_ID = new CustomPayload.Id<>(Identifier.of(InitializeClient.NAME));
    public static final CustomPayload.Id<BBORCustomPayload> ADD_BOUNDING_BOX_ID = new CustomPayload.Id<>(Identifier.of(AddBoundingBox.NAME));
    public static final CustomPayload.Id<BBORCustomPayload> STRUCTURE_LIST_SYNC_ID = new CustomPayload.Id<>(Identifier.of(StructureListSync.NAME));
    public static final CustomPayload.Id<BBORCustomPayload> SUBSCRIBE_ID = new CustomPayload.Id<>(Identifier.of(SubscribeToServer.NAME));
    public static final CustomPayload.Id<BBORCustomPayload> SERVUX_STRUCTURES_ID = new CustomPayload.Id<>(Identifier.of("servux:structures"));

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RegisterPayloadHandlersEvent.class, BBORNetworking::onRegisterPayloads);
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0.0").optional();

        registrar.playBidirectional(INITIALIZE_ID, BBORCustomPayload.codec(INITIALIZE_ID.id()), BBORNetworking::handlePayload);
        registrar.playBidirectional(ADD_BOUNDING_BOX_ID, BBORCustomPayload.codec(ADD_BOUNDING_BOX_ID.id()), BBORNetworking::handlePayload);
        registrar.playBidirectional(STRUCTURE_LIST_SYNC_ID, BBORCustomPayload.codec(STRUCTURE_LIST_SYNC_ID.id()), BBORNetworking::handlePayload);
        registrar.playBidirectional(SERVUX_STRUCTURES_ID, BBORCustomPayload.codec(SERVUX_STRUCTURES_ID.id()), BBORNetworking::handlePayload);
        registrar.playBidirectional(SUBSCRIBE_ID, BBORCustomPayload.codec(SUBSCRIBE_ID.id()), BBORNetworking::handlePayload);
    }

    private static void handlePayload(BBORCustomPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow() == NetworkSide.CLIENTBOUND) {
                if (FMLEnvironment.dist == Dist.CLIENT) {
                    BBORClientNetworking.handleClient(payload, context);
                }
            } else {
                handleServer(payload, context);
            }
        });
    }

    private static void handleServer(BBORCustomPayload payload, IPayloadContext context) {
        if (payload.id().toString().equals(SubscribeToServer.NAME)) {
            if (context.player() instanceof ServerPlayerEntity player) {
                com.irtimaled.bbor.Logger.info("BBOR: Server received SubscribeToServer from player: %s", player.getName().getString());
                CommonInterop.playerSubscribed(player);
            }
        }
    }
}
