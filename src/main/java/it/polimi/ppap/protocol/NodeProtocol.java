package it.polimi.ppap.protocol;

import it.polimi.deib.ppap.node.NodeFacade;
import peersim.cdsim.CDProtocol;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

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
        System.out.println("NODE PROTOCOL CYCLE");
        sendCurrentDemandToControl(getCurrentDemand());
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {

    }

    //TODO
    public Object getDemandAllocation(){
        return getDemandAllocationFromControl();
    }

    public void setPlacementAllocation(Object placementAllocation){
        sendPlacementAllocationToControl(placementAllocation);
    }

//--------------------------------------------------------------------------
// INTERNAL
//--------------------------------------------------------------------------

    private boolean isPlacementAllocationUpdated(){

        return false;
    }

    //TODO
    private void sendPlacementAllocationToControl(Object placementAllocation){

    }

    private void sendCurrentDemandToControl(Object demand){
        NodeFacade nodeFacade = getNodeFacade();

    }

    //TODO
    private Object getDemandAllocationFromControl(){
        //get from the control system the current demand and allocation

        return null;
    }

}
