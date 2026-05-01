package com.hardworkers.hardworkers.block;

public enum MinerTier {
    WOOD     ("wood",       40),
    STONE    ("stone",      30),
    IRON     ("iron",       20),
    DIAMOND  ("diamond",    12),
    NETHERITE("netherite",   6);

    public final String id;
    public final int mineInterval;

    MinerTier(String id, int mineInterval) {
        this.id = id;
        this.mineInterval = mineInterval;
    }
}
