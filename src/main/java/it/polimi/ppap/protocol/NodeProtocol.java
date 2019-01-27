package it.polimi.ppap.protocol;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.common.scheme.ServiceWorkload;
import it.polimi.ppap.generator.initializer.ServiceWorkloadGenerator;
import it.polimi.ppap.generator.workload.ServiceRequestGenerator;
import org.apache.commons.math3.distribution.NormalDistribution;
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
        fluctuateWorkload();
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {}

    public void updatePlacementAllocation(Map<Service, Float> placementAllocation){
        ServiceRequestGenerator serviceRequestGenerator = getServiceRequestGenerator();
        for(Service service : placementAllocation.keySet()){
            if(placementAllocation.get(service) > 0 && !nodeFacade.isServing(service)) {
                placeServiceOnThisNode(new Service(service), serviceRequestGenerator, placementAllocation);
            }else if(placementAllocation.get(service) == 0 && nodeFacade.isServing(service)) {
                removeServiceFromThisNode(service, serviceRequestGenerator);
            }
        }
    }

//--------------------------------------------------------------------------
// INTERNAL
//--------------------------------------------------------------------------

    private void placeServiceOnThisNode(Service service, ServiceRequestGenerator serviceRequestGenerator, Map<Service,Float> placementAllocation) {
        System.out.println("########### Placing Service " + service.getId() + " Onto Node ##############");
        float allocation  = placementAllocation.get(service);
        service.setTargetAllocation(allocation);
        ServiceWorkloadGenerator serviceWorkloadGenerator = new ServiceWorkloadGenerator(service);
        float initialWorkload = serviceWorkloadGenerator.nextWorkload();
        nodeFacade.addService(service);
        Map.Entry<Float, Float> workloadAllocation = new AbstractMap.SimpleEntry<>(initialWorkload, allocation);
        currentWorkloadAllocation.put(service, workloadAllocation);
        ServiceWorkload serviceWorkload = new ServiceWorkload(service, initialWorkload);
        serviceRequestGenerator.activateWorkloadForService(serviceWorkload);
    }

    private void removeServiceFromThisNode(Service service, ServiceRequestGenerator serviceRequestGenerator) {
        System.out.println("########### Removing Service " + service.getId() + " From Node ##############");
        serviceRequestGenerator.disableWorkloadForService(service);
        currentWorkloadAllocation.remove(service);
        nodeFacade.removeService(service);
    }

    //TODO the optimal allocation may be a crazy value; we need to filter this value;
    //TODO also, the optimal allocation refers to the last one, which can be an outlier; we need the whole history
    private void fetchOptimalAllocationFromControl(){
        for(Service service : currentWorkloadAllocation.keySet()){
            if(nodeFacade.isServing(service)) {
                float currentWorkload = currentWorkloadAllocation.get(service).getKey();
                //System.out.println("######### Current Workload for " + service + ": " + currentWorkload);
                float optimalAllocation = nodeFacade.getLastOptimalAllocation(service);
                //System.out.println("######### Optimal Allocation for " + service + ": " + optimalAllocation);
                Map.Entry<Float, Float> workloadAllocation = currentWorkloadAllocation.get(service);
                if(currentWorkload > 0 && optimalAllocation > 0) {
                    workloadAllocation.setValue(optimalAllocation);
                }else if(currentWorkload == 0){
                    workloadAllocation.setValue(0f);
                }
            }
        }
    }

    //TODO it is not wise to sync this with the control tick for two reasons: i) makes it unrealistically easy for the CS; 2) workload fluctuation is not cyclic
    private void fluctuateWorkload(){
        for(Service service : currentWorkloadAllocation.keySet()){
            if(nodeFacade.isServing(service)) {
                Map.Entry<Float, Float> workloadAllocation = currentWorkloadAllocation.get(service);
                float currentWorkload = workloadAllocation.getKey();
                //TODO varying the interarrival time with average twice the SLA and STD = SLA
                NormalDistribution normalDistribution = new NormalDistribution(service.getSLA() * 2, service.getSLA());
                float nextWorkload = (float) normalDistribution.sample();
                workloadAllocation = new AbstractMap.SimpleEntry<>(nextWorkload, workloadAllocation.getValue());
                currentWorkloadAllocation.put(service, workloadAllocation);
                ServiceWorkload serviceWorkload = new ServiceWorkload(service, currentWorkload);
                serviceRequestGenerator.updateWorkloadForService(serviceWorkload);
            }
        }
    }
}
