package com.readutf.inari.core.logging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LoggerFactory {

    @Contract("_ -> new")
    public static @NotNull Logger getLogger(@NotNull Class<?> clazz) {
        return new GenericLogger(org.slf4j.LoggerFactory.getLogger("Inari/" + clazz.getSimpleName()));
    }
}
