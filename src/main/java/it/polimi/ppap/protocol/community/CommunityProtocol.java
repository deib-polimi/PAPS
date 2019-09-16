package it.polimi.ppap.protocol.community;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.service.AggregateServiceAllocation;
import it.polimi.ppap.service.ServiceCatalog;
import it.polimi.ppap.service.ServiceWorkload;
import it.polimi.ppap.topology.FogTopology;
import it.polimi.ppap.topology.community.Community;
import it.polimi.ppap.topology.node.FogNode;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
        for(Community community : FogTopology.getCommunities()) {
            communityMemberBehaviour.cyclicBehavior((FogNode) node, community, nodePid, pid);
            if (community.isLeader((FogNode) node)) {
                communityLeaderBehavior.cyclicBehavior((FogNode) node, pid);
            }
        }
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        CommunityMessage msg = (CommunityMessage) event;
        switch (msg.getCode()){
            case CommunityMessage.MBR_MON_MSG:
                processMemberMessage((FogNode) node, pid, msg);
                break;
            case CommunityMessage.LDR_PLAN_MSG:
                LeaderMessage leaderMessage = (LeaderMessage) msg;
                processLeaderMessage(leaderMessage, node, pid);
                break;
            default:
                break;
        }
    }

    //--------------------------------------------------------------------------
    // INTERNAL BEHAVIOR
    //--------------------------------------------------------------------------

    private void processMemberMessage(FogNode node, int pid, CommunityMessage msg) {
        MemberMessage memberMessage = (MemberMessage) msg;
        Community community = FogTopology.getCommunity(memberMessage.communityId);
        updateNodeServiceWorkload(memberMessage.getSender(), memberMessage.getLocalServiceWorkloadHistory());
        storeNodeWorkloadAllocation(memberMessage.getSender(), memberMessage.getWorkloadAllocation());
        incMonitoringCount(community.getId());
        if(isAllMonitoringReceived(community, getMonitoringCount(community.getId()))) {
            communityLeaderBehavior.analyze(node, pid);
            communityLeaderBehavior.plan(node, pid);
            resetMonitoringCount(community.getId());
        }
    }

    private void updateNodeServiceWorkload(FogNode sender, Map<Service, Set<ServiceWorkload>> localServiceWorkloadHistory){
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

    private void storeNodeWorkloadAllocation(FogNode sender, Map<Service, Map.Entry<Float, Float>> currentWorkloadAllocation){
        for(Service service : currentWorkloadAllocation.keySet()) {
            if(!workloadAllocationHistory.containsKey(service))
                workloadAllocationHistory.put(service, new TreeMap<>());
            Map.Entry<Float, Float> workloadAllocation = currentWorkloadAllocation.get(service);
            if(workloadAllocation.getValue() > 0)
                workloadAllocationHistory.get(service).put(1 / workloadAllocation.getKey(), workloadAllocation.getValue());
        }
    }

    private boolean isAllMonitoringReceived(Community community, int monitoringCount){
        return community.size() == monitoringCount;
    }

    private void processLeaderMessage(LeaderMessage leaderMessage, Node node, int pid){
        Map<Service, AggregateServiceAllocation> placementAllocation = (Map<Service, AggregateServiceAllocation>) leaderMessage.getContent();
        communityMemberBehaviour.execute(placementAllocation, node, nodePid);
    }
}
