package it.polimi.ppap.generator.initializer;

import it.polimi.deib.ppap.node.services.Service;
import org.apache.commons.math3.distribution.NormalDistribution;

public class ServiceWorkloadGenerator {

    final NormalDistribution normalDistribution;

    public ServiceWorkloadGenerator(Service service) {
        float allocationFactor = 5 / service.getTargetAllocation();
        float mean = service.getSLA() * allocationFactor;
        float std = service.getSLA();
        normalDistribution = new NormalDistribution(mean, std);
    }

    public float nextWorkload(){
        return (float) normalDistribution.sample();
    }
}
