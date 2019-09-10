package it.polimi.ppap.generators;

import org.apache.commons.math3.distribution.NormalDistribution;
import peersim.core.CommonState;

import java.util.Random;

public class NormalServiceWorkloadGenerator {

    final NormalDistribution normalDistribution;
    final Random random = CommonState.r;
    final float activeProbability;

    public NormalServiceWorkloadGenerator(float mean, float std, float activeProbability) {
        normalDistribution = new NormalDistribution(mean, std);
        this.activeProbability = activeProbability;
    }

    public float nextWorkload(){
        if(random.nextInt() < activeProbability) {
            float sample;
            for (int i = 0; i < 1000; i++)
                if ((sample = (float) normalDistribution.sample()) > 0)
                    return sample;
            throw new NegativeServiceWorkloadException();
        }else
            return 0f;
    }


    public class NegativeServiceWorkloadException extends RuntimeException{}
}
