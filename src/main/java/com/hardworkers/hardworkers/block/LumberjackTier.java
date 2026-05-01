package com.hardworkers.hardworkers.block;

public enum LumberjackTier {

    WOOD    ("wood",      40),
    STONE   ("stone",     30),
    IRON    ("iron",      20),
    DIAMOND ("diamond",   12),
    NETHERITE("netherite", 6);

    /** Used in block/item registry names, e.g. "lumberjack_wood". */
    public final String id;
    /** Ticks between breaking each log. Lower = faster. */
    public final int chopInterval;

    LumberjackTier(String id, int chopInterval) {
        this.id = id;
        this.chopInterval = chopInterval;
    }
}
