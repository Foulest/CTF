package com.readutf.inari.core.logging.store;

import lombok.SneakyThrows;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlatFileLogStore implements LogStore {

    private static final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private static final String format = "[%s] [%s] %s %s\n";
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.ROOT);

    private final FileWriter writer;

    @SneakyThrows
    public FlatFileLogStore(UUID gameId, File dataDirectory) {
        writer = new FileWriter(new File(dataDirectory, gameId + ".log"), StandardCharsets.UTF_8);
    }

    @Override
    public void saveLog(Level level, long timeStamp, String message, Throwable throwable) {
        singleThreadExecutor.submit(() -> {
            try {
                writer.write(format.formatted(level, formatTime(timeStamp), message, throwable != null ? throwable.getMessage() : ""));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private static @NotNull String formatTime(long time) {
        return timeFormat.format(Instant.ofEpochMilli(time));
    }

    @Override
    public void shutdown() {
        try {
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
