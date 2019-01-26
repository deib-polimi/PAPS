package it.polimi.ppap.protocol;

import it.polimi.deib.ppap.node.NodeFacade;
import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.generator.workload.ServiceRequestGenerator;
import peersim.cdsim.CDProtocol;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

import java.util.Map;

public class NodeProtocol
    extends NodeStateHolder
    implements CDProtocol, EDProtocol {

    public NodeProtocol(String prefix){
    }

//--------------------------------------------------------------------------
// INTERFACE
//--------------------------------------------------------------------------

    @Override
    public void nextCycle(Node node, int protocolID) {
        updateCurrentDemandInControl(getCurrentDemand());
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {

    }

    //TODO
    public Object getDemandAllocation(){
        return getDemandAllocationFromControl();
    }

    public void setPlacementAllocation(Map<Service, Float> placementAllocation){
        updatePlacementAllocationInControl(placementAllocation);
    }

//--------------------------------------------------------------------------
// INTERNAL
//--------------------------------------------------------------------------

    private void updatePlacementAllocationInControl(Map<Service, Float> placementAllocation){
        NodeFacade nodeFacade = getNodeFacade();
        ServiceRequestGenerator serviceRequestGenerator = getServiceRequestGenerator();
        for(Service service : placementAllocation.keySet()){
            if(placementAllocation.get(service) > 0 && !nodeFacade.isServing(service)) {
                float allocation  = placementAllocation.get(service);
                service.setTargetAllocation(allocation);
                nodeFacade.addService(service);
                serviceRequestGenerator.activateDemandForService(service);
            }else if(placementAllocation.get(service) == 0 && nodeFacade.isServing(service)) {
                serviceRequestGenerator.disableDemandForService(service);
                nodeFacade.removeService(service);
            }
        }
    }

    private void updateCurrentDemandInControl(Object demand){
        NodeFacade nodeFacade = getNodeFacade();

    }

    //TODO
    private Object getDemandAllocationFromControl(){
        //get from the control system the current demand and allocation

        return null;
    }

}
