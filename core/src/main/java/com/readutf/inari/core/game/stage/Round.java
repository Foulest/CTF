package com.readutf.inari.core.game.stage;

import com.readutf.inari.core.game.team.Team;

public interface Round {

    void roundStart();

    void roundEnd(Team winnerTeam);

    boolean hasRoundEnded();
}
