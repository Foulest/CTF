package com.readutf.inari.test.games.bedwars.generator;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.util.List;

@Getter
@AllArgsConstructor
public final class GeneratorLevel {

    private final List<GeneratorItem> items;
    private final Duration upgradeTime;
}
