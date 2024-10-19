package com.readutf.inari.core.arena.exceptions;

import lombok.Getter;

import java.io.Serial;

@Getter
public class ArenaLoadException extends Exception {

    @Serial
    private static final long serialVersionUID = -6794520746857401032L;

    private final Exception exception;

    public ArenaLoadException(String message, Exception exception) {
        super(message);
        this.exception = exception;
    }

    @Override
    public void printStackTrace() {
        if (exception != null) {
            exception.printStackTrace();
        } else {
            super.printStackTrace();
        }
    }

    // TODO: Add readObject and writeObject methods
}
