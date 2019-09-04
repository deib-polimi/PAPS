package it.polimi.ppap.protocol.community;

import it.polimi.deib.ppap.node.NodeFacade;
import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.service.AggregateServiceAllocation;
import it.polimi.ppap.service.ServiceCatalog;
import it.polimi.ppap.service.ServiceWorkload;
import it.polimi.ppap.solver.OplModSolver;
import it.polimi.ppap.topology.FogNode;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.transport.Transport;

import java.util.Map;
import java.util.Set;
import java.util.stream.DoubleStream;

public class CommunityLeaderBehaviour {

    final MemberStateHolder memberStateHolder;

    public CommunityLeaderBehaviour(MemberStateHolder memberStateHolder) {
        this.memberStateHolder = memberStateHolder;
    }

    /**
     * By default, the leader behaves in a reactive manner, namely after all community members have sent their message.
     * Additional cyclic behavior can be added in this method. Note that there is no guarantees this method will be
     * called before or after community members.
     * @param node
     * @param pid
     */
    public void cyclicBehavior(FogNode node, int pid){}

    /**
     * The leader's Analysis as part of the community-level MAPE loop. It consists of the aggregation of the workload
     * for a given service from various sources within the community. The aggregate (allocation) demand is then obtained
     * from the aggregate workload.
     * @param node
     * @param pid
     */
    public void analyze(
                        final FogNode node,
                        final int pid)
    {
        memberStateHolder.getNodeServiceDemand().clear();
        for(FogNode member : memberStateHolder.getNodeServiceWorkload().keySet()){
            for(Service service : ServiceCatalog.getServiceCatalog()) {
                if(memberStateHolder.getNodeServiceWorkload().get(member).containsKey(service)) {
                    float aggregateWorkload = getAverageWorkload(memberStateHolder.getNodeServiceWorkload().get(member).get(service));
                    if(aggregateWorkload > 0)
                        updateDemandFromStaticAllocation(
                                member,
                                service,
                                aggregateWorkload);
                    else
                        memberStateHolder.updateServiceDemand(member, service, 0);
                }
            }
            memberStateHolder.getNodeServiceWorkload().get(member).clear();
        }
    }

    private float getAverageWorkload(Set<ServiceWorkload> serviceWorkloads){
        DoubleStream workloadStream = serviceWorkloads.stream().mapToDouble(e -> e.getWorkload());
        int total = serviceWorkloads.size();
        Double frequencySum = workloadStream.sum();
        return frequencySum.floatValue() / total;
    }

    private void updateDemandFromStaticAllocation(
                                final FogNode member,
                                final Service service,
                                final float workload)
    {
        final float demandFromMember = NodeFacade.getStaticAllocation(workload, service.getRT(), memberStateHolder.getReferenceControlPeriod());
        memberStateHolder.updateServiceDemand(member, service, demandFromMember);
    }

    /*private Map<Service, UnivariateFunction> buildServiceUnivariateFunctionMap() {
        Map<Service, UnivariateFunction> workloadDemandFunctionMap = new HashMap<>();
        for(Service service : ServiceCatalog.getServiceCatalog()){
            Optional<UnivariateFunction> workloadDemandFunction = createUnivariateFunction(workloadDemandFunctionMap, service);
            if(workloadDemandFunction.isPresent())
                workloadDemandFunctionMap.put(service, workloadDemandFunction.get());
        }
        return workloadDemandFunctionMap;
    }

    private Optional<UnivariateFunction> createUnivariateFunction(Map<Service, UnivariateFunction> workloadDemandFunctionMap, Service service) {
        if(workloadAllocationHistory.containsKey(service) && workloadAllocationHistory.get(service).size() > 2) {
            double x[] = workloadAllocationHistory.get(service).keySet().stream().mapToDouble(e -> (double) e).toArray();
            double y[] = workloadAllocationHistory.get(service).values().stream().mapToDouble(e -> (double) e).toArray();
            UnivariateInterpolator interpolator = new SplineInterpolator();
            return Optional.of(interpolator.interpolate(x, y));
        } else
            return Optional.empty();
    }

    private void updateDemandFromWorkload(ServiceWorkload serviceWorkload, UnivariateFunction workloadDemandFunction) {
        float workloadFromMember = 1 / serviceWorkload.getWorkload();
        FogNode member = serviceWorkload.getSource();
        Service service = serviceWorkload.getService();
        if(!memberStateHolder.getNodeServiceDemand().containsKey(member))
            memberStateHolder.getNodeServiceDemand().put(member, new HashMap<>());
        try {
            float demandFromMember = (float) workloadDemandFunction.value(workloadFromMember);
            if(demandFromMember > 0)
                updateServiceDemand(member, service, demandFromMember);
            else
                updateServiceDemand(member, service, 1f);
        }catch (OutOfRangeException exceptiothisn){
            float fitWorkload = estimateDemandFromNearestWorkload(service, member, workloadFromMember);
            float demandFromMember = (float) workloadDemandFunction.value(fitWorkload);
            updateServiceDemand(member, servthisice, demandFromMember);
        }
        System.out.println("Allocation demand from member " + service + ": " + memberStateHolder.getNodeServiceDemand().get(member).get(service));
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
    }*/

    /**
     * The leader's Plan activity as part of the community-level MAPE loop. It consists of solving the optimal placement
     * problem and advertising the plan to the community members.
     * @param node
     * @param pid
     */
    public void plan(FogNode node, int pid){
        solvePlacementAllocation();
        sendPlanToMembers(node, pid);
    }

    //TODO async system call to CPLEX solver; otherwise complex optimization will make the simulation to stop
    private void solvePlacementAllocation(){
        OplModSolver oplModSolver = new OplModSolver();
        oplModSolver.generateData(ServiceCatalog.getServiceCatalog(), memberStateHolder.getNodeServiceDemand(), memberStateHolder.getOptimizationBeta());
        try {
            memberStateHolder.setNodeServiceAllocation(oplModSolver.solve(memberStateHolder.getNodeServiceDemand(), false));
        } catch (OplModSolver.OplSolutionNotFoundException ex){
            memberStateHolder.setNodeServiceAllocation(oplModSolver.solve(memberStateHolder.getNodeServiceDemand(), true));
        }
    }

    private void sendPlanToMembers(FogNode node, int pid){
        Linkable linkable =
                (Linkable) node.getProtocol(FastConfig.getLinkable(pid));

        Map<Service, AggregateServiceAllocation> leaderServiceAllocation = memberStateHolder.getNodeServiceAllocation().get(node);
        ((Transport) node.getProtocol(FastConfig.getTransport(pid))).
                send(
                        node,
                        node,
                        new LeaderMessage(node, leaderServiceAllocation),
                        pid);
        for(int i = 0; i < linkable.degree(); i++){
            Node member = linkable.getNeighbor(i);
            Map<Service, AggregateServiceAllocation> memberServiceAllocation = memberStateHolder.getNodeServiceAllocation().get(member);
            ((Transport) node.getProtocol(FastConfig.getTransport(pid))).
                    send(
                            node,
                            member,
                            new LeaderMessage(node, memberServiceAllocation),
                            pid);
        }
    }
}
