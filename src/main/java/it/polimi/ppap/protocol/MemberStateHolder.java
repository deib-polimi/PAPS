package it.polimi.ppap.protocol;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.common.scheme.PlacementAllocationSchema;
import it.polimi.ppap.model.FogNode;
import peersim.core.Protocol;

import java.util.Map;
import java.util.TreeMap;

public abstract class MemberStateHolder implements Protocol {

    boolean leader;
    int monitoringCount;
    public boolean isLeader() {
        return leader;
    }
    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    //MAPE: Monitoring

    Map<FogNode, Map<Service, Float>> nodeServiceDemand = new TreeMap<>();

    public int getMonitoringCount() {
        return monitoringCount;
    }

    public void incMonitoringCount() {
        this.monitoringCount++;
    }

    public void resetMonitoringCount(){
        this.monitoringCount = 0;
    }

    //MAPE: Analysis

    public Map<FogNode, Map<Service, Float>> getNodeServiceDemand(){
        return nodeServiceDemand;
    }

    public void updateServiceDemand(FogNode node, Service service, float demand){
        if(!nodeServiceDemand.containsKey(node))
            nodeServiceDemand.put(node, new TreeMap<>());
        Map<Service, Float> serviceDemand = nodeServiceDemand.get(node);
        serviceDemand.put(service, demand);
        nodeServiceDemand.put(node, serviceDemand);
    }

    //MAPE: Planning

    Map<FogNode, Map<Service, Float>> nodeServiceAllocation = new TreeMap<>();

    public Map<FogNode, Map<Service, Float>> getNodeServiceAllocation() {
        return nodeServiceAllocation;
    }

    public void setNodeServiceAllocation(Map<FogNode, Map<Service, Float>> nodeServiceAllocation) {
        this.nodeServiceAllocation = nodeServiceAllocation;
    }

    /**
     * Overrides the clone method, as required by PeerSim.
     * TODO Check if we need to properly implement this (i.e. clone the member's state)
     * @return
     */
    @Override
    public Object clone() {
        MemberStateHolder svh=null;
        try {
            svh=(MemberStateHolder)super.clone();
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return svh;
    }
}
