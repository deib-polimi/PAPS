package it.polimi.ppap.generators;

import peersim.core.CommonState;

import java.util.Random;

public class UniformFogNodeCapacityGenerator {

    static Random random = CommonState.r;

    final int minCapacity;
    final int baseCapacity;
    final short maxCapacityMultiplier;

    public UniformFogNodeCapacityGenerator(int minCapacity, int baseCapacity, short maxCapacityMultiplier) {
        this.minCapacity = minCapacity;
        this.baseCapacity = baseCapacity;
        this.maxCapacityMultiplier = maxCapacityMultiplier;
    }

    public long nextCapacity(){
        return minCapacity + baseCapacity * random.nextInt(maxCapacityMultiplier);
    }
}
