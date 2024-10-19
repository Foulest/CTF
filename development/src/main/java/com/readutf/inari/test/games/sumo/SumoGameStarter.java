package com.readutf.inari.test.games.sumo;

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
import com.readutf.inari.core.logging.Logger;
import com.readutf.inari.core.logging.LoggerFactory;
import com.readutf.inari.core.scoreboard.ScoreboardManager;
import com.readutf.inari.core.utils.ThreadUtils;
import lombok.AllArgsConstructor;
import com.readutf.inari.test.InariDemo;
import com.readutf.inari.test.games.GameStarter;
import com.readutf.inari.test.games.shared.AwaitingPlayersStage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public class SumoGameStarter implements GameStarter {

    private static final Logger logger = LoggerFactory.getLogger(SumoGameStarter.class);

    private final ArenaManager arenaManager;
    private final GameManager gameManager;
    private final GameEventManager eventManager;
    private final ScoreboardManager scoreboardManager;

    @Override
    public CompletableFuture<Game> startGame(ArenaMeta arenaMeta,
                                             @NotNull List<List<UUID>> playerTeams) throws ArenaLoadException {
        logger.info("Starting sumo game...");

        List<Team> teams = new ArrayList<>();

        for (int i = 0; i < playerTeams.size(); i++) {
            List<UUID> players = playerTeams.get(i);
            teams.add(new Team("Team " + i, TeamColor.values()[i], players));
        }

        CompletableFuture<Game> future = new CompletableFuture<>();

        logger.info("Finding arena...");

        CompletableFuture<ActiveArena> arenaFuture = arenaManager.load(arenaMeta);

        arenaFuture.thenAccept(activeArena -> {
            logger.info("Loaded arena: " + activeArena.getArenaMeta().getName());

            Game createdMatch = Game.builder(InariDemo.getInstance(), activeArena, eventManager, scoreboardManager, teams,
                            (game, previousRound) -> new AwaitingPlayersStage(game, 2, 60),
                            (game, previousRound) -> new SumoRound(game, null),
                            (game, previousRound) -> new SumoRound(game, (SumoRound) previousRound),
                            (game, previousRound) -> new SumoRound(game, (SumoRound) previousRound),
                            (game, previousRound) -> new SumoRound(game, (SumoRound) previousRound),
                            (game, previousRound) -> new SumoRound(game, (SumoRound) previousRound))
                    .setPlayerSpawnHandler(game -> new TeamBasedSpawning(game, "spawn"))
                    .setSpectatorSpawnHandler(SumoSpectatorSpawnFinder::new)
                    .build();

            ThreadUtils.ensureSync(InariDemo.getInstance(), () -> {
                try {
                    gameManager.startGame(createdMatch);
                    future.complete(createdMatch);
                } catch (GameException ex) {
                    future.completeExceptionally(ex);
                }
            });
        });
        return future;
    }
}
