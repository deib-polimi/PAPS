package it.polimi.ppap.common.communication;

import peersim.core.Node;

public class MemberMessage extends CommunityMessage {

    final Object content;

    public MemberMessage(Node sender, Object content){
        super(sender, MBR_MON_MSG);
        this.content = content;
    }

    public Object getContent() {
        return content;
    }
}
