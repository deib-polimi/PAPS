package it.polimi.ppap.protocol;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.service.AggregateServiceAllocation;
import it.polimi.ppap.service.ServiceCatalog;
import it.polimi.ppap.service.ServiceWorkload;
import it.polimi.ppap.topology.FogNode;
import it.polimi.ppap.transport.CommunityMessage;
import it.polimi.ppap.transport.LeaderMessage;
import it.polimi.ppap.transport.MemberMessage;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Linkable;
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
        communityMemberBehaviour.monitor((FogNode) node, nodePid, pid);
        if(isLeader()) {
            communityLeaderBehaviour.cyclicBehavior((FogNode) node, pid);
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
                processLeaderMessage((Map<Service, AggregateServiceAllocation>) leaderMessage.getContent(), node, pid);
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
        updateNodeServiceWorkload(memberMessage.getSender(), memberMessage.getLocalServiceWorkloadHistory());
        storeNodeWorkloadAllocation(memberMessage.getSender(), memberMessage.getWorkloadAllocation());
        incMonitoringCount();
        if(isAllMonitoringReceived(getMonitoringCount(), node, pid)) {
            communityLeaderBehaviour.analyze(node, pid);
            communityLeaderBehaviour.plan(node, pid);
            resetMonitoringCount();
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

    //TODO: only works if the leader has a connection to all members, which is expected; can improve it?
    private boolean isAllMonitoringReceived(int monitoringCount, Node node, int pid){
        Linkable linkable =
                (Linkable) node.getProtocol(FastConfig.getLinkable(pid));
        return linkable.degree() + 1 == monitoringCount;
    }

    private void processLeaderMessage(Map<Service, AggregateServiceAllocation> placementAllocation, Node node, int pid){
        communityMemberBehaviour.execute(placementAllocation, node, nodePid);
    }

}
