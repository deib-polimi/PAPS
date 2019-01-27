package it.polimi.ppap.generator.initializer;

import it.polimi.deib.ppap.node.services.Service;
import org.apache.commons.math3.distribution.NormalDistribution;

public class ServiceWorkloadGenerator {

    final NormalDistribution normalDistribution;

    public ServiceWorkloadGenerator(float mean, float std) {
        normalDistribution = new NormalDistribution(mean, std);
    }

    public float nextWorkload(){
        float sample;
        for(int i=0; i < 1000; i++)
            if((sample = (float) normalDistribution.sample()) >= 0)
                return sample;
        throw new NegativeServiceWorkloadException();
    }


    public class NegativeServiceWorkloadException extends RuntimeException{}
}
