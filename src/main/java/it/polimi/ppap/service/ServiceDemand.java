package it.polimi.ppap.service;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.topology.FogNode;

public class ServiceDemand implements Comparable{

    private final FogNode source;
    private final Service service;
    private float demand;

    public ServiceDemand(FogNode source, Service service, float demand){
        this.source = source;
        this.service = service;
        this.demand = demand;
    }

    public Service getService() {
        return service;
    }

    public float getDemand() {
        return demand;
    }

    public FogNode getSource() {
        return source;
    }

    @Override
    public int compareTo(Object o) {
        return toString().compareTo(((ServiceDemand)o).toString());
    }

    @Override
    public String toString() {
        return service.getId() + "@" + source.getID();
    }
}
