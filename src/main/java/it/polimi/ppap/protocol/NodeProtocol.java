package it.polimi.ppap.protocol;

import it.polimi.deib.ppap.node.NodeFacade;
import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.common.scheme.ServiceDemand;
import it.polimi.ppap.generator.initializer.ServiceDemandGenerator;
import it.polimi.ppap.generator.workload.ServiceRequestGenerator;
import org.jfree.chart.axis.Tick;
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
        getOptimalAllocationFromControl();
        updateDemand();
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {

    }

    public void setPlacementAllocation(Map<Service, Float> placementAllocation){
        updatePlacementAllocationInControl(placementAllocation);
    }

//--------------------------------------------------------------------------
// INTERNAL
//--------------------------------------------------------------------------

    private void updatePlacementAllocationInControl(Map<Service, Float> placementAllocation){
        ServiceRequestGenerator serviceRequestGenerator = getServiceRequestGenerator();
        for(Service service : placementAllocation.keySet()){
            if(placementAllocation.get(service) > 0 && !nodeFacade.isServing(service)) {
                placeServiceOnThisNode(new Service(service), serviceRequestGenerator, placementAllocation);
            }else if(placementAllocation.get(service) == 0 && nodeFacade.isServing(service)) {
                removeServiceFromThisNode(service, serviceRequestGenerator);
            }
        }
    }

    private void placeServiceOnThisNode(Service service, ServiceRequestGenerator serviceRequestGenerator, Map<Service,Float> placementAllocation) {
        System.out.println("########### Placing Service " + service.getId() + " Onto Node ##############");
        float allocation  = placementAllocation.get(service);
        float initialDemand = service.getSLA() * 5; //TODO interarrival time twice the SLA
        service.setTargetAllocation(allocation);
        nodeFacade.addService(service);
        Map.Entry<Float, Float> demandAllocation = new AbstractMap.SimpleEntry<>(initialDemand, allocation);
        currentDemandAllocation.put(service, demandAllocation);
        ServiceDemand serviceDemand = new ServiceDemand(service, initialDemand);
        serviceRequestGenerator.activateDemandForService(serviceDemand);
    }

    private void removeServiceFromThisNode(Service service, ServiceRequestGenerator serviceRequestGenerator) {
        System.out.println("########### Removing Service " + service.getId() + " From Node ##############");
        serviceRequestGenerator.disableDemandForService(service);
        currentDemandAllocation.remove(service);
        nodeFacade.removeService(service);
    }

    private void getOptimalAllocationFromControl(){
        for(Service service : currentDemandAllocation.keySet()){
            if(nodeFacade.isServing(service)) {
                float optimalAllocation = nodeFacade.getLastOptimalAllocation(service);
                optimalAllocation = Math.max(0, optimalAllocation);
                Map.Entry<Float, Float> demandAllocation = currentDemandAllocation.get(service);
                demandAllocation.setValue(optimalAllocation);
            }
        }
    }

    private void updateDemand(){
        for(Service service : currentDemandAllocation.keySet()){
            if(nodeFacade.isServing(service)) {
                Map.Entry<Float, Float> demandAllocation = currentDemandAllocation.get(service);
                float currentDemand = demandAllocation.getKey();
                //TODO
                float nextDemand = new ServiceDemandGenerator((int) (currentDemand * 0.8), (int) (currentDemand * 1.2)).nextDemand();
                demandAllocation = new AbstractMap.SimpleEntry<>(currentDemand, demandAllocation.getValue());
                currentDemandAllocation.put(service, demandAllocation);
                ServiceDemand serviceDemand = new ServiceDemand(service, currentDemand);
                serviceRequestGenerator.updateDemandForService(serviceDemand);
            }
        }
    }
}
