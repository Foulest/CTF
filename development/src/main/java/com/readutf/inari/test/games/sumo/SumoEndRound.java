package com.readutf.inari.test.games.sumo;

import com.readutf.inari.core.game.Game;
import com.readutf.inari.core.game.GameEndReason;
import com.readutf.inari.core.game.stage.Round;
import com.readutf.inari.core.game.team.Team;
import com.readutf.inari.core.utils.ColorUtils;
import com.readutf.inari.core.utils.ThreadUtils;
import lombok.AllArgsConstructor;
import com.readutf.inari.test.utils.CancellableTask;
import com.readutf.inari.test.utils.Countdown;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.util.Map;

@AllArgsConstructor
public class SumoEndRound implements Round {

    private final Game game;
    private final SumoRound previousRound;
    private final Team winningTeam;

    @Override
    public void roundStart() {
        Team team1 = game.getTeams().get(0);
        Team team2 = game.getTeams().get(1);

        Team winner = winningTeam;
        Team losers = team1 == winner ? team2 : team1;

        Map<Team, Integer> teamScores = previousRound.getTeamScores();

        for (Player onlinePlayer : winner.getOnlinePlayers()) {
            onlinePlayer.showTitle(Title.title(
                    ColorUtils.color("&6Victory!"),
                    ColorUtils.color("&f%s &7- &f%s".formatted(teamScores.getOrDefault(team1, 0), teamScores.getOrDefault(team2, 0)))
            ));
        }

        for (Player onlinePlayer : losers.getOnlinePlayers()) {
            onlinePlayer.showTitle(Title.title(
                    ColorUtils.color("&6Victory!"),
                    ColorUtils.color("&f%s &7- &f%s".formatted(teamScores.getOrDefault(team1, 0), teamScores.getOrDefault(team2, 0)))
            ));
        }

        Countdown.startCountdown(game, 5, new CancellableTask<>() {
            @Override
            public void run(Integer integer) {
                if (integer == 0) {
                    ThreadUtils.ensureSync(game.getJavaPlugin(), () -> game.endGame(winningTeam, GameEndReason.ENEMIES_ELIMINATED));
                }
            }
        });
    }

    @Override
    public void roundEnd(Team winnerTeam) {
    }

    @Override
    public boolean hasRoundEnded() {
        return false;
    }
}
