package it.polimi.ppap.common.random;

import peersim.core.CommonState;

public class RandomDemand {

    public static double initialDemand(double base) {
        return CommonState.r.nextDouble() * base;
    }

    public static double getVariation(double delta) {
        return (CommonState.r.nextDouble() - 0.55) * delta;
    }
}