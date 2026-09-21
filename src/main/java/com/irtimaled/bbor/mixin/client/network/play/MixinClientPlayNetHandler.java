package com.irtimaled.bbor.mixin.client.network.play;

import com.irtimaled.bbor.client.events.GameJoin;
import com.irtimaled.bbor.client.interop.ClientInterop;
import com.irtimaled.bbor.common.EventBus;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetHandler {

    @Inject(method = "onUnloadChunk", at = @At("RETURN"))
    private void onChunkUnload(UnloadChunkS2CPacket packet, CallbackInfo ci) {
        ClientInterop.unloadChunk(packet.pos().x, packet.pos().z);
    }

//    @Inject(method = "onSynchronizeTags", at = @At("RETURN"))
//    private void onSynchronizeTags(SynchronizeTagsS2CPacket packet, CallbackInfo ci) {
//        CommonInterop.loadWorldStructures(this.world);
//    }

    @Inject(method = "onGameJoin", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;<init>(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ClientPlayNetworkHandler;)V"))
    private void onGameJoin(CallbackInfo ci) {
        EventBus.publish(new GameJoin((ClientPlayNetworkHandler) (Object) this));
    }

    @Inject(method = "sendChatCommand", at = @At("HEAD"), cancellable = true)
    private void interceptSendCommand(String command, CallbackInfo ci) {
        if (ClientInterop.interceptCommandUsage(command)) {
            ci.cancel();
        }
    }
}
