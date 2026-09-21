package com.irtimaled.bbor.client;

import net.neoforged.bus.api.IEventBus;

public class ClientProxyInitializer {
    public static void init(IEventBus modEventBus) {
        new ClientProxy().init(modEventBus);
    }
}
