package it.polimi.ppap.protocol.community;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.service.ServiceWorkload;
import it.polimi.ppap.topology.community.Community;
import it.polimi.ppap.topology.node.FogNode;

import java.util.Map;
import java.util.Set;

public class MemberMessage extends CommunityMessage {

    final String communityId;
    final Map<Service, Set<ServiceWorkload>> localServiceWorkloadHistory;
    final Map<Service, Map.Entry<Float, Float>> workloadAllocation;

    public MemberMessage(FogNode sender, String communityId, Map<Service, Set<ServiceWorkload>> localServiceWorkloadHistory, Map<Service, Map.Entry<Float, Float>> workflowAllocation){
        super(sender, MBR_MON_MSG);
        this.communityId = communityId;
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
