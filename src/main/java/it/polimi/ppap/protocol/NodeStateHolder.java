package it.polimi.ppap.protocol;

import it.polimi.deib.ppap.node.NodeFacade;
import peersim.core.Protocol;

public abstract class NodeStateHolder implements Protocol {

    long memoryCapacity;
    Object currentDemand;
    NodeFacade nodeFacade;

    @Override
    public Object clone() {
        NodeStateHolder svh=null;
        try {
            svh=(NodeStateHolder)super.clone();
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return svh;
    }

    public long getMemoryCapacity() {
        return memoryCapacity;
    }

    public void setMemoryCapacity(long memoryCapacity) {
        this.memoryCapacity = memoryCapacity;
    }

    public NodeFacade getNodeFacade() {
        return nodeFacade;
    }

    public void setNodeFacade(NodeFacade nodeFacade) {
        this.nodeFacade = nodeFacade;
    }

    public Object getCurrentDemand() {
        return currentDemand;
    }

    public void setCurrentDemand(Object currentDemand) {
        this.currentDemand = currentDemand;
    }
}
