package it.polimi.ppap.protocol;

import com.google.common.collect.ImmutableMap;
import it.polimi.deib.ppap.node.NodeFacade;
import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.service.workload.ServiceRequestGenerator;
import it.polimi.ppap.service.ServiceWorkload;
import peersim.core.Protocol;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    Map<Service, Set<ServiceWorkload>> localServiceWorkloadHistory;

    public Map<Service, Set<ServiceWorkload>> getLocalServiceWorkloadHistory() {
        return ImmutableMap.copyOf(localServiceWorkloadHistory);
    }

    public void setLocalServiceWorkloadHistory(Map<Service, Set<ServiceWorkload>> localServiceWorkloadHistory) {
        this.localServiceWorkloadHistory = localServiceWorkloadHistory;
    }

    public void clearLocalServiceWorkloadHistory(){
        localServiceWorkloadHistory.clear();
    }

    protected void addToLocalServiceWorkloadHistory(ServiceWorkload serviceWorkload){
        Set<ServiceWorkload> serviceWorkloads = localServiceWorkloadHistory.getOrDefault(serviceWorkload.getService(), new HashSet<>());
        serviceWorkloads.add(serviceWorkload);
        localServiceWorkloadHistory.put(serviceWorkload.getService(), serviceWorkloads);
    }

    /**
     * For each service hosted by this node, keeps track of the workload (interarrival time) -> allocation (CTNs)
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
        NodeStateHolder svh = null;
        try {
            svh = (NodeStateHolder) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return svh;
    }
}
