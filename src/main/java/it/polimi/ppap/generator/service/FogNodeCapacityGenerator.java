package it.polimi.ppap.generator.service;

import peersim.core.CommonState;

import java.util.Random;

public class FogNodeCapacityGenerator {

    static Random random = CommonState.r;

    final int baseCapacity;
    final short maxCapacityMultiplier;

    public FogNodeCapacityGenerator(int baseCapacity, short maxCapacityMultiplier) {
        this.baseCapacity = baseCapacity;
        this.maxCapacityMultiplier = maxCapacityMultiplier;
    }

    public long nextCapacity(){
        return baseCapacity * (1 + random.nextInt(maxCapacityMultiplier));
    }
}
