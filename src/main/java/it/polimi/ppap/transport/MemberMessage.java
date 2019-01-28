package it.polimi.ppap.transport;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.service.ServiceWorkload;
import it.polimi.ppap.topology.FogNode;

import java.util.Map;

public class MemberMessage extends CommunityMessage {

    final Map<Service, ServiceWorkload> localServiceWorkload;
    final Map<Service, Map.Entry<Float, Float>> workloadAllocation;

    public MemberMessage(FogNode sender, Map<Service, ServiceWorkload> localServiceWorkload, Map<Service, Map.Entry<Float, Float>> workflowAllocation){
        super(sender, MBR_MON_MSG);
        this.localServiceWorkload = localServiceWorkload;
        this.workloadAllocation = workflowAllocation;
    }

    public Map<Service, ServiceWorkload> getLocalServiceWorkload() {
        return localServiceWorkload;
    }

    public Map<Service, Map.Entry<Float, Float>> getWorkloadAllocation() {
        return workloadAllocation;
    }
}
