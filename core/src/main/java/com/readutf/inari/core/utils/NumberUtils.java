package com.readutf.inari.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NumberUtils {

    public static @Nullable Integer parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
