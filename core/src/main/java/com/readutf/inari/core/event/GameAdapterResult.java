package com.readutf.inari.core.event;

import com.readutf.inari.core.game.Game;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@ToString
@AllArgsConstructor
public class GameAdapterResult {

    private final @Nullable Game game;
    private final @Nullable String failReason;

    public GameAdapterResult(@NotNull Game game) {
        this.game = game;
        failReason = null;
    }

    public GameAdapterResult(@NotNull String failReason) {
        this.failReason = failReason;
        game = null;
    }

    boolean isSuccessful() {
        return game != null;
    }
}
