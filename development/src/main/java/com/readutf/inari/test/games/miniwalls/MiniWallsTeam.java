package com.readutf.inari.test.games.miniwalls;

import com.readutf.inari.core.game.team.Team;
import com.readutf.inari.core.game.team.TeamColor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
public class MiniWallsTeam extends Team {

    private final AtomicBoolean witherAlive;

    MiniWallsTeam(String teamName, TeamColor color, List<UUID> players) {
        super(teamName, color, players);
        witherAlive = new AtomicBoolean(true);
    }
}
