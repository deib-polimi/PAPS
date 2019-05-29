package it.polimi.ppap.transport;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.service.ServiceWorkload;
import it.polimi.ppap.topology.FogNode;

import java.util.Map;
import java.util.Set;

public class MemberMessage extends CommunityMessage {

    final Map<Service, Set<ServiceWorkload>> localServiceWorkloadHistory;
    final Map<Service, Map.Entry<Float, Float>> workloadAllocation;

    public MemberMessage(FogNode sender, Map<Service, Set<ServiceWorkload>> localServiceWorkloadHistory, Map<Service, Map.Entry<Float, Float>> workflowAllocation){
        super(sender, MBR_MON_MSG);
        this.localServiceWorkloadHistory = localServiceWorkloadHistory;
        this.workloadAllocation = workflowAllocation;
    }

    public Map<Service, Set<ServiceWorkload>> getLocalServiceWorkloadHistory() {
        return localServiceWorkloadHistory;
    }

    public Map<Service, Map.Entry<Float, Float>> getWorkloadAllocation() {
        return workloadAllocation;
    }
}
