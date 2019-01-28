package it.polimi.ppap.protocol;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.service.AggregateServiceDemand;
import it.polimi.ppap.service.ServiceDemand;
import it.polimi.ppap.service.ServiceWorkload;
import it.polimi.ppap.generator.initializer.ServiceWorkloadGenerator;
import peersim.cdsim.CDProtocol;
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
        //fluctuateWorkload();
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {}

    public void updatePlacementAllocation(Map<Service, AggregateServiceDemand> placementAllocation){
        for(Service service : placementAllocation.keySet()){
            if(placementAllocation.containsKey(service) && !nodeFacade.isServing(service)) {
                if(placementAllocation.get(service).getAggregateDemand() > 0)
                    placeServiceOnThisNode(new Service(service), placementAllocation);
            }else if(!placementAllocation.containsKey(service) && nodeFacade.isServing(service)) {
                removeServiceFromThisNode(service);
            }
        }
    }

//--------------------------------------------------------------------------
// INTERNAL
//--------------------------------------------------------------------------

    private void placeServiceOnThisNode(final Service service,
                                        Map<Service,AggregateServiceDemand> placementAllocation) {
        System.out.println("########### Placing Service " + service.getId() + " Onto Node ##############");
        float allocation  = placementAllocation.get(service).getAggregateDemand();
        service.setTargetAllocation(allocation);
        nodeFacade.addService(service);
        //float workload = serviceRequestGenerator.getAggregateWorkload(service);
        //Map.Entry<Float, Float> workloadAllocation = new AbstractMap.SimpleEntry<>(workload, allocation);
        //currentWorkloadAllocation.put(service, workloadAllocation);
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

    //TODO it is not wise to sync this with the control tick for two reasons: i) makes it unrealistically easy for the CS; 2) workload fluctuation is not cyclic
    private void fluctuateWorkload(){
        serviceRequestGenerator.forEach(service -> {
            if(nodeFacade.isServing(service)) {
                Map.Entry<Float, Float> workloadAllocation = currentWorkloadAllocation.get(service);
                float currentWorkload = workloadAllocation.getKey();
                float std = service.getSLA() * 0.1f;
                ServiceWorkloadGenerator serviceWorkloadGenerator = new ServiceWorkloadGenerator(currentWorkload, std, 0.6f);
                float nextWorkload = serviceWorkloadGenerator.nextWorkload();
                System.out.println("########### Next Workload for " + service.getId() + ": " + nextWorkload + " ##############");
                workloadAllocation = new AbstractMap.SimpleEntry<>(nextWorkload, workloadAllocation.getValue());
                currentWorkloadAllocation.put(service, workloadAllocation);
                //ServiceWorkload serviceWorkload = new ServiceWorkload(service, currentWorkload);
                //serviceRequestGenerator.updateWorkloadForService(serviceWorkload);
            }
        });
    }
}
