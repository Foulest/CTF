package com.readutf.inari.core.game.spectator;

import com.readutf.inari.core.game.Game;
import com.readutf.inari.core.game.events.GameRespawnEvent;
import com.readutf.inari.core.game.events.GameSpectateEvent;
import com.readutf.inari.core.game.exception.GameException;
import com.readutf.inari.core.game.spawning.SpawnFinder;
import com.readutf.inari.core.logging.GameLoggerFactory;
import com.readutf.inari.core.logging.Logger;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SpectatorManager {


    private final Game game;
    private final Logger logger;
    private final Map<UUID, SpectatorData> spectatorData;
    private final List<UUID> awaitingRejoin = new ArrayList<>();
    private final SpectatorTask spectatorTask;

    public SpectatorManager(Game game) {
        this.spectatorData = new HashMap<>();
        this.game = game;
        this.logger = game.getLoggerFactory().getLogger(SpectatorManager.class);
        (spectatorTask = new SpectatorTask(game)).runTaskTimer(game.getJavaPlugin(), 0, 1);
    }

    public boolean setSpectator(UUID playerId) {
        logger.debug("Setting player " + playerId + " to spectator");

        Player player = Bukkit.getPlayer(playerId);
        SpectatorData data = new SpectatorData(false, 0, true, List.of());

        if (player != null) {
            GameSpectateEvent event = new GameSpectateEvent(player, game, data);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return false;
        }

        spectatorData.put(playerId, data);


        if (data.isRespawn() && data.getRespawnAt() <= System.currentTimeMillis()) {
            respawnPlayer(playerId, true);
            return true;
        }

        if (player != null) {

            Location spawn;
            try {
                spawn = game.getSpectatorSpawnFinder().findSpawn(player);
            } catch (GameException e) {
                logger.error("Failed to find a spawn location for player " + player.getName(), e);
                player.sendMessage(ChatColor.RED + "Failed to find a spawn location for you.");
                return true;
            }

            applyState(data, player, spawn);

            for (Component c : game.getLang().getSpectateMessage(player, data)) {
                player.sendMessage(c);

            }
        }

        return true;
    }

    public void respawnPlayer(UUID playerId, boolean teleport) {

        if (!spectatorData.containsKey(playerId)) return;

        logger.debug("Respawning player " + playerId);

        spectatorData.remove(playerId);

        SpawnFinder spawnFinder = game.getPlayerSpawnFinder();


        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            Bukkit.getPluginManager().callEvent(new GameRespawnEvent(player, game));

            //spawn player
            if (teleport && spawnPlayer(spawnFinder, player)) return;

            //reset player state
            revertState(player);


            player.sendMessage(ChatColor.GREEN + "You have respawned!");
        } else {
            awaitingRejoin.add(playerId);
        }


    }

    private boolean spawnPlayer(SpawnFinder spawnFinder, Player player) {
        Location spawn;
        try {
            spawn = spawnFinder.findSpawn(player);
        } catch (GameException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Failed to find a spawn location for you.");
            return true;
        }
        player.teleport(spawn);
        return false;
    }

    public void revertState(Player player) {
        try {
            logger.debug("Reverting state for player " + player.getName());

            player.clearActivePotionEffects();
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setHealth(player.getMaxHealth());
            player.setGameMode(GameMode.SURVIVAL);

            for (Player alivePlayer : game.getOnlinePlayers()) {
                logger.debug("Showing player " + player.getName() + " to " + alivePlayer.getName());
                alivePlayer.showPlayer(game.getJavaPlugin(), player);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void applyState(SpectatorData data, Player player, Location spawn) {
        player.teleport(spawn);
        player.setAllowFlight(data.isCanFly());
        player.setFlying(true);
        for (Player alivePlayer : game.getOnlineAndAlivePlayers()) {
            alivePlayer.hidePlayer(game.getJavaPlugin(), player);
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false, false));
    }

    public void shutdown() {
        spectatorTask.cancel();
    }

    public Collection<UUID> getSpectators() {
        return spectatorData.keySet();
    }

    public @Nullable SpectatorData getSpectatorData(UUID playerId) {
        return spectatorData.get(playerId);
    }


    public boolean isSpectator(UUID uniqueId) {
        return spectatorData.containsKey(uniqueId);
    }
}
