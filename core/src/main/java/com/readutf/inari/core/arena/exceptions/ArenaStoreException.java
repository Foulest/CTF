package com.readutf.inari.core.arena.exceptions;

import java.io.Serial;

public class ArenaStoreException extends Exception {

    @Serial
    private static final long serialVersionUID = 2980147335269645192L;

    public ArenaStoreException(String message) {
        super(message);
    }

    // TODO: Add readObject and writeObject methods
}
