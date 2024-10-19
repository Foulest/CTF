package com.readutf.inari.core.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.InvalidCommandArgument;
import com.readutf.inari.core.game.GameManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class GameCompletions {

    @RequiredArgsConstructor
    public static class GameIdCompletion implements CommandCompletions.CommandCompletionHandler<BukkitCommandCompletionContext> {

        private final GameManager gameManager;

        /**
         * Get a list of game ids.
         *
         * @param context The context of the command.
         * @return A list of game ids.
         * @throws InvalidCommandArgument If the command argument is invalid.
         */
        @Override
        public Collection<String> getCompletions(BukkitCommandCompletionContext context) {
            List<String> gameIds = gameManager.getGames().stream().map(game -> game.getGameId().toString()).toList();
            return getShortestUnique(gameIds);
        }

        static List<String> getShortestUnique(@NotNull List<String> list) {
            int currentLength = 0;

            for (String s1 : list) {
                System.out.println(s1);

                for (int i = 1; i < s1.length(); i++) {
                    String substring = s1.substring(0, i);

                    if (list.stream().noneMatch(s -> !s.equalsIgnoreCase(s1) && s.startsWith(substring))) {
                        currentLength = i;
                        break;
                    }
                }
            }

            int finalCurrentLength = currentLength;
            return list.stream().map(s -> s.substring(0, finalCurrentLength)).toList();
        }
    }

    @RequiredArgsConstructor
    public static class GamePlayersCompletion implements CommandCompletions.CommandCompletionHandler<BukkitCommandCompletionContext> {

        private final GameManager gameManager;

        /**
         * Get a list of players.
         *
         * @param context The context of the command.
         * @return A list of players in all games.
         * @throws InvalidCommandArgument If the command argument is invalid.
         */
        @Override
        public Collection<String> getCompletions(BukkitCommandCompletionContext context) {
            return gameManager.getGames().stream().flatMap(game -> game.getOnlinePlayers().stream().map(Player::getName)).toList();
        }
    }
}
