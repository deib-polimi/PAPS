package it.polimi.ppap.common.scheme;

import it.polimi.deib.ppap.node.services.Service;

public class ServiceDemand {

    private final Service service;
    private float demand;

    public ServiceDemand(Service service, float demand){
        this.service = service;
        this.demand = demand;
    }
}
