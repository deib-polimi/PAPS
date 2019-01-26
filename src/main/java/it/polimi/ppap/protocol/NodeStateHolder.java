package it.polimi.ppap.protocol;

import it.polimi.deib.ppap.node.NodeFacade;
import it.polimi.ppap.generator.workload.ServiceRequestGenerator;
import it.polimi.ppap.model.FogNode;
import peersim.core.Protocol;

import java.util.Set;

public abstract class NodeStateHolder implements Protocol {

    Object currentDemand;
    NodeFacade nodeFacade;
    ServiceRequestGenerator serviceRequestGenerator;

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

    public NodeFacade getNodeFacade() {
        return nodeFacade;
    }

    public void setNodeFacade(NodeFacade nodeFacade) {
        this.nodeFacade = nodeFacade;
    }

    public Object getCurrentDemand() {
        return currentDemand;
    }

    public void setCurrentDemand(Object currentDemand) {
        this.currentDemand = currentDemand;
    }
}
