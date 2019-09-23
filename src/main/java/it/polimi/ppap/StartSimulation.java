package it.polimi.ppap;

import peersim.Simulator;

/**
 * Placeholder for starting the simulation using a class within our package.
 * @see Simulator
 */
public class StartSimulation extends Simulator{

    public static void main(String [] args){
        System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        Simulator.main(args);
    }
}
