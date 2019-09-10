package it.polimi.ppap.protocol.system;

import it.polimi.ppap.topology.FogNode;

import java.util.Map;

public class InterNodeDelayMessage extends SystemMessage {

    final Map<FogNode, Integer> interNodeDelay;

    public InterNodeDelayMessage(FogNode sender, Map<FogNode, Integer> interNodeDelay){
        super(sender, NODE_MON_MSG);
        this.interNodeDelay = interNodeDelay;
    }

    public Map<FogNode, Integer> getInterNodeDelay() {
        return interNodeDelay;
    }
}
