package it.polimi.ppap.protocol;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import it.polimi.ppap.common.communication.CommunityMessage;
import it.polimi.ppap.common.communication.LeaderMessage;
import it.polimi.ppap.common.communication.MemberMessage;
import it.polimi.ppap.solver.OplModSolver;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                processMemberMessage(node, pid, msg);
                break;
            case CommunityMessage.LDR_PLAN_MSG:
                LeaderMessage leaderMessage = (LeaderMessage) msg;
                execute(leaderMessage.getContent(), node, pid);
                break;
            default:
                break;
        }
    }

// LEADER BEHAVIOR

    private void processMemberMessage(Node node, int pid, CommunityMessage msg) {
        MemberMessage memberMessage = (MemberMessage) msg;
        storeMonitoredDemand(memberMessage.getContent(), msg.getSender(), node, pid);
        incMonitoringCount();
        if(isAllMonitoringReceived(getMonitoringCount(), node, pid)) {
            analyze(node, pid);
            plan(node, pid);
            sendPlanToMembers(getPlacementAllocationSchema(), node, pid);
            resetMonitoringCount();
        }

    }

    private void performLeaderBehavior(Node node, int pid){
        coordinateSharedMemberCapacityAllocation(node, pid);
    }

    private void coordinateSharedMemberCapacityAllocation(Node node, int pid){
        //System.out.println("Performing shared member capacity allocation");

    }

    private void storeMonitoredDemand(Object demand, Node sender, Node node, int pid){

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

    private void plan(Node node, int pid){
        //System.out.println("Performing the PLAN activity");
        Object placementAllocation = solvePlacementAllocation(node, pid);
        sendPlanToMembers(placementAllocation, node, pid);
    }

    //TODO async system call to CPLEX solver;
    private Object solvePlacementAllocation(Node node, int pid){
        OplModSolver oplModSolver = new OplModSolver();
        oplModSolver.generateData(getNodeServiceDemand());
        setNodeServiceAllocation(oplModSolver.solve(getNodeServiceDemand()));
        return new Object();
    }

    private void sendPlanToMembers(Object placementAllocation, Node node, int pid){
        Linkable linkable =
                (Linkable) node.getProtocol(FastConfig.getLinkable(pid));
        for(int i = 0; i < linkable.degree(); i++){
            Node member = linkable.getNeighbor(i);
            ((Transport) node.getProtocol(FastConfig.getTransport(pid))).
                    send(
                        node,
                        member,
                        new LeaderMessage(node, placementAllocation),
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
        Object demandAllocation = nodeProtocol.getDemandAllocation();
        try {
            sendMonitoredDataToLeader(demandAllocation, node, pid);
        } catch (CommunityLeaderNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendMonitoredDataToLeader(Object demandAllocation, Node node, int pid) throws CommunityLeaderNotFoundException {
        Node communityLeader = getCommunityLeader(node, pid);
        ((Transport) node.getProtocol(FastConfig.getTransport(pid))).
                send(
                    node,
                    communityLeader,
                    new MemberMessage(node, demandAllocation),
                    pid);
    }

    //MAPE: EXECUTION
    private void execute(Object placementAllocation, Node node, int pid){
        //System.out.println("Performing the EXECUTE activity");
        NodeProtocol nodeProtocol = (NodeProtocol) node.getProtocol(nodePid);
        nodeProtocol.setPlacementAllocation(placementAllocation);
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
