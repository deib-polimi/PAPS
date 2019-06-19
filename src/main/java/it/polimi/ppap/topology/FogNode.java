package it.polimi.ppap.topology;

import peersim.core.GeneralNode;
import peersim.core.Node;

import java.util.HashMap;
import java.util.Map;

public class FogNode extends GeneralNode implements Node, Comparable{

    private long memoryCapacity;

    private Map<Long, Integer> linksDelay;

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
    public Object clone() {
        FogNode result = null;
        result=(FogNode)super.clone();
        result.linksDelay = new HashMap<>();
        return result;
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

    public void addLinkDelay(long id, int linkDelay) {
        linksDelay.put(id, linkDelay);
    }

    public int getLinkDelay(long id){
        return linksDelay.get(id);
    }
}
