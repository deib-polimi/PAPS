package it.polimi.ppap.protocol.system;

import it.polimi.ppap.topology.FogNode;

public class DemandCapacityMessage extends SystemMessage {

    final DemandCapacity demandCapacity;

    public DemandCapacityMessage(FogNode sender, DemandCapacity demandCapacity){
        super(sender, DEMAND_CAPCITY_MSG);
        this.demandCapacity = demandCapacity;
    }

    public DemandCapacity getDemandCapacity() {
        return demandCapacity;
    }

    class DemandCapacity {
        final float demand;
        final float capacity;
        DemandCapacity(float demand, float capacity) {
            this.demand = demand;
            this.capacity = capacity;
        }
    }
}
