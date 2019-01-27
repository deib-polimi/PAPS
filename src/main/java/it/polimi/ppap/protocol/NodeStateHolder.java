package it.polimi.ppap.protocol;

import it.polimi.deib.ppap.node.NodeFacade;
import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.generator.workload.ServiceRequestGenerator;
import peersim.core.Protocol;

import java.util.Map;

public abstract class NodeStateHolder implements Protocol, NodeFacade.TickListener {

    NodeFacade nodeFacade;
    ServiceRequestGenerator serviceRequestGenerator;

    /**
     * For each service hosted by this node, stores the current demand (interarrival time) and allocation (CTNs)
     */
    Map<Service, Map.Entry<Float, Float>> currentWorkloadAllocation;

    public ServiceRequestGenerator getServiceRequestGenerator() {
        return serviceRequestGenerator;
    }

    public void setServiceRequestGenerator(ServiceRequestGenerator serviceRequestGenerator) {
        this.serviceRequestGenerator = serviceRequestGenerator;
    }

    public void setNodeFacade(NodeFacade nodeFacade) {
        this.nodeFacade = nodeFacade;
    }

    public Map<Service, Map.Entry<Float, Float>> getCurrentWorkloadAllocation() {
        return currentWorkloadAllocation;
    }

    public void setCurrentWorkloadAllocation(Map<Service, Map.Entry<Float, Float>> currentWorkloadAllocation) {
        this.currentWorkloadAllocation = currentWorkloadAllocation;
    }

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
}
