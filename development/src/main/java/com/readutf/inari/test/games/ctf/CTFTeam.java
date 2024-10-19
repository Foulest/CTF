package com.readutf.inari.test.games.ctf;

import com.readutf.inari.core.game.team.Team;
import com.readutf.inari.core.game.team.TeamColor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
public class CTFTeam extends Team {

    private final AtomicBoolean flagAtBase;
    private int points;

    CTFTeam(String teamName, TeamColor color, List<UUID> players) {
        super(teamName, color, players);
        flagAtBase = new AtomicBoolean(true);
        points = 0;
    }
}
