package com.readutf.inari.test;

import co.aikar.commands.PaperCommandManager;
import com.readutf.inari.core.arena.ArenaManager;
import com.readutf.inari.core.arena.marker.impl.TileEntityScanner;
import com.readutf.inari.core.arena.meta.ArenaMeta;
import com.readutf.inari.core.arena.selection.impl.WorldEditSelectionManager;
import com.readutf.inari.core.arena.stores.schematic.SchematicArenaManager;
import com.readutf.inari.core.arena.stores.schematic.loader.impl.WorldEditLoader;
import com.readutf.inari.core.commands.ArenaCommands;
import com.readutf.inari.core.commands.EventDebugCommand;
import com.readutf.inari.core.commands.completions.GameCompletions;
import com.readutf.inari.core.commands.completions.GameMakerCommand;
import com.readutf.inari.core.event.GameEventManager;
import com.readutf.inari.core.game.GameManager;
import com.readutf.inari.core.scoreboard.ScoreboardManager;
import com.readutf.inari.test.commands.GameCommand;
import com.readutf.inari.test.games.GameStarterManager;
import com.readutf.inari.test.listeners.DemoListeners;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;

@Getter
public class InariDemo extends JavaPlugin {

    @Getter
    private static InariDemo instance;

    private ArenaManager arenaManager;
    private GameManager gameManager;
    private GameStarterManager gameStarterManager;
    private GameEventManager gameEventManager;

    public InariDemo() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // Initialize managers
        WorldEditSelectionManager worldEditSelectionManager = new WorldEditSelectionManager();
        arenaManager = new SchematicArenaManager(this, new TileEntityScanner(), new WorldEditLoader(this), new File(getDataFolder(), "schematics"));
        // this.arenaManager = new GridArenaManager(this, getDataFolder(), new TileEntityScanner());
        gameManager = new GameManager();
        gameEventManager = new GameEventManager(this, gameManager);
        gameStarterManager = new GameStarterManager(arenaManager, gameManager, new ScoreboardManager(this), gameEventManager);

        // Register completions
        PaperCommandManager paperCommandManager = new PaperCommandManager(this);
        paperCommandManager.getCommandCompletions().registerCompletion("gameids", new GameCompletions.GameIdCompletion(gameManager));
        paperCommandManager.getCommandCompletions().registerCompletion("gameplayers", new GameCompletions.GamePlayersCompletion(gameManager));
        paperCommandManager.getCommandCompletions().registerCompletion("gametypes", c -> gameStarterManager.getGameStarters());
        paperCommandManager.getCommandCompletions().registerCompletion("arena", c -> arenaManager.findAvailableArenas(arenaMeta -> true).stream().map(ArenaMeta::getName).toList());

        // Register commands
        Arrays.asList(
                new ArenaCommands(this, worldEditSelectionManager, arenaManager),
                // new DevCommand(gameManager, arenaManager, gameEventManager),
                new EventDebugCommand(gameEventManager),
                new GameCommand(arenaManager, gameStarterManager),
                new GameMakerCommand(gameManager)
        ).forEach(paperCommandManager::registerCommand);

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new DemoListeners(), this);
    }

    @Override
    public void onDisable() {
        // Shutdown managers
        arenaManager.shutdown();
        gameManager.shutdown();
    }
}
