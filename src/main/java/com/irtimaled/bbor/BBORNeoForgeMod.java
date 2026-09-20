package com.irtimaled.bbor;

import com.irtimaled.bbor.client.ClientProxy;
import com.irtimaled.bbor.common.CommonProxy;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import com.irtimaled.bbor.common.network.BBORNetworking;

import java.util.concurrent.atomic.AtomicBoolean;

@Mod("bbor")
public class BBORNeoForgeMod {

    private static final AtomicBoolean serverInitialized = new AtomicBoolean(false);
    private static final AtomicBoolean clientInitialized = new AtomicBoolean(false);

    public BBORNeoForgeMod(IEventBus modEventBus) {
        BBORNetworking.register(modEventBus);
        NeoForge.EVENT_BUS.register(new BBORNeoForgeModListener());
        if (FMLEnvironment.dist == Dist.CLIENT && clientInitialized.compareAndSet(false, true)) {
            new ClientProxy().init(modEventBus);
        }
    }

    public static class BBORNeoForgeModListener {
        @SubscribeEvent
        public void onSetup(ServerAboutToStartEvent event) {
            if (FMLEnvironment.dist == Dist.DEDICATED_SERVER && serverInitialized.compareAndSet(false, true)) {
                CommonProxy.isServer = true;
                new CommonProxy().init();
            }
        }
    }
}
