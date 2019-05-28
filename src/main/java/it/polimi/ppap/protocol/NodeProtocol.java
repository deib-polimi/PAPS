package it.polimi.ppap.protocol;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.service.AggregateServiceAllocation;
import it.polimi.ppap.service.ServiceAllocation;
import it.polimi.ppap.service.ServiceWorkload;
import it.polimi.ppap.random.initializer.ServiceWorkloadGenerator;
import it.polimi.ppap.service.ServiceWorkloadFraction;
import it.polimi.ppap.topology.FogNode;
import peersim.cdsim.CDProtocol;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

import java.util.AbstractMap;
import java.util.Map;

public class NodeProtocol
    extends NodeStateHolder
    implements CDProtocol, EDProtocol {

    public NodeProtocol(String prefix){}

//--------------------------------------------------------------------------
// INTERFACE
//--------------------------------------------------------------------------

    @Override
    public void nextCycle(Node node, int protocolID) {

    }

    @Override
    public void afterTick() {
        fetchOptimalAllocationFromControl();
        synchronized (CommonState.r) {
            CommonState.r.notify();
        }
        //fluctuateWorkload();
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {}

    public void updatePlacementAllocation(Map<Service, AggregateServiceAllocation> placementAllocation, int nodePID){
        for(Service service : placementAllocation.keySet())
            if(placementAllocation.get(service).getAggregateAllocation() > 0) {
                if (!nodeFacade.isServing(service))
                    placeServiceOnThisNode(new Service(service), placementAllocation, nodePID);
                else
                    updateServiceAllocation(service, placementAllocation, nodePID);
            }else if(nodeFacade.isServing(service)) {
                removeServiceFromThisNode(service);
        }
    }

//--------------------------------------------------------------------------
// INTERNAL
//--------------------------------------------------------------------------

    private void placeServiceOnThisNode(final Service service,
                                        Map<Service,AggregateServiceAllocation> placementAllocation,
                                        final int nodePID) {
        System.out.println("########### Placing Service " + service.getId() + " Onto Node ##############");
        nodeFacade.addService(service);
        updateServiceAllocation(service, placementAllocation, nodePID);
        placementAllocation.get(service).stream().filter(e -> e.getAllocation() > 0).forEach(serviceDemand -> {
            activateWorkloadForDemandFraction(service, nodePID, serviceDemand);
        });
    }

    private void updateServiceAllocation(Service service, Map<Service, AggregateServiceAllocation> placementAllocation, int nodePID) {
        float allocation  = placementAllocation.get(service).getAggregateAllocation();
        nodeFacade.setTargetAllocation(service, allocation);
    }

    private void activateWorkloadForDemandFraction(Service service, int nodePID, ServiceAllocation serviceDemand) {
        FogNode source  = serviceDemand.getSource();
        NodeProtocol sourceProt = (NodeProtocol) source.getProtocol(nodePID);
        ServiceWorkload serviceWorkload = sourceProt.getLocalServiceWorkload().get(service);
        float workloadFraction = serviceDemand.getFraction();
        serviceRequestGenerator.activateServiceWorkload(new ServiceWorkloadFraction(serviceWorkload, workloadFraction));
        System.out.println("########### Activated Workload for " + service.getId() + ": " + serviceWorkload.getWorkload() + " ##############");
    }

    private void removeServiceFromThisNode(Service service) {
        System.out.println("########### Removing Service " + service.getId() + " From Node ##############");
        serviceRequestGenerator.disableServiceWorkload(service);
        currentWorkloadAllocation.remove(service);
        nodeFacade.removeService(service);
    }

    //TODO the optimal allocation may be a crazy value; we need to filter this value;
    //TODO also, the optimal allocation refers to the last one, which can be an outlier; we need the whole history
    private void fetchOptimalAllocationFromControl(){
        serviceRequestGenerator.forEach(service -> {
            if(nodeFacade.isServing(service)) {
                float currentWorkload = serviceRequestGenerator.getAggregateWorkload(service);
                //System.out.println("######### Current Workload for " + service + ": " + currentWorkload);
                float optimalAllocation = nodeFacade.getLastOptimalAllocation(service);
                //TODO this heuristic must be assessed
//                optimalAllocation = Math.min(service.getTargetAllocation() * 2, optimalAllocation);
                if(optimalAllocation > 0) {
                    //System.out.println("######### Optimal Allocation for " + service + ": " + optimalAllocation);
                    //TODO here we should add to a history set containing multiple workload-allocation pairs for later analysis
                    if (currentWorkload > 0 && optimalAllocation > 0) {
                        Map.Entry<Float, Float> workloadAllocation = new AbstractMap.SimpleEntry<>(currentWorkload, optimalAllocation);
                        currentWorkloadAllocation.put(service, workloadAllocation);
                    }
                }
            }
        });
    }
}
