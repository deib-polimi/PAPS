package it.polimi.ppap.topology.node;

import it.polimi.deib.ppap.node.NodeFacade;

public class FogNodeFactory {

    public static NodeFacade createCTNodeFacade(FogNode node, long controlPeriod, float alpha, boolean CT) {
        return new NodeFacade(node.getID() + "", node.getMemoryCapacity(), controlPeriod, alpha, CT);
    }
}
