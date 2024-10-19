package com.readutf.inari.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ThreadUtils {

    public static void ensureSync(JavaPlugin javaPlugin, Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(javaPlugin, runnable);
        }
    }
}
