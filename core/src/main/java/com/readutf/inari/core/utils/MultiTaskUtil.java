package com.readutf.inari.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultiTaskUtil {

    public static <T> @Nullable List<T> collectAll(ExecutorService service, @NotNull List<Supplier<T>> tasks) {
        try {
            List<CompletableFuture<T>> futures = tasks.stream().map(task -> CompletableFuture.supplyAsync(task, service)).toList();
            CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            all.get();

            return futures.stream().map(tCompletableFuture -> tCompletableFuture.getNow(null)).toList();
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
