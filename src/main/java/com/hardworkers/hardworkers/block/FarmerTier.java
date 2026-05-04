package com.hardworkers.hardworkers.block;

public enum FarmerTier {
    WOOD     ("wood",       30, 0),
    STONE    ("stone",      24, 200),
    IRON     ("iron",       18, 100),
    DIAMOND  ("diamond",    12,  50),
    NETHERITE("netherite",   6,  25);

    public final String id;
    /** Ticks between each crop harvest action. */
    public final int harvestInterval;
    /** Ticks between forced random-tick calls on crops in range (0 = no boost). */
    public final int growthBoostInterval;

    FarmerTier(String id, int harvestInterval, int growthBoostInterval) {
        this.id = id;
        this.harvestInterval = harvestInterval;
        this.growthBoostInterval = growthBoostInterval;
    }
}
