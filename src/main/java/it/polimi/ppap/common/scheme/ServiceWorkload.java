package it.polimi.ppap.common.scheme;

import it.polimi.deib.ppap.node.services.Service;

public class ServiceWorkload {

    private final Service service;
    private float workload;

    public ServiceWorkload(Service service, float demand){
        this.service = service;
        this.workload = demand;
    }

    public Service getService() {
        return service;
    }

    public float getWokload() {
        return workload;
    }
}
