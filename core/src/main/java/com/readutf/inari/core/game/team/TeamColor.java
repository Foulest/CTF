package com.readutf.inari.core.game.team;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.DyeColor;

@Getter
@AllArgsConstructor
public enum TeamColor {
    BLACK("&0"),
    DARK_BLUE("&1"),
    GREEN("&2"),
    CYAN("&3"),
    DARK_RED("&4"),
    PURPLE("&5"),
    ORANGE("&6"),
    LIGHT_GRAY("&7"),
    GRAY("&8"),
    BLUE("&9"),
    LIME("&a"),
    LIGHT_BLUE("&b"),
    RED("&c"),
    PINK("&d"),
    YELLOW("&e"),
    WHITE("&f");

    private final String colorCode;

    public DyeColor getDyeColor() {
        return DyeColor.valueOf(name());
    }
}
