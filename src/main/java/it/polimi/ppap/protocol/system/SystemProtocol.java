package it.polimi.ppap.protocol.system;

import it.polimi.ppap.protocol.community.MemberMessage;
import it.polimi.ppap.topology.FogNode;
import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import sun.nio.ch.Net;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SystemProtocol implements CDProtocol, EDProtocol {

    @Override
    public void nextCycle(Node node, int protocolID) {

    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        SystemMessage msg = (SystemMessage) event;
        switch (msg.getCode()){
            case SystemMessage.NODE_MON_MSG:
                processNodeMessage((FogNode) node, (NodeMessage) msg, pid);
                break;
            case SystemMessage.SPV_PLAN_MSG:
                processSupervisorMessage((FogNode) node, (SupervisorMessage) msg, pid);
                break;
            default:
                break;
        }
    }

    private void processNodeMessage(FogNode node, NodeMessage msg, int pid) {
        updateInterNodeDelay(msg.getSender(), msg.getInterNodeDelay());
        if(isAllMonitoringReceived(node, getMonitoringCount(), pid)) {
            analyze();
            plan();
        }
    }

    int monitoringCount;

    public int getMonitoringCount() {
        return monitoringCount;
    }

    private boolean isAllMonitoringReceived(Node node, int monitoringCount, int pid){
        return Network.size() == monitoringCount;
    }

    Map<FogNode, Map<FogNode, Integer>> completeInterNodeDelay = new TreeMap<>();

    private void updateInterNodeDelay(FogNode sender, Map<FogNode, Integer> interNodeDelay) {
        completeInterNodeDelay.put(sender, interNodeDelay);
    }

    private void processSupervisorMessage(FogNode node, SupervisorMessage msg, int pid) {

    }

    private void analyze() {
        for(int i = 0; i < Network.size(); i++){
            FogNode node = (FogNode) Network.get(i);

        }
    }

    private void plan() {

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
