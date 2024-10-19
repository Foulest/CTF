package com.readutf.inari.test.games.ctf;

import com.google.common.base.Preconditions;
import com.readutf.inari.core.arena.ActiveArena;
import com.readutf.inari.core.arena.ArenaManager;
import com.readutf.inari.core.arena.exceptions.ArenaLoadException;
import com.readutf.inari.core.arena.meta.ArenaMeta;
import com.readutf.inari.core.event.GameEventManager;
import com.readutf.inari.core.game.Game;
import com.readutf.inari.core.game.GameManager;
import com.readutf.inari.core.game.exception.GameException;
import com.readutf.inari.core.game.spawning.impl.TeamBasedSpawning;
import com.readutf.inari.core.game.team.Team;
import com.readutf.inari.core.game.team.TeamColor;
import com.readutf.inari.core.scoreboard.ScoreboardManager;
import com.readutf.inari.core.utils.ThreadUtils;
import com.readutf.inari.test.InariDemo;
import com.readutf.inari.test.games.GameStarter;
import com.readutf.inari.test.games.shared.AwaitingPlayersStage;
import com.readutf.inari.test.games.sumo.SumoSpectatorSpawnFinder;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class CTFStarter implements GameStarter {

    private final ArenaManager arenaManager;
    private final GameManager gameManager;
    private final GameEventManager eventManager;
    private final ScoreboardManager scoreboardManager;

    private static final List<TeamColor> teamColors = List.of(
            TeamColor.RED,
            TeamColor.BLUE
    );

    @Override
    public CompletableFuture<Game> startGame(ArenaMeta arenaMeta,
                                             @NotNull List<List<UUID>> players) throws ArenaLoadException {
        Preconditions.checkArgument(players.size() == 2, "CTF requires at least two players to start.");

        List<Team> teams = new ArrayList<>();

        for (List<UUID> team : players) {
            teams.add(new CTFTeam("Team", teamColors.get(teams.size()), team));
        }

        CompletableFuture<Game> gameReadyFuture = new CompletableFuture<>();
        CompletableFuture<ActiveArena> arenaFuture = arenaManager.load(arenaMeta);

        arenaFuture.thenAccept(initialArena -> {
            Game startedGame =
                    Game.builder(InariDemo.getInstance(),
                                    initialArena,
                                    eventManager,
                                    scoreboardManager,
                                    teams,
                            (game, previousRound) -> new AwaitingPlayersStage(game, 2, 30),
                            (game, round) -> new CTFRound(game))
                    .setPlayerSpawnHandler(game -> new TeamBasedSpawning(game, "spawn"))
                    .setSpectatorSpawnHandler(SumoSpectatorSpawnFinder::new)
                    .build();

            ThreadUtils.ensureSync(InariDemo.getInstance(), () -> {
                try {
                    gameManager.startGame(startedGame);
                    gameReadyFuture.complete(startedGame);
                } catch (GameException ex) {
                    gameReadyFuture.completeExceptionally(ex);
                }
            });
        });
        return gameReadyFuture;
    }
}
