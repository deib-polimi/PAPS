package it.polimi.ppap.protocol;

import it.polimi.deib.ppap.node.NodeFacade;
import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.generator.workload.ServiceRequestGenerator;
import peersim.core.Protocol;

import java.util.Map;
import java.util.TreeMap;

public abstract class NodeStateHolder implements Protocol, NodeFacade.TickListener {

    NodeFacade nodeFacade;
    ServiceRequestGenerator serviceRequestGenerator;
    Map<Service, Map.Entry<Float, Float>> currentDemandAllocation = new TreeMap<>();

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

    public ServiceRequestGenerator getServiceRequestGenerator() {
        return serviceRequestGenerator;
    }

    public void setServiceRequestGenerator(ServiceRequestGenerator serviceRequestGenerator) {
        this.serviceRequestGenerator = serviceRequestGenerator;
    }

    public void setNodeFacade(NodeFacade nodeFacade) {
        this.nodeFacade = nodeFacade;
    }

    public Map<Service, Map.Entry<Float, Float>> getCurrentDemandAllocation() {
        return currentDemandAllocation;
    }
}
