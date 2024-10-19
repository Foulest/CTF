package com.readutf.inari.test.games.bedwars;

import com.readutf.inari.core.game.team.Team;
import com.readutf.inari.core.game.team.TeamColor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
class BedwarsTeam extends Team {

    private final AtomicBoolean bedState;

    BedwarsTeam(String teamName, TeamColor color, List<UUID> players) {
        super(teamName, color, players);
        bedState = new AtomicBoolean(true);
    }

    boolean hasBed() {
        return bedState.get();
    }

    void setHasBed(boolean hasBed) {
        bedState.set(hasBed);
    }
}
