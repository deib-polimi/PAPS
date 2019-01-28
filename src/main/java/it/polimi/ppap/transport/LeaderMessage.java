package it.polimi.ppap.transport;

import peersim.core.Node;

public class LeaderMessage extends CommunityMessage {

    final Object content;

    public LeaderMessage(Node sender, Object content){
        super(sender, CommunityMessage.LDR_PLAN_MSG);
        this.content = content;
    }

    public Object getContent() {
        return content;
    }
}
