package it.polimi.ppap.protocol.community;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.service.AggregateServiceAllocation;
import it.polimi.ppap.service.ServiceCatalog;
import it.polimi.ppap.service.ServiceDemand;
import it.polimi.ppap.service.ServiceWorkload;
import it.polimi.ppap.topology.node.FogNode;

import java.util.*;

public class MemberState {

    final String communityId;

    public MemberState(String communityId){
        this.communityId = communityId;
        this.leader = false;
        this.nodeServiceWorkload = new TreeMap<>();
        this.workloadAllocationHistory = new TreeMap<>();
        this.nodeServiceDemand = new TreeMap<>();
        this.monitoringCount = new HashMap<>();
    }

    boolean leader;

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
        this.monitoringCount.remove(id);
    }

    Map<FogNode, Map<Service, Set<ServiceWorkload>>> nodeServiceWorkload;

    public Map<FogNode, Map<Service, Set<ServiceWorkload>>> getNodeServiceWorkload() {
        return nodeServiceWorkload;
    }

    public void updateNodeServiceWorkload(
            FogNode sender, Map<Service,
            Set<ServiceWorkload>> localServiceWorkloadHistory){

        Map<Service, Set<ServiceWorkload>> nodeServiceWorkloads = nodeServiceWorkload.getOrDefault(sender, new TreeMap<>());
        nodeServiceWorkload.put(sender, nodeServiceWorkloads);
        for(Service service : ServiceCatalog.getServiceCatalog()) {
            Set<ServiceWorkload> serviceWorkloads = nodeServiceWorkload.get(sender).getOrDefault(service, new HashSet<>());
            if (localServiceWorkloadHistory.containsKey(service))
                for(ServiceWorkload serviceWorkload : localServiceWorkloadHistory.get(service))
                    serviceWorkloads.add(serviceWorkload);
            else
                serviceWorkloads.add(new ServiceWorkload(sender, service, 0f, 0f));

            nodeServiceWorkload.get(sender).put(service, serviceWorkloads);
        }
    }

    Map<Service, Map<Float, Float>> workloadAllocationHistory;

    public Map<Service, Map<Float, Float>> getWorkloadAllocationHistory() {
        return workloadAllocationHistory;
    }

    public void storeNodeWorkloadAllocation(FogNode sender, Map<Service, Map.Entry<Float, Float>> currentWorkloadAllocation){
        for(Service service : currentWorkloadAllocation.keySet()) {
            if(!workloadAllocationHistory.containsKey(service))
                workloadAllocationHistory.put(service, new TreeMap<>());
            Map.Entry<Float, Float> workloadAllocation = currentWorkloadAllocation.get(service);
            if(workloadAllocation.getValue() > 0)
                workloadAllocationHistory.get(service).put(1 / workloadAllocation.getKey(), workloadAllocation.getValue());
        }
    }

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


    Map<FogNode, Map<Service, AggregateServiceAllocation>> nodeServiceAllocation = new TreeMap<>();

    public Map<FogNode, Map<Service, AggregateServiceAllocation>> getNodeServiceAllocation() {
        return nodeServiceAllocation;
    }

    public void setNodeServiceAllocation(Map<FogNode, Map<Service, AggregateServiceAllocation>> nodeServiceAllocation) {
        this.nodeServiceAllocation = nodeServiceAllocation;
    }
}
