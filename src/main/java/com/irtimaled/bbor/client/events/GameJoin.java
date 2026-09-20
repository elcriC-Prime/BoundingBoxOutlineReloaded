package com.irtimaled.bbor.client.events;

import net.minecraft.client.network.ClientPlayNetworkHandler;

public record GameJoin(ClientPlayNetworkHandler handler) {
}
