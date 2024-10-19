package com.readutf.inari.core.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import com.readutf.inari.core.arena.ArenaManager;
import com.readutf.inari.core.arena.exceptions.ArenaStoreException;
import com.readutf.inari.core.arena.meta.ArenaMeta;
import com.readutf.inari.core.arena.selection.SelectionManager;
import com.readutf.inari.core.utils.ColorUtils;
import com.readutf.inari.core.utils.WorldCuboid;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@CommandAlias("arena")
@AllArgsConstructor
public class ArenaCommands extends BaseCommand {

    private final JavaPlugin javaPlugin;
    private final SelectionManager selectionManager;
    private final ArenaManager arenaManager;

    @Subcommand("create")
    public void createArena(Player player, String name) {
        WorldCuboid selection = selectionManager.getSelection(player);

        if (selection == null) {
            player.sendMessage(ColorUtils.color("&cYou must make a selection first."));
            return;
        }

        try {
            ArenaMeta arena = arenaManager.createArena(name, selection);

            player.sendMessage(ColorUtils.color("&aCreated arena " + arena.getName() + " with " + arena.getNumOfMarkers() + " markers."));
        } catch (ArenaStoreException ex) {
            ex.printStackTrace();
            player.sendMessage(ColorUtils.color("&cCould not create arena: " + ex.getLocalizedMessage()));
        }
    }

    @Subcommand("list")
    public void listArenas(@NotNull Player player) {
        List<ArenaMeta> allArenas = arenaManager.findAvailableArenas(arenaMeta -> true);
        player.sendMessage(ColorUtils.color("&aAvailable Arenas:"));

        for (ArenaMeta allArena : allArenas) {
            player.sendMessage(ColorUtils.color(" &8* &f%s &7(%s markers)".formatted(allArena.getName(), allArena.getNumOfMarkers())));
        }
    }
}
