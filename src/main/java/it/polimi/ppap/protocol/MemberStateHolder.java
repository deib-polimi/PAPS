package it.polimi.ppap.protocol;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.common.scheme.ServiceDemand;
import it.polimi.ppap.common.scheme.PlacementAllocationSchema;
import peersim.core.Node;
import peersim.core.Protocol;

import java.util.HashMap;
import java.util.Map;

public abstract class MemberStateHolder implements Protocol {

    boolean leader;
    int monitoringCount;
    Object demandAllocation;
    public boolean isLeader() {
        return leader;
    }
    public void setLeader(boolean leader) {
        this.leader = leader;
    }



    //MAPE - Monitoring and Analysis

    Map<Node, Map<Service, Float>> nodeServiceDemand = new HashMap<>();

    public int getMonitoringCount() {
        return monitoringCount;
    }

    public void incMonitoringCount() {
        this.monitoringCount++;
    }

    public void resetMonitoringCount(){
        this.monitoringCount = 0;
    }

    public Map<Node, Map<Service, Float>> getNodeServiceDemand(){
        return nodeServiceDemand;
    }

    public void setNodeServiceDemand(Map<Node, Map<Service, Float>> nodeServiceDemand){
        this.nodeServiceDemand = nodeServiceDemand;
    }

    public void updateServiceDemand(Node node, Service service, float demand){
        if(!nodeServiceDemand.containsKey(node))
            nodeServiceDemand.put(node, new HashMap<>());
        Map<Service, Float> serviceDemand = nodeServiceDemand.get(node);
        serviceDemand.put(service, demand);
        nodeServiceDemand.put(node, serviceDemand);
    }

    //MAPE - Planning

    PlacementAllocationSchema placementAllocationSchema;

    public PlacementAllocationSchema getPlacementAllocationSchema() {
        return placementAllocationSchema;
    }

    public void setPlacementAllocationSchema(PlacementAllocationSchema placementAllocationSchema) {
        this.placementAllocationSchema = placementAllocationSchema;
    }

    //MAPE - ?

    public Object getDemandAllocation() {
        return demandAllocation;
    }

    public void setDemandAllocation(Object demandAllocation) {
        this.demandAllocation = demandAllocation;
    }


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
