package com.readutf.inari.core.logging.store;

import java.util.UUID;

@FunctionalInterface
public interface LogStoreFactory {

    LogStore createLogStore(UUID gameId);
}
