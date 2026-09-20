package com.irtimaled.bbor.client.keyboard;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

class CustomKeyBinding extends KeyBinding {
    private final Key key;
    private final InputUtil.Key forgeKey;

    CustomKeyBinding(String description, String translationKey) {
        super(description, InputUtil.fromTranslationKey(translationKey).getCode(), KeyListener.Category);
        this.forgeKey = InputUtil.fromTranslationKey(translationKey);
        this.key = new Key(this.forgeKey.getCode());
    }

    @Override
    public void setBoundKey(InputUtil.Key input) {
        super.setBoundKey(input);
        if (input != null) {
            key.updateKeyCode(input.getCode());
        }
    }

    @Override
    public void setKeyModifierAndCode(net.neoforged.neoforge.client.settings.KeyModifier keyModifier, InputUtil.Key input) {
        super.setKeyModifierAndCode(keyModifier, input);
        if (input != null) {
            key.updateKeyCode(input.getCode());
        }
    }

    public Key getBBORKey() {
        return key;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof KeyBinding other)) return false;
        return getTranslationKey().equals(other.getTranslationKey());
    }

    @Override
    public int hashCode() {
        return getTranslationKey().hashCode();
    }
}
