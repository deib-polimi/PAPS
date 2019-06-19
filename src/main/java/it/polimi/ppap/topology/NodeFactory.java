package it.polimi.ppap.topology;

import it.polimi.deib.ppap.node.NodeFacade;

public class NodeFactory {

    public static NodeFacade createCTNodeFacade(FogNode node, long controlPeriod, float alpha, boolean CT) {
        return new NodeFacade(node.getID() + "", node.getMemoryCapacity(), controlPeriod, alpha, CT);
    }
}
