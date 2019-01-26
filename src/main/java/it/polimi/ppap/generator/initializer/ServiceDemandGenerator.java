package it.polimi.ppap.generator.initializer;

import peersim.core.CommonState;

import java.util.Random;

public class ServiceDemandGenerator {

    static Random random = CommonState.r;

    final int minDemand;
    final int maxDemand;

    public ServiceDemandGenerator(int minDemand, int maxDemand) {
        this.minDemand = minDemand;
        this.maxDemand = maxDemand;
    }

    public long nextDemand(){
        return minDemand + random.nextInt(maxDemand - minDemand);
    }
}
