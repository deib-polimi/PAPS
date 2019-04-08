package it.polimi.ppap.protocol;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.service.AggregateServiceDemand;
import it.polimi.ppap.service.ServiceWorkload;
import it.polimi.ppap.transport.CommunityMessage;
import it.polimi.ppap.transport.LeaderMessage;
import it.polimi.ppap.transport.MemberMessage;
import it.polimi.ppap.topology.FogNode;
import it.polimi.ppap.solver.OplModSolver;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.exception.OutOfRangeException;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

import java.util.*;

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
        performMemberBehavior((FogNode) node, protocolID);
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
        nodeServiceDemand.clear();
        Map<Service, UnivariateFunction> workloadDemandFunctionMap = buildServiceUnivariateFunctionMap();
        for(FogNode member : nodeServiceWorkload.keySet()){
            for(ServiceWorkload serviceWorkload : nodeServiceWorkload.get(member)){
                Service service = serviceWorkload.getService();
                if(serviceWorkload.getWorkload() > 0 && workloadDemandFunctionMap.containsKey(service)) //TODO isActive for god sake?
                    calculateDemandFromWorkload(serviceWorkload, workloadDemandFunctionMap.get(service));
                else
                    initializeDemand(serviceWorkload);
            }
        }
        plan(node, pid);
    }

    private Map<Service, UnivariateFunction> buildServiceUnivariateFunctionMap() {
        Map<Service, UnivariateFunction> workloadDemandFunctionMap = new HashMap<>();
        for(Service service : workloadAllocationHistory.keySet()){
            if(workloadAllocationHistory.containsKey(service) && workloadAllocationHistory.get(service).size() > 2) {
                double x[] = workloadAllocationHistory.get(service).keySet().stream().mapToDouble(e -> (double) e).toArray();
                double y[] = workloadAllocationHistory.get(service).values().stream().mapToDouble(e -> (double) e).toArray();
                UnivariateInterpolator interpolator = new SplineInterpolator();
                UnivariateFunction workloadDemandFunction = interpolator.interpolate(x, y);
                workloadDemandFunctionMap.put(service, workloadDemandFunction);
            }
        }
        return workloadDemandFunctionMap;
    }

    private void calculateDemandFromWorkload(ServiceWorkload serviceWorkload, UnivariateFunction workloadDemandFunction) {
        float workloadFromMember = 1 / serviceWorkload.getWorkload();
        FogNode member = serviceWorkload.getSource();
        Service service = serviceWorkload.getService();
        if(!nodeServiceDemand.containsKey(member))
            nodeServiceDemand.put(member, new HashMap<>());
        try {
            float demandFromMember = (float) workloadDemandFunction.value(workloadFromMember);
            if(demandFromMember > 0)
                nodeServiceDemand.get(member).put(service, demandFromMember);
            else
                nodeServiceDemand.get(member).put(service, 1f);
        }catch (OutOfRangeException exception){
            float fitWorkload = estimateDemandFromNearestWorkload(service, member, workloadFromMember);
            float demandFromMember = (float) workloadDemandFunction.value(fitWorkload);
            nodeServiceDemand.get(member).put(service, demandFromMember);
        }
        System.out.println("Allocation demand from member " + service + ": " + nodeServiceDemand.get(member).get(service));
    }

    private float estimateDemandFromNearestWorkload(Service service, FogNode member, final float unfitWorkload) {
        //TODO if is out of range, which value to use? perhaps the nearest one
        //float demandFromMember = (float) workloadAllocationHistory.get(service).values().stream().mapToDouble(e -> (double) e).max().getAsDouble();
        OptionalDouble optionalWorkload = workloadAllocationHistory.get(service).keySet().stream().filter(e -> e > unfitWorkload).mapToDouble(e-> e).min();
        float fitWorkload;
        if(optionalWorkload.isPresent())
            fitWorkload = (float) optionalWorkload.getAsDouble();
        else
            fitWorkload =  (float) workloadAllocationHistory.get(service).keySet().stream().filter(e -> e < unfitWorkload).mapToDouble(e-> e).max().getAsDouble();
        return fitWorkload;
    }

    private void initializeDemand(ServiceWorkload serviceWorkload){
        Service service = serviceWorkload.getService();
        FogNode member = serviceWorkload.getSource();
        Map<Service, Float> serviceDemand = nodeServiceDemand.getOrDefault(member, new TreeMap<>());
        float demandFromMember = serviceWorkload.getWorkload() > 0 ? 1 : 0;
        serviceDemand.put(service, demandFromMember);
        nodeServiceDemand.put(member, serviceDemand);
    }

    private void plan(FogNode node, int pid){
        //System.out.println("Performing the PLAN activity");
        solvePlacementAllocation();
        sendPlanToMembers(node, pid);
    }

    private void processMemberMessage(FogNode node, int pid, CommunityMessage msg) {
        MemberMessage memberMessage = (MemberMessage) msg;
        updateNodeServiceWorkload(memberMessage.getSender(), memberMessage.getLocalServiceWorkload());
        storeNodeWorkloadAllocation(memberMessage.getSender(), memberMessage.getWorkloadAllocation());
        incMonitoringCount();
        if(isAllMonitoringReceived(getMonitoringCount(), node, pid)) {
            analyze(node, pid);
            resetMonitoringCount();
        }
    }

    private void updateNodeServiceWorkload(FogNode sender, Map<Service, ServiceWorkload> localServiceWorkload){
        Set<ServiceWorkload> serviceWorkloads = new HashSet<>();
        for(Service service : localServiceWorkload.keySet())
            serviceWorkloads.add(localServiceWorkload.get(service));
        nodeServiceWorkload.put(sender, serviceWorkloads);
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

    private void performMemberBehavior(FogNode node, int pid){
        monitor(node, pid);
    }

    //MAPE: MONITORING
    private void monitor(FogNode node, int pid){
        //System.out.println("Performing the MONITOR activity");
        NodeProtocol nodeProtocol = (NodeProtocol) node.getProtocol(nodePid);
        Map<Service, ServiceWorkload> localServiceWorkload = nodeProtocol.getLocalServiceWorkload();
        Map<Service, Map.Entry<Float, Float>> currentDemandAllocation = nodeProtocol.getCurrentWorkloadAllocation();
        try {
            sendMonitoredDataToLeader(localServiceWorkload, currentDemandAllocation, node, pid);
        } catch (CommunityLeaderNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendMonitoredDataToLeader(Map<Service, ServiceWorkload> localServiceWorkload,
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

    // -----------------
    //MAPE: EXECUTION

    private void execute(Map<Service, AggregateServiceDemand> placementAllocation, Node node, int pid){
        //System.out.println("Performing the EXECUTE activity");
        NodeProtocol nodeProtocol = (NodeProtocol) node.getProtocol(nodePid);
        nodeProtocol.updatePlacementAllocation(placementAllocation, nodePid);
    }


    private FogNode getCommunityLeader(FogNode node, int pid) throws CommunityLeaderNotFoundException {
        Linkable linkable =
                (Linkable) node.getProtocol(FastConfig.getLinkable(pid));
        if(isLeader())
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
}
