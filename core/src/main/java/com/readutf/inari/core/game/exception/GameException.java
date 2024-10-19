package com.readutf.inari.core.game.exception;

import java.io.Serial;

public class GameException extends Exception {

    @Serial
    private static final long serialVersionUID = -4425443289931335772L;

    private Exception exception;

    public GameException(String message) {
        super(message);
    }

    public GameException(String message, Exception exception) {
        super(message);
        this.exception = exception;
    }

    // TODO: Add readObject and writeObject methods
}
