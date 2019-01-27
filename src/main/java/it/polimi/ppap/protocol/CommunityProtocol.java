package it.polimi.ppap.protocol;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.common.communication.CommunityMessage;
import it.polimi.ppap.common.communication.LeaderMessage;
import it.polimi.ppap.common.communication.MemberMessage;
import it.polimi.ppap.model.FogNode;
import it.polimi.ppap.solver.OplModSolver;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

import java.util.Map;

public class CommunityProtocol
        extends MemberStateHolder
        implements CDProtocol, EDProtocol {

    private static final String PAR_NODE_PROTOCOL = "nodeprotocol";

//--------------------------------------------------------------------------
// Initialization
//--------------------------------------------------------------------------

    private final int nodePid;

    public CommunityProtocol(String prefix){
        nodePid = Configuration.getPid(prefix + "." + PAR_NODE_PROTOCOL);
    }

//--------------------------------------------------------------------------
// METHODS
//--------------------------------------------------------------------------

    @Override
    public void nextCycle(Node node, int protocolID) {
        System.out.println("COMMUNITY PROTOCOL CYCLE");
        performMemberBehavior(node, protocolID);
        if(isLeader()) {
            performLeaderBehavior(node, protocolID);
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
                execute((Map<Service, Float>) leaderMessage.getContent(), node, pid);
                break;
            default:
                break;
        }
    }

    // LEADER BEHAVIOR

    private void processMemberMessage(FogNode node, int pid, CommunityMessage msg) {
        MemberMessage memberMessage = (MemberMessage) msg;
        storeMonitoredDemand(memberMessage.getContent(), msg.getSender(), node, pid);
        System.out.println("Member Monitoring Message Received: " + monitoringCount);
        incMonitoringCount();
        if(isAllMonitoringReceived(getMonitoringCount(), node, pid)) {
            analyze(node, pid);
            plan(node, pid);
            sendPlanToMembers(node, pid);
            resetMonitoringCount();
        }

    }

    private void performLeaderBehavior(Node node, int pid){
        coordinateSharedMemberCapacityAllocation(node, pid);
    }

    private void coordinateSharedMemberCapacityAllocation(Node node, int pid){
        //System.out.println("Performing shared member capacity allocation");
    }

    private void storeMonitoredDemand(Map<Service, Map.Entry<Float, Float>> currentDemandAllocation, Node sender, Node node, int pid){
        for(Service service : currentDemandAllocation.keySet()) {
            Map.Entry<Float, Float> demandAllocation = currentDemandAllocation.get(service);
            float currentDemand = demandAllocation.getValue();
            //System.out.println("######### Current Demand from " + sender + " for service " + service + ": " + currentDemand);
            getNodeServiceDemand().get(sender).put(service, currentDemand);
        }
    }

    private void analyze(Node node, int pid){
        //System.out.println("Performing ANALYSIS activity");
    }

    //TODO: only works in a fully connected topology
    private boolean isAllMonitoringReceived(int monitoringCount, Node node, int pid){
        Linkable linkable =
                (Linkable) node.getProtocol(FastConfig.getLinkable(pid));
        return linkable.degree() + 1 == monitoringCount;
    }

    private void plan(FogNode node, int pid){
        //System.out.println("Performing the PLAN activity");
        solvePlacementAllocation(node, pid);
        sendPlanToMembers(node, pid);
    }

    //TODO async system call to CPLEX solver;
    private void solvePlacementAllocation(Node node, int pid){
        OplModSolver oplModSolver = new OplModSolver();
        oplModSolver.generateData(getNodeServiceDemand());
        setNodeServiceAllocation(oplModSolver.solve(getNodeServiceDemand()));
    }

    private void sendPlanToMembers(FogNode node, int pid){
        Linkable linkable =
                (Linkable) node.getProtocol(FastConfig.getLinkable(pid));

        Map<Service, Float> leaderServiceAllocation = getNodeServiceAllocation().get(node);
        execute(leaderServiceAllocation, node, pid);
        for(int i = 0; i < linkable.degree(); i++){
            Node member = linkable.getNeighbor(i);
            Map<Service, Float> memberServiceAllocation = getNodeServiceAllocation().get(member);
             ((Transport) node.getProtocol(FastConfig.getTransport(pid))).
                    send(
                        node,
                        member,
                        new LeaderMessage(node, memberServiceAllocation),
                        pid);
        }
    }

    // MEMBERS BEHAVIOR

    private void performMemberBehavior(Node node, int pid){
        monitor(node, pid);
    }

    //MAPE: MONITORING
    private void monitor(Node node, int pid){
        //System.out.println("Performing the MONITOR activity");
        NodeProtocol nodeProtocol = (NodeProtocol) node.getProtocol(nodePid);
        Map<Service, Map.Entry<Float, Float>> demandAllocation = nodeProtocol.getCurrentWorkloadAllocation();
        try {
            sendMonitoredDataToLeader(demandAllocation, node, pid);
        } catch (CommunityLeaderNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendMonitoredDataToLeader(Map<Service, Map.Entry<Float, Float>> demandAllocation,
                                           Node node, int pid) throws CommunityLeaderNotFoundException {
        Node communityLeader = getCommunityLeader(node, pid);
        ((Transport) node.getProtocol(FastConfig.getTransport(pid))).
                send(
                    node,
                    communityLeader,
                    new MemberMessage(node, demandAllocation),
                    pid);
    }

    //MAPE: EXECUTION
    private void execute(Map<Service, Float> placementAllocation, Node node, int pid){
        //System.out.println("Performing the EXECUTE activity");
        NodeProtocol nodeProtocol = (NodeProtocol) node.getProtocol(nodePid);
        nodeProtocol.updatePlacementAllocation(placementAllocation);
    }


    private Node getCommunityLeader(Node node, int pid) throws CommunityLeaderNotFoundException {
        Linkable linkable =
                (Linkable) node.getProtocol(FastConfig.getLinkable(pid));
        if(isLeader())
            return node;
        else {
            for (int i = 0; i < linkable.degree(); i++) {
                Node member = linkable.getNeighbor(i);
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
}
