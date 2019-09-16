package it.polimi.ppap.protocol.community;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.service.AggregateServiceAllocation;
import it.polimi.ppap.service.ServiceDemand;
import it.polimi.ppap.service.ServiceWorkload;
import it.polimi.ppap.topology.node.FogNode;
import peersim.core.Protocol;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public abstract class MemberStateHolder implements Protocol {

    //Community

    boolean leader;
    public boolean isLeader() {
        return leader;
    }
    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    CommunityMemberBehavior communityMemberBehaviour;

    public void setCommunityMemberBehaviour(CommunityMemberBehavior communityMemberBehaviour) {
        this.communityMemberBehaviour = communityMemberBehaviour;
    }

    CommunityLeaderBehaviour communityLeaderBehavior;

    public void initializeLeader(float optimizationBeta, int referenceControlPeriod){
        setLeader(true);
        nodeServiceWorkload = new TreeMap<>();
        workloadAllocationHistory = new TreeMap<>();
        nodeServiceDemand = new TreeMap<>();
        this.optimizationBeta = optimizationBeta;
        this.referenceControlPeriod = referenceControlPeriod;
        communityLeaderBehavior = new CommunityLeaderBehaviour(this);
    }

    //MAPE: Monitoring

    Map<String, Integer> monitoringCount;

    public int getMonitoringCount(String communityId) {
        return monitoringCount.get(communityId);
    }

    public void incMonitoringCount(String communityId) {
        Integer actual = this.monitoringCount.getOrDefault(communityId, 0);
        this.monitoringCount.put(communityId, ++actual);
    }

    public void resetMonitoringCount(String id){
        this.monitoringCount.put(id, 0);
    }

    Map<FogNode, Map<Service, Set<ServiceWorkload>>> nodeServiceWorkload;

    public Map<FogNode, Map<Service, Set<ServiceWorkload>>> getNodeServiceWorkload() {
        return nodeServiceWorkload;
    }

    Map<Service, Map<Float, Float>> workloadAllocationHistory;

    //MAPE: Analysis

    Map<FogNode, Map<Service, ServiceDemand>> nodeServiceDemand;

    public Map<FogNode, Map<Service, ServiceDemand>> getNodeServiceDemand(){
        return nodeServiceDemand;
    }

    public void updateServiceDemand(FogNode node, Service service, float demand){
        if(!nodeServiceDemand.containsKey(node))
            nodeServiceDemand.put(node, new TreeMap<>());
        Map<Service, ServiceDemand> serviceDemand = nodeServiceDemand.get(node);
        serviceDemand.put(service, new ServiceDemand(node, service, demand));//TODO replace or update?
        nodeServiceDemand.put(node, serviceDemand);
    }

    //MAPE: Planning

    float optimizationBeta;

    public float getOptimizationBeta() {
        return optimizationBeta;
    }

    int referenceControlPeriod;

    public int getReferenceControlPeriod() {
        return referenceControlPeriod;
    }

    Map<FogNode, Map<Service, AggregateServiceAllocation>> nodeServiceAllocation = new TreeMap<>();

    public Map<FogNode, Map<Service, AggregateServiceAllocation>> getNodeServiceAllocation() {
        return nodeServiceAllocation;
    }

    public void setNodeServiceAllocation(Map<FogNode, Map<Service, AggregateServiceAllocation>> nodeServiceAllocation) {
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
