package com.readutf.inari.core.game;

import com.readutf.inari.core.arena.ActiveArena;
import com.readutf.inari.core.event.GameEventManager;
import com.readutf.inari.core.event.testlistener.TestListener;
import com.readutf.inari.core.game.death.DeathListeners;
import com.readutf.inari.core.game.death.DeathManager;
import com.readutf.inari.core.game.events.GameEndEvent;
import com.readutf.inari.core.game.events.GameStartEvent;
import com.readutf.inari.core.game.exception.GameException;
import com.readutf.inari.core.game.lang.DefaultGameLang;
import com.readutf.inari.core.game.spawning.SpawnFinder;
import com.readutf.inari.core.game.spectator.SpectatorData;
import com.readutf.inari.core.game.spectator.SpectatorManager;
import com.readutf.inari.core.game.stage.Round;
import com.readutf.inari.core.game.stage.RoundCreator;
import com.readutf.inari.core.game.task.GameThread;
import com.readutf.inari.core.game.team.Team;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
@Setter
public class Game {

    private static Timer timer = new Timer();

    private final UUID gameId;
    private final JavaPlugin javaPlugin;
    private final List<Team> playerTeams;
    private final ArrayDeque<RoundCreator> stages;
    private final GameEventManager gameEventManager;
    private final DeathManager deathManager;
    private final SpectatorManager spectatorManager;
    private final GameThread gameThread;

    private SpawnFinder playerSpawnFinder;
    private @NotNull GameState gameState;
    private SpawnFinder spectatorSpawnFinder;
    private GameLang lang;
    private ActiveArena arena;
    private Round currentRound;

    protected Game(JavaPlugin javaPlugin, GameEventManager gameEventManager, ActiveArena intialArena, List<Team> playerTeams, RoundCreator... stageCreators) {
        this.gameId = UUID.randomUUID();
        this.javaPlugin = javaPlugin;
        this.arena = intialArena;
        this.playerTeams = playerTeams;
        this.gameEventManager = gameEventManager;
        this.spectatorManager = new SpectatorManager(this);
        this.lang = new DefaultGameLang();
        this.gameThread = new GameThread(this);
        this.stages = new ArrayDeque<>(Arrays.asList(stageCreators));
        this.deathManager = new DeathManager(this);
        this.gameState = GameState.WAITING;

        timer.schedule(gameThread, 0, 1);

        Arrays.asList(
                new TestListener(),
                new DeathListeners(deathManager)
        ).forEach(o -> gameEventManager.scanForListeners(this, o));
    }

    public void start() throws GameException {
        if (gameState != GameState.WAITING) throw new GameException("Game is already started");

        RoundCreator creator = stages.poll();
        if (creator == null) throw new GameException("No stages to start");
        currentRound = creator.createRound(this, null);


        for (Player alivePlayers : getOnlineAndAlivePlayers()) {
            alivePlayers.teleport(playerSpawnFinder.findSpawn(this, alivePlayers));
        }

        currentRound.roundStart();

        Bukkit.getPluginManager().callEvent(new GameStartEvent(this));
        this.gameState = GameState.ACTIVE;
    }

    public void endRound() {

        currentRound.roundEnd();

        RoundCreator creator = stages.poll();
        if (creator == null) {
            endGame(null, GameEndReason.NO_ROUNDS_LEFT);
            return;
        }

        currentRound = creator.createRound(this, currentRound);
        currentRound.roundStart();
    }

    public void endGame(Team winner, GameEndReason reason) {
        gameState = GameState.ENDED;

        GameEndEvent event = new GameEndEvent(this, winner, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        for (Player onlinePlayer : getOnlinePlayers()) {
            for (Component c : lang.getGameSummaryMessage(onlinePlayer)) {
                onlinePlayer.sendMessage(c);
            }

            SpectatorData spectatorData = spectatorManager.getSpectatorData(onlinePlayer.getUniqueId());
            if(spectatorData != null) {
                spectatorManager.revertState(onlinePlayer);
            }

        }

        currentRound.roundEnd();
        gameThread.cancel();
        spectatorManager.shutdown();
        gameEventManager.unregisterGame(this);
        GameManager.getInstance().removeGame(this);
    }

    public void messageAlive(TextComponent textComponent) {

        for (Player onlineAndAlivePlayer : getOnlineAndAlivePlayers()) {
            onlineAndAlivePlayer.sendMessage(textComponent);
        }

    }

    public void messageAlive(String message) {
        messageAlive(Component.text(message));
    }

    public void killPlayer(Player player) {
        deathManager.killPlayer(player);
    }

    public void registerListeners(Object object) {
        gameEventManager.scanForListeners(this, object);
    }

    public List<Player> getOnlineAndAlivePlayers() {
        List<Player> list = new ArrayList<>();
        for (UUID uuid : getAllPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline() || !isAlive(player)) continue;
            list.add(player);
        }
        return list;
    }

    public List<Player> getOnlinePlayers() {
        List<Player> list = new ArrayList<>();
        for (UUID uuid : getAllPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            list.add(player);
        }
        return list;
    }

    public Team getTeamById(int index) {
        return playerTeams.get(index);
    }

    public int getTeamIndex(Team team) {
        return playerTeams.indexOf(team);
    }

    public int getTeamIndex(UUID player) {
        return getTeamIndex(getTeamByPlayer(player));
    }

    public Team getTeamByPlayer(Player player) {
        return getTeamByPlayer(player.getUniqueId());
    }

    public Team getTeamByPlayer(UUID playerId) {
        for (Team playerTeam : playerTeams) {
            if (playerTeam.getPlayers().contains(playerId)) return playerTeam;
        }
        return null;
    }

    public boolean isAlive(Player player) {
        return !spectatorManager.isSpectator(player.getUniqueId());
    }

    public List<UUID> getAllPlayers() {
        List<UUID> list = new ArrayList<>();
        for (Team playerTeam : playerTeams) {
            List<UUID> players = playerTeam.getPlayers();
            list.addAll(players);
        }
        return list;
    }

    public static GameBuilder builder(JavaPlugin javaPlugin,
                                      ActiveArena intialArena,
                                      GameEventManager gameEventManager,
                                      List<Team> playerTeams,
                                      RoundCreator... stageCreators) {
        return new GameBuilder(javaPlugin, intialArena, gameEventManager, playerTeams, stageCreators);
    }
}
