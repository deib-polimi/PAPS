package it.polimi.ppap.service;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.topology.FogNode;

public class ServiceDemand implements Comparable{

    private final FogNode source;
    private final Service service;
    private float demand;
    private float fraction;

    public ServiceDemand(FogNode source, Service service, float demand, float fraction){
        this.source = source;
        this.service = service;
        this.demand = demand;
        this.fraction = fraction;
    }

    public FogNode getSource() {
        return source;
    }

    public Service getService() {
        return service;
    }

    public float getDemand() {
        return demand;
    }

    public float getFraction() {
        return fraction;
    }

    @Override //TODO
    public int compareTo(Object o) {
        ServiceDemand other = (ServiceDemand) o;
        String combinedId = demand + "_" + service.getId() + "_" + source.getID();
        String otherCombinedId = other.getDemand() + "_" + other.getService().getId() + "_" + other.getSource().getID();
        return combinedId.compareTo(otherCombinedId);
    }

    @Override
    public String toString() {
        return demand + " to " + service + " from " + source;
    }
}
