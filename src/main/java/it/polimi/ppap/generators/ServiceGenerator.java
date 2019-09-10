package it.polimi.ppap.generators;

import it.polimi.deib.ppap.node.services.Service;

public class ServiceGenerator {
    static long serviceCount = 0;

    final UniformServiceMemoryRequirementGenerator serviceMemoryRequirementGenerator;

    public ServiceGenerator(
            long baseServiceMemory,
            short serviceMemoryMultiplier){
        this.serviceMemoryRequirementGenerator = new UniformServiceMemoryRequirementGenerator(baseServiceMemory, serviceMemoryMultiplier);
    }

    public  Service nextService(float rtSLA, float etMax){
        String id = getNextServiceId();
        long memory = serviceMemoryRequirementGenerator.nextMemRequirement();
        incServiceCount();
        return new Service(id, memory, rtSLA, etMax);
    }

    private String getNextServiceId(){
        return serviceCount + "";
    }

    private void incServiceCount(){
        serviceCount++;
    }
}