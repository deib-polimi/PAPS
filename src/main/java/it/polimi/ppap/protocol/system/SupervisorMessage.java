package it.polimi.ppap.protocol.system;

import it.polimi.ppap.topology.node.FogNode;

public class SupervisorMessage extends SystemMessage {

    final boolean leader;

    public SupervisorMessage(FogNode sender, boolean leader){
        super(sender, SPV_PLAN_MSG);
        this.leader = leader;
    }

}
