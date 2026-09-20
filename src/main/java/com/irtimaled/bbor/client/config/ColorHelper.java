package com.irtimaled.bbor.client.config;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ColorHelper {
    private static final Map<HexColor, Color> colorMap = new ConcurrentHashMap<>();

    private static Color getColor(HexColor value) {
        if (value == null) return Color.WHITE;
        Color color = colorMap.computeIfAbsent(value, ColorHelper::decodeColor);
        return color != null ? color : Color.WHITE;
    }

    private static Color decodeColor(HexColor hexColor) {
        if (hexColor == null) return Color.WHITE;
        try {
            int color = Integer.decode(hexColor.getValue());
            return new Color(color, hexColor.hasAlpha());
        } catch (Exception ignored) {
            return Color.WHITE;
        }
    }

    public static Color getColor(Setting<HexColor> value) {
        if (value == null) return Color.WHITE;

        Color color = getColor(value.get());
        return color != null ? color : getColor(value.defaultValue);
    }
}
