package it.polimi.ppap.service;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.topology.FogNode;

public class ServiceAllocation implements Comparable{

    private final FogNode source;
    private final Service service;
    private float allocation;
    private float fraction;

    public ServiceAllocation(FogNode source, Service service, float allocation, float fraction){
        this.source = source;
        this.service = service;
        this.allocation = allocation;
        this.fraction = fraction;
    }

    public FogNode getSource() {
        return source;
    }

    public Service getService() {
        return service;
    }

    public float getAllocation() {
        return allocation;
    }

    public float getFraction() {
        return fraction;
    }

    @Override //TODO
    public int compareTo(Object o) {
        ServiceAllocation other = (ServiceAllocation) o;
        String combinedId = allocation + "_" + service.getId() + "_" + source.getID();
        String otherCombinedId = other.getAllocation() + "_" + other.getService().getId() + "_" + other.getSource().getID();
        return combinedId.compareTo(otherCombinedId);
    }

    @Override
    public String toString() {
        return allocation + " to " + service + " from " + source;
    }
}
