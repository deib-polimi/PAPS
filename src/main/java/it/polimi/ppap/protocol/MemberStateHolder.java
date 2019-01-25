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



    //MAPE - Monitoring and Analysis

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

    public Map<FogNode, Map<Service, Float>> getNodeServiceDemand(){
        return nodeServiceDemand;
    }

    public void setNodeServiceDemand(Map<FogNode, Map<Service, Float>> nodeServiceDemand){
        this.nodeServiceDemand = nodeServiceDemand;
    }

    public void updateServiceDemand(FogNode node, Service service, float demand){
        if(!nodeServiceDemand.containsKey(node))
            nodeServiceDemand.put(node, new TreeMap<>());
        Map<Service, Float> serviceDemand = nodeServiceDemand.get(node);
        serviceDemand.put(service, demand);
        nodeServiceDemand.put(node, serviceDemand);
    }

    //MAPE - Planning

    Map<FogNode, Map<Service, Float>> nodeServiceAllocation = new TreeMap<>();

    PlacementAllocationSchema placementAllocationSchema;

    public PlacementAllocationSchema getPlacementAllocationSchema() {
        return placementAllocationSchema;
    }

    public void setPlacementAllocationSchema(PlacementAllocationSchema placementAllocationSchema) {
        this.placementAllocationSchema = placementAllocationSchema;
    }

    public Map<FogNode, Map<Service, Float>> getNodeServiceAllocation() {
        return nodeServiceAllocation;
    }

    public void setNodeServiceAllocation(Map<FogNode, Map<Service, Float>> nodeServiceAllocation) {
        this.nodeServiceAllocation = nodeServiceAllocation;
    }

    //MAPE - ?



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
