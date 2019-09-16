package it.polimi.ppap.protocol.community;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.protocol.node.NodeProtocol;
import it.polimi.ppap.service.AggregateServiceAllocation;
import it.polimi.ppap.service.ServiceWorkload;
import it.polimi.ppap.topology.community.Community;
import it.polimi.ppap.topology.community.CommunityLeaderNotFoundException;
import it.polimi.ppap.topology.node.FogNode;
import peersim.config.FastConfig;
import peersim.core.Node;
import peersim.transport.Transport;

import java.util.Map;
import java.util.Set;

public class CommunityMemberBehavior {

    final MemberStateHolder memberStateHolder;

    public CommunityMemberBehavior(MemberStateHolder memberStateHolder) {
        this.memberStateHolder = memberStateHolder;
    }

    //MAPE: MONITORING
    public void cyclicBehavior(FogNode node, Community community, int nodePid, int pid){
        NodeProtocol nodeProtocol = (NodeProtocol) node.getProtocol(nodePid);
        Map<Service, Set<ServiceWorkload>> localServiceWorkload = nodeProtocol.getLocalServiceWorkloadHistory();
        nodeProtocol.clearLocalServiceWorkloadHistory();
        Map<Service, Map.Entry<Float, Float>> currentDemandAllocation = nodeProtocol.getCurrentWorkloadAllocation();
        try {
            //TODO must respect the shares among communities if this is a shared node
            monitor(node, community, localServiceWorkload, currentDemandAllocation, pid);
        } catch (CommunityLeaderNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void monitor(FogNode node, Community community,
                         Map<Service, Set<ServiceWorkload>> localServiceWorkload,
                         Map<Service, Map.Entry<Float, Float>> currentWorkloadAllocation,
                         int pid) throws CommunityLeaderNotFoundException {
        FogNode leader = community.getLeader();
        ((Transport) node.getProtocol(FastConfig.getTransport(pid))).
            send(
                node,
                leader,
                new MemberMessage(node, community.getId(), localServiceWorkload, currentWorkloadAllocation),
                pid
            );
    }

    public void execute(Map<Service, AggregateServiceAllocation> placementAllocation, Node node, int nodePid) {
        NodeProtocol nodeProtocol = (NodeProtocol) node.getProtocol(nodePid);
        nodeProtocol.updatePlacementAllocation((FogNode) node, placementAllocation, nodePid);
    }
}