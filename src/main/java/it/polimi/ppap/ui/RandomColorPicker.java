package it.polimi.ppap.ui;

import java.awt.*;
import java.util.Random;

public class RandomColorPicker {

    private static Random r = new Random();

    public static Color pickRandomColor(){

        int red = r.nextInt(256);
        int green = r.nextInt(256);
        int blue = r.nextInt(256);
        return new Color(red, green, blue);
    }
}
