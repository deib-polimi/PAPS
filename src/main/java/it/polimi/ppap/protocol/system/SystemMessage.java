package it.polimi.ppap.protocol.system;

import it.polimi.ppap.topology.FogNode;

public abstract class SystemMessage {

    public static final int NODE_MON_MSG = 0;
    public static final int SPV_PLAN_MSG = 1;

    final FogNode sender;
    final int code;

    public SystemMessage(FogNode sender, int code){
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
