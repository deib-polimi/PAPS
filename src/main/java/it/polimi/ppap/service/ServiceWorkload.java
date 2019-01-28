package it.polimi.ppap.service;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.topology.FogNode;

public class ServiceWorkload {

    private final FogNode source;
    private final Service service;
    private float workload;

    public ServiceWorkload(FogNode source, Service service, float demand){
        this.source = source;
        this.service = service;
        this.workload = demand;
    }

    public Service getService() {
        return service;
    }

    public float getWokload() {
        return workload;
    }

    public FogNode getSource(){
        return source;
    }
}
