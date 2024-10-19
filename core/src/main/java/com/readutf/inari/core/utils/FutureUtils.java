package com.readutf.inari.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FutureUtils {

    public static <T> @NotNull CompletableFuture<List<T>> completeAll(@NotNull Collection<CompletableFuture<T>> futures) {
        AtomicInteger completedTasks = new AtomicInteger(0);
        CompletableFuture<List<T>> allDone = new CompletableFuture<>();

        for (CompletableFuture<T> future : futures) {
            future.thenAccept(t -> {
                synchronized (completedTasks) {
                    if (completedTasks.incrementAndGet() == futures.size()) {
                        allDone.complete(futures.stream().map(CompletableFuture::join).toList());
                    }
                }
            });
        }
        return allDone;
    }
}
