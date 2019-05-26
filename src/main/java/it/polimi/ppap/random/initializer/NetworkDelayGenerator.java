package it.polimi.ppap.random.initializer;

import peersim.core.CommonState;

import java.util.Random;

public class NetworkDelayGenerator {

    public Random rand = CommonState.r;

    public int nextDelay(int maxSize) {
        //Get a linearly multiplied random number
        int randomMultiplier = maxSize * (maxSize + 1) / 2;
        int randomInt = rand.nextInt(randomMultiplier);

        //Linearly iterate through the possible values to find the correct one
        int linearRandomNumber = 0;
        for (int i = maxSize; randomInt >= 0; i--) {
            randomInt -= i;
            linearRandomNumber++;
        }
        return linearRandomNumber;
    }
}