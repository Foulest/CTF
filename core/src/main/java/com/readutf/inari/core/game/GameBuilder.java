package com.readutf.inari.core.game;

import com.google.common.base.Preconditions;
import com.readutf.inari.core.arena.ActiveArena;
import com.readutf.inari.core.event.GameEventManager;
import com.readutf.inari.core.game.spawning.SpawnFinderFactory;
import com.readutf.inari.core.game.stage.RoundCreator;
import com.readutf.inari.core.game.team.Team;
import com.readutf.inari.core.scoreboard.ScoreboardManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GameBuilder {

    private final Game game;

    GameBuilder(JavaPlugin javaPlugin,
                ActiveArena initialArena,
                GameEventManager gameEventManager,
                ScoreboardManager scoreboardManager,
                List<Team> playerTeams,
                RoundCreator... stageCreators) {
        game = new Game(javaPlugin, gameEventManager, scoreboardManager, initialArena, playerTeams, stageCreators);
    }

    public GameBuilder(@NotNull GameCreator gameCreator) {
        game = gameCreator.create();
    }

    public GameBuilder setPlayerSpawnHandler(@NotNull SpawnFinderFactory spawnFinderFactory) {
        game.setPlayerSpawnFinder(spawnFinderFactory.create(game));
        return this;
    }

    public GameBuilder setSpectatorSpawnHandler(@NotNull SpawnFinderFactory spawnFinderFactory) {
        game.setSpectatorSpawnFinder(spawnFinderFactory.create(game));
        return this;
    }

    public Game build() {
        Preconditions.checkArgument(game.getPlayerSpawnFinder() != null, "Player spawn finder cannot be null");
        Preconditions.checkArgument(game.getSpectatorSpawnFinder() != null, "Spectator spawn finder cannot be null");
        return game;
    }
}
