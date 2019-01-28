package it.polimi.ppap.transport;

import peersim.core.Node;

public abstract class CommunityMessage {

    public static final int MBR_MON_MSG = 0;
    public static final int LDR_PLAN_MSG = 1;
    public static final int NODE_CRT_MSG = 2;

    final Node sender;
    final int code;

    public CommunityMessage(Node sender, int code){
        this.sender = sender;
        this.code = code;
    }

    public Node getSender() {
        return sender;
    }

    public int getCode() {
        return code;
    }
}
