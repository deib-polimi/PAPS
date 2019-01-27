package it.polimi.ppap.model;

import peersim.core.GeneralNode;
import peersim.core.Node;

public class FogNode extends GeneralNode implements Node, Comparable{

    private long memoryCapacity;

    public long getMemoryCapacity() {
        return memoryCapacity;
    }

    public void setMemoryCapacity(long capacity) {
        this.memoryCapacity = capacity;
    }

    public FogNode(String prefix){
        super(prefix);
    }

    @Override
    public int compareTo(Object o) {
        FogNode other = (FogNode) o;
        return (int) (this.getID() - other.getID());
    }

    @Override
    public String toString() {
        return "FogNode " + getID();
    }
}
