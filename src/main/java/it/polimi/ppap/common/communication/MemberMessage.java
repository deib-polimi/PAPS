package it.polimi.ppap.common.communication;

import it.polimi.deib.ppap.node.services.Service;
import peersim.core.Node;

import java.util.Map;

public class MemberMessage extends CommunityMessage {

    final Map<Service, Map.Entry<Float, Float>> content;

    public MemberMessage(Node sender, Map<Service, Map.Entry<Float, Float>> content){
        super(sender, MBR_MON_MSG);
        this.content = content;
    }

    public Map<Service, Map.Entry<Float, Float>> getContent() {
        return content;
    }
}
