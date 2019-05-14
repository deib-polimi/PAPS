package it.polimi.ppap.service;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.topology.FogNode;

public class ServiceWorkload {

    private final FogNode source;
    private final Service service;
    private float workload;

    public ServiceWorkload(FogNode source, Service service, float workload){
        this.source = source;
        this.service = service;
        this.workload = workload;
    }

    public Service getService() {
        return service;
    }

    public float getWorkload() {
        return workload;
    }

    public FogNode getSource(){
        return source;
    }

    public int getInterNodeDelay(FogNode target) {
        return source.getLinkDelay(target.getID());
    }
}
