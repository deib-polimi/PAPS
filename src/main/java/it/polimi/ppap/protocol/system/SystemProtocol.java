package it.polimi.ppap.protocol.system;

import peersim.cdsim.CDProtocol;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

public class SystemProtocol implements CDProtocol, EDProtocol {

    @Override
    public void nextCycle(Node node, int protocolID) {

    }

    @Override
    public void processEvent(Node node, int pid, Object event) {

    }

    @Override
    public Object clone() {
        SystemProtocol svh=null;
        try {
            svh=(SystemProtocol)super.clone();
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return svh;
    }


}
