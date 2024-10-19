package com.readutf.inari.test.games;

import com.readutf.inari.core.arena.ArenaManager;
import com.readutf.inari.core.event.GameEventManager;
import com.readutf.inari.core.game.GameManager;
import com.readutf.inari.core.scoreboard.ScoreboardManager;
import com.readutf.inari.test.games.bedwars.BedwarsStarter;
import com.readutf.inari.test.games.ctf.CTFStarter;
import com.readutf.inari.test.games.miniwalls.MiniWallsStarter;
import com.readutf.inari.test.games.sumo.SumoGameStarter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameStarterManager {

    private final Map<String, GameStarter> gameStarters;

    public GameStarterManager(ArenaManager arenaManager, GameManager gameManager, ScoreboardManager scoreboardManager, GameEventManager eventManager) {
        gameStarters = Map.of(
                "sumo", new SumoGameStarter(arenaManager, gameManager, eventManager, scoreboardManager),
                "bedwars", new BedwarsStarter(arenaManager, gameManager, eventManager, scoreboardManager),
                "miniwalls", new MiniWallsStarter(arenaManager, gameManager, eventManager, scoreboardManager),
                "ctf", new CTFStarter(arenaManager, gameManager, eventManager, scoreboardManager)
        );
    }

    public GameStarter getStarter(String gameName) {
        return gameStarters.get(gameName);
    }

    public List<String> getGameStarters() {
        return new ArrayList<>(gameStarters.keySet());
    }
}
