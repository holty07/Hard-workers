package com.hardworkers.hardworkers;

import net.neoforged.neoforge.common.ModConfigSpec;

public class HardWorkersConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue LUMBERJACK_SEARCH_RADIUS;
    public static final ModConfigSpec.IntValue LUMBERJACK_CHOP_INTERVAL;

    static {
        BUILDER.comment("Lumberjack Settings").push("lumberjack");

        LUMBERJACK_SEARCH_RADIUS = BUILDER
            .comment("Radius in blocks the lumberjack searches for trees around its home block")
            .defineInRange("searchRadius", 16, 4, 64);

        LUMBERJACK_CHOP_INTERVAL = BUILDER
            .comment("Ticks between each log broken (20 ticks = 1 second)")
            .defineInRange("chopInterval", 20, 5, 100);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
