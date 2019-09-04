package it.polimi.ppap.protocol.adaptation;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.protocol.CommunityProtocol;
import it.polimi.ppap.protocol.MemberStateHolder;
import it.polimi.ppap.protocol.NodeProtocol;
import it.polimi.ppap.service.AggregateServiceAllocation;
import it.polimi.ppap.service.ServiceWorkload;
import it.polimi.ppap.topology.FogNode;
import it.polimi.ppap.transport.MemberMessage;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.transport.Transport;

import java.util.Map;
import java.util.Set;

public class CommunityMemberBehaviour {

    final MemberStateHolder memberStateHolder;

    public CommunityMemberBehaviour(MemberStateHolder memberStateHolder) {
        this.memberStateHolder = memberStateHolder;
    }

    //MAPE: MONITORING
    public void monitor(FogNode node, int nodePid, int pid){
        NodeProtocol nodeProtocol = (NodeProtocol) node.getProtocol(nodePid);
        Map<Service, Set<ServiceWorkload>> localServiceWorkload = nodeProtocol.getLocalServiceWorkloadHistory();
        nodeProtocol.clearLocalServiceWorkloadHistory();
        Map<Service, Map.Entry<Float, Float>> currentDemandAllocation = nodeProtocol.getCurrentWorkloadAllocation();
        try {
            sendMonitoredDataToLeader(localServiceWorkload, currentDemandAllocation, node, pid);
        } catch (CommunityLeaderNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendMonitoredDataToLeader(Map<Service, Set<ServiceWorkload>> localServiceWorkload,
                                           Map<Service, Map.Entry<Float, Float>> currentWorkloadAllocation,
                                           FogNode node, int pid) throws CommunityLeaderNotFoundException {
        FogNode communityLeader = getCommunityLeader(node, pid);
        ((Transport) node.getProtocol(FastConfig.getTransport(pid))).
                send(
                        node,
                        communityLeader,
                        new MemberMessage(node, localServiceWorkload, currentWorkloadAllocation),
                        pid);
    }


    private FogNode getCommunityLeader(FogNode node, int pid) throws CommunityLeaderNotFoundException {
        Linkable linkable =
                (Linkable) node.getProtocol(FastConfig.getLinkable(pid));
        if(memberStateHolder.isLeader())
            return node;
        else {
            for (int i = 0; i < linkable.degree(); i++) {
                FogNode member = (FogNode) linkable.getNeighbor(i);
                CommunityProtocol memberCommunityProtocol = (CommunityProtocol) member.getProtocol(pid);
                if (memberCommunityProtocol.isLeader())
                    return member;
            }
            throw new CommunityLeaderNotFoundException("The community has no elected leader");
        }
    }

    private class CommunityLeaderNotFoundException extends Exception {
        CommunityLeaderNotFoundException(String msg){
            super(msg);
        }
    }

    public void execute(Map<Service, AggregateServiceAllocation> placementAllocation, Node node, int nodePid) {
        NodeProtocol nodeProtocol = (NodeProtocol) node.getProtocol(nodePid);
        nodeProtocol.updatePlacementAllocation((FogNode) node, placementAllocation, nodePid);
    }
}
