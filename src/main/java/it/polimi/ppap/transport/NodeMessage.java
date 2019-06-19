package it.polimi.ppap.transport;

import it.polimi.ppap.topology.FogNode;

public class NodeMessage
extends CommunityMessage{

    final Object content;

    public NodeMessage(FogNode sender, Object content){
        super(sender, NODE_CRT_MSG);
        this.content = content;
    }
}
