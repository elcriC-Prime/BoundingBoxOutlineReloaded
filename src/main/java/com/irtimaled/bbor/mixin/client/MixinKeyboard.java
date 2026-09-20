package com.irtimaled.bbor.mixin.client;

import com.irtimaled.bbor.client.keyboard.KeyListener;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Keyboard.class)
public class MixinKeyboard {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(at = @At("HEAD"), method = "onKey", cancellable = true)
    public void onOnKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (this.client.getWindow() != null && window == this.client.getWindow().getHandle()) {
            if (KeyListener.onKeyEvent(window, key, scancode, action, modifiers)) {
                ci.cancel();
            }
        }
    }
}
