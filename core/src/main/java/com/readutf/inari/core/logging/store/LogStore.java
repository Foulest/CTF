package com.readutf.inari.core.logging.store;

import org.apache.logging.log4j.Level;

public interface LogStore {

    void saveLog(Level level, long timeStamp, String message, Throwable throwable);

    void shutdown();
}
