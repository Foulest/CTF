package com.readutf.inari.core.utils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaperUtils {

    @Getter
    private static boolean paper;

    static {
        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            paper = true;
        } catch (ClassNotFoundException ex) {
            paper = false;
        }
    }
}
