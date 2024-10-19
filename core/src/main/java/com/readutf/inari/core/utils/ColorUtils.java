package com.readutf.inari.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColorUtils {

    public static @NotNull Component color(String str) {
        return LegacyComponentSerializer.legacy('&').deserialize(str);
    }
}
