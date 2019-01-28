package it.polimi.ppap.protocol;

import it.polimi.deib.ppap.node.NodeFacade;
import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.generator.workload.ServiceRequestGenerator;
import it.polimi.ppap.service.ServiceWorkload;
import peersim.core.Protocol;

import java.util.Map;

public abstract class NodeStateHolder implements Protocol, NodeFacade.TickListener {

    protected NodeFacade nodeFacade;
    protected ServiceRequestGenerator serviceRequestGenerator;


    /**
     * The source of workload for different services generated at this node (surrogate access points)
     */
    Map<Service, ServiceWorkload> localServiceWorkload;

    public void setLocalServiceWorkload(Map<Service, ServiceWorkload> localServiceWorkload) {
        this.localServiceWorkload = localServiceWorkload;
    }

    public Map<Service, ServiceWorkload> getLocalServiceWorkload() {
        return localServiceWorkload;
    }

    /**
     * For each service hosted by this node, keeps track of theworkload (interarrival time) -> allocation (CTNs)
     */
    Map<Service, Map.Entry<Float, Float>> currentWorkloadAllocation;

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
