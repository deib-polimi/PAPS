package it.polimi.ppap.random;

import peersim.core.CommonState;

import java.util.Random;

public class UniformServiceMemoryRequirementGenerator {

    static Random random = CommonState.r;

    final long baseServiceMemory;
    final short serviceMemoryMultiplier;

    public UniformServiceMemoryRequirementGenerator(long baseServiceMemory,
                                                    short serviceMemoryMultiplier) {
        this.baseServiceMemory = baseServiceMemory;
        this.serviceMemoryMultiplier = serviceMemoryMultiplier;
    }

    public long nextMemRequirement(){
        return baseServiceMemory * (1 + random.nextInt(serviceMemoryMultiplier));
    }
}
