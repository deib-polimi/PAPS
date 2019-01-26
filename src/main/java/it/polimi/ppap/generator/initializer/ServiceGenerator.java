package it.polimi.ppap.generator.initializer;

import it.polimi.deib.ppap.node.services.Service;
import peersim.core.CommonState;

import java.util.Random;

public class ServiceGenerator {

    static Random random = CommonState.r;
    static long serviceCount = 0;

    final long baseServiceMemory;
    final short serviceMemoryMultiplier;

    public ServiceGenerator(
            long baseServiceMemory,
            short serviceMemoryMultiplier){
        this.baseServiceMemory = baseServiceMemory;
        this.serviceMemoryMultiplier = serviceMemoryMultiplier;
    }

    public  Service nextService(float targetRT){
        String id = getNextServiceId();
        long memory = getNextServiceMemoryRequirement();
        incServiceCount();
        return new Service(id, memory, targetRT);
    }

    private long getNextServiceMemoryRequirement() {
        return baseServiceMemory * (1 + random.nextInt(serviceMemoryMultiplier));
    }

    private String getNextServiceId(){
        return serviceCount + "";
    }

    private void incServiceCount(){
        serviceCount++;
    }
}