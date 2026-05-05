package com.hardworkers.hardworkers.block;

public enum WarehouseTier {
    WOOD("wood",      4, 0.25),
    STONE("stone",    8, 0.28),
    IRON("iron",     12, 0.32),
    DIAMOND("diamond", 18, 0.37),
    NETHERITE("netherite", 27, 0.43);

    public final String id;
    public final int stacksPerTrip;
    public final double moveSpeed;

    WarehouseTier(String id, int stacksPerTrip, double moveSpeed) {
        this.id = id;
        this.stacksPerTrip = stacksPerTrip;
        this.moveSpeed = moveSpeed;
    }
}
