package it.polimi.ppap.protocol;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.service.AggregateServiceDemand;
import it.polimi.ppap.transport.CommunityMessage;
import it.polimi.ppap.transport.LeaderMessage;
import it.polimi.ppap.transport.MemberMessage;
import it.polimi.ppap.topology.FogNode;
import it.polimi.ppap.solver.OplModSolver;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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
    // INTERFACE
    //--------------------------------------------------------------------------

    @Override
    public void nextCycle(Node node, int protocolID) {
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
                execute((Map<Service, AggregateServiceDemand>) leaderMessage.getContent(), node, pid);
                break;
            default:
                break;
        }
    }

    //--------------------------------------------------------------------------
    // METHODS
    //--------------------------------------------------------------------------

    // -----------------
    // LEADER's BEHAVIOR

    private void performLeaderBehavior(Node node, int pid){
        coordinateSharedMemberCapacityAllocation(node, pid);
    }

    private void coordinateSharedMemberCapacityAllocation(Node node, int pid){
        //System.out.println("Performing shared member capacity allocation");
    }

    private void analyze(FogNode node, int pid){
        //System.out.println("Performing ANALYSIS activity");
        for(Service service : workloadAllocationHistory.keySet()){
            if(workloadAllocationHistory.get(service).size() > 2) {
                double x[] = workloadAllocationHistory.get(service).keySet().stream().mapToDouble(e -> (double) 1/e).sorted().toArray();
                double y[] = workloadAllocationHistory.get(service).values().stream().mapToDouble(e -> (double) 1/e).sorted().toArray();
                System.out.println(x);
                System.out.println(y);
                UnivariateInterpolator interpolator = new SplineInterpolator();
                UnivariateFunction function = interpolator.interpolate(x, y);
                double currentWorkload = workloadAllocationHistory.get(service).keySet().stream().mapToDouble(e -> (double) e).average().getAsDouble();
                double interpolatedAllocation = function.value(1 / currentWorkload);
                System.out.println("##############################f(" + 1 / currentWorkload + ") = " + interpolatedAllocation);
            }else{

            }
        }
        plan(node, pid);
    }

    private void plan(FogNode node, int pid){
        //System.out.println("Performing the PLAN activity");
        solvePlacementAllocation();
        sendPlanToMembers(node, pid);
    }

    private void processMemberMessage(FogNode node, int pid, CommunityMessage msg) {
        MemberMessage memberMessage = (MemberMessage) msg;
        storeWorkloadAllocation(memberMessage.getContent(), msg.getSender(), node, pid);
        incMonitoringCount();
        if(isAllMonitoringReceived(getMonitoringCount(), node, pid)) {
            analyze(node, pid);
            resetMonitoringCount();
        }

    }

    private void storeWorkloadAllocation(Map<Service, Map.Entry<Float, Float>> currentWorkloadAllocation, Node sender, Node node, int pid){
        for(Service service : currentWorkloadAllocation.keySet()) {
            if(!workloadAllocationHistory.containsKey(service))
                workloadAllocationHistory.put(service, new TreeMap<>());
            Map.Entry<Float, Float> workloadAllocation = currentWorkloadAllocation.get(service);
            workloadAllocationHistory.get(service).put(workloadAllocation.getKey(), workloadAllocation.getValue());
        }
    }

    //TODO: only works if the leader has a connection to all members, which is expected; can improve it?
    private boolean isAllMonitoringReceived(int monitoringCount, Node node, int pid){
        Linkable linkable =
                (Linkable) node.getProtocol(FastConfig.getLinkable(pid));
        return linkable.degree() + 1 == monitoringCount;
    }

    //TODO async system call to CPLEX solver; otherwise complex optimization will make the simulation to stop
    private void solvePlacementAllocation(){
        OplModSolver oplModSolver = new OplModSolver();
        oplModSolver.generateData(getNodeServiceDemand());
        setNodeServiceAllocation(oplModSolver.solve(getNodeServiceDemand()));
    }

    private void sendPlanToMembers(FogNode node, int pid){
        Linkable linkable =
                (Linkable) node.getProtocol(FastConfig.getLinkable(pid));

        Map<Service, AggregateServiceDemand> leaderServiceAllocation = getNodeServiceAllocation().get(node);
        execute(leaderServiceAllocation, node, pid);
        for(int i = 0; i < linkable.degree(); i++){
            Node member = linkable.getNeighbor(i);
            Map<Service, AggregateServiceDemand> memberServiceAllocation = getNodeServiceAllocation().get(member);
             ((Transport) node.getProtocol(FastConfig.getTransport(pid))).
                    send(
                        node,
                        member,
                        new LeaderMessage(node, memberServiceAllocation),
                        pid);
        }
    }

    // -----------------
    // MEMBER's BEHAVIOR

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

    // -----------------
    //MAPE: EXECUTION

    private void execute(Map<Service, AggregateServiceDemand> placementAllocation, Node node, int pid){
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
