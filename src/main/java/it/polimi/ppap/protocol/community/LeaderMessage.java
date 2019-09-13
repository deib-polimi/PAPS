package it.polimi.ppap.protocol.community;

import it.polimi.ppap.topology.node.FogNode;

public class LeaderMessage extends CommunityMessage {

    final Object content;

    public LeaderMessage(FogNode sender, Object content){
        super(sender, CommunityMessage.LDR_PLAN_MSG);
        this.content = content;
    }

    public Object getContent() {
        return content;
    }
}
