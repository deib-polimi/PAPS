package it.polimi.ppap.protocol.community;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.service.AggregateServiceAllocation;
import it.polimi.ppap.topology.FogTopology;
import it.polimi.ppap.topology.community.Community;
import it.polimi.ppap.topology.node.FogNode;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

import java.util.Map;

public class CommunityProtocol
        extends MemberStateHolder
        implements CDProtocol, EDProtocol {

    /**
     * The node protocol variable name in the simulation config file.
     */
    private static final String PAR_NODE_PROTOCOL = "nodeprotocol";

    /**
     * The node protocol id.
     */
    private final int nodePid;

    public CommunityProtocol(String prefix){
        nodePid = Configuration.getPid(prefix + "." + PAR_NODE_PROTOCOL);
    }

    //--------------------------------------------------------------------------
    // INTERFACE
    //--------------------------------------------------------------------------

    @Override
    public void nextCycle(Node node, int pid) {
        FogNode fogNode = (FogNode) node;
        for(Community community : fogNode.getCommunities()) {
            communityMemberBehaviour.cyclicBehavior((FogNode) node, community, nodePid, pid);
            if (community.isLeader((FogNode) node)) {
                CommunityLeaderBehavior leaderBehavior = communityLeaderBehavior.get(community.getId());
                leaderBehavior.cyclicBehavior((FogNode) node, pid);
            }
        }
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        CommunityMessage msg = (CommunityMessage) event;
        switch (msg.getCode()){
            case CommunityMessage.MBR_MON_MSG:
                processMemberMessage((FogNode) node, (MemberMessage) msg, pid);
                break;
            case CommunityMessage.LDR_PLAN_MSG:
                LeaderMessage leaderMessage = (LeaderMessage) msg;
                processLeaderMessage(node, leaderMessage, pid);
                break;
            default:
                break;
        }
    }

    //--------------------------------------------------------------------------
    // INTERNAL BEHAVIOR
    //--------------------------------------------------------------------------

    private void processMemberMessage(FogNode node, MemberMessage memberMessage, int pid) {
        Community community = FogTopology.getCommunity(memberMessage.communityId);
        MemberState memberState = getMemberState(community.getId());
        memberState.updateNodeServiceWorkload(memberMessage.getSender(), memberMessage.getLocalServiceWorkloadHistory());
        memberState.storeNodeWorkloadAllocation(memberMessage.getSender(), memberMessage.getWorkloadAllocation());
        memberState.incMonitoringCount(community.getId());
        if(isAllMonitoringReceived(community, memberState.getMonitoringCount(community.getId()))) {
            CommunityLeaderBehavior leaderBehavior = communityLeaderBehavior.get(community.getId());
            leaderBehavior.analyze(node, pid);
            leaderBehavior.plan(node, community, pid);
            memberState.resetMonitoringCount(community.getId());
        }
    }

    private boolean isAllMonitoringReceived(Community community, int monitoringCount){
        return community.size() == monitoringCount;
    }

    private void processLeaderMessage(Node node, LeaderMessage leaderMessage, int pid){
        Map<Service, AggregateServiceAllocation> placementAllocation = (Map<Service, AggregateServiceAllocation>) leaderMessage.getContent();
        communityMemberBehaviour.execute(placementAllocation, node, nodePid);
    }
}
