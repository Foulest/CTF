package com.readutf.inari.test.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import com.readutf.inari.core.arena.ActiveArena;
import com.readutf.inari.core.arena.exceptions.ArenaLoadException;
import com.readutf.inari.core.arena.meta.ArenaMeta;
import com.readutf.inari.core.arena.stores.gridworld.GridArenaManager;
import com.readutf.inari.core.event.GameEventManager;
import com.readutf.inari.core.game.GameManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class DevCommand extends BaseCommand {

    private final GameManager gameManager;
    private final GridArenaManager arenaManager;
    private final GameEventManager eventManager;

    @SneakyThrows
    @CommandAlias("dev-sync")
    public void syncPaste(Player player) {
        ArenaMeta meta = arenaManager.findAvailableArenas(arenaMeta -> arenaMeta.getName().startsWith("sumo")).get(0);
        ActiveArena activeArena = arenaManager.spawnNewArena(meta);
    }

    @SneakyThrows
    @CommandAlias("dev-async")
    public void asyncPaste(Player player) {
        ArenaMeta meta = arenaManager.findAvailableArenas(arenaMeta -> arenaMeta.getName().startsWith("sumo")).get(0);

        CompletableFuture.runAsync(() -> {
            try {
                ActiveArena activeArena = arenaManager.spawnNewArena(meta);
            } catch (ArenaLoadException ignored) {
            }
        }).thenAccept(unused -> {
            player.sendMessage("Arena loaded");
        });
    }
}
