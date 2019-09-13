package it.polimi.ppap.protocol.community;

import it.polimi.ppap.topology.node.FogNode;

public abstract class CommunityMessage {

    public static final int MBR_MON_MSG = 0;
    public static final int LDR_PLAN_MSG = 1;
    public static final int NODE_CRT_MSG = 2;

    final FogNode sender;
    final int code;

    public CommunityMessage(FogNode sender, int code){
        this.sender = sender;
        this.code = code;
    }

    public FogNode getSender() {
        return sender;
    }

    public int getCode() {
        return code;
    }
}
