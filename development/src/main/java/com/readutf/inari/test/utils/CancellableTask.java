package com.readutf.inari.test.utils;

import lombok.Setter;

@Setter
public abstract class CancellableTask<T> {

    public Runnable cancelTaskRunnable;

    public abstract void run(T t);

    protected void cancel() {
        if (cancelTaskRunnable != null) {
            cancelTaskRunnable.run();
        }
    }
}
