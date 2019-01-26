package it.polimi.ppap.generator.workload;

import it.polimi.deib.ppap.node.NodeFacade;
import it.polimi.deib.ppap.node.commons.NormalDistribution;
import it.polimi.deib.ppap.node.commons.Utils;
import it.polimi.deib.ppap.node.services.Service;
import it.polimi.deib.ppap.node.services.ServiceRequest;
import peersim.core.CommonState;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class ServiceRequestGenerator {

    final Map<Service, Boolean> activeServices = new TreeMap<>();
    final NodeFacade nodeFacade;
    final Random random = CommonState.r;

    public ServiceRequestGenerator(NodeFacade nodeFacade){
        this.nodeFacade = nodeFacade;
    }

    public void activateDemandForService(Service service){
        activeServices.put(service, true);
        generateRequests(service);
    }

    public void disableDemandForService(Service service){
        activeServices.put(service, false);
    }

    private void generateRequests(Service service){
        if(nodeFacade.isServing(service)) {
            Thread t = new Thread(executeRequestsStableScenario(nodeFacade, 250, service)); //TODO 1000 to 250
            t.start();
        }else
            throw new ServiceNotRunningExecption();
    }

    public class ServiceNotRunningExecption extends RuntimeException{}

    private final static short MAX_WORKLOAD_SCENARIOS = 4;

    private Runnable executeRequestsStableScenario(NodeFacade facade, long num, Service service){
        return () -> {
            while(activeServices.containsKey(service)) {
                NormalDistribution normalDistribution = Utils.getNormalDistribution(service.getSLA() * 0.8, service.getSLA() * 0.8 * 0.1);
                stableScenario(nodeFacade, num, service, normalDistribution);
            }
        };
    }

    private Runnable executeRequestsRandomScenario(NodeFacade facade, long num, Service service){
        return () -> {
            NormalDistribution normalDistribution = Utils.getNormalDistribution(service.getSLA()*0.8, service.getSLA()*0.8*0.1);
            short nextScenario = 0; //start with stable rate
            while(activeServices.containsKey(service)) {
                switch (nextScenario){
                    case 0:
                        stableScenario(facade, 200, service, normalDistribution);
                        break;

                    case 1:
                        decreasingScenario(facade, num, service, normalDistribution);

                    case 2:
                        peakScenario(facade, num, service, normalDistribution);

                        break;
                    default:
                        quietScenario(num, normalDistribution);

                        break;
                }
                nextScenario = (short)random.nextInt(MAX_WORKLOAD_SCENARIOS);
            }
        };
    }

    private void stableScenario(NodeFacade facade, long num, Service service, NormalDistribution normalDistribution) {
        //System.out.println("PHASE 1: " + service);
        // stable system
        for (int i = 0; i < num; i++) {
            facade.execute(new ServiceRequest(service, (long) normalDistribution.random()));
            try {
                Thread.sleep((long) (service.getSLA() * 1.2));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private void quietScenario(long num, NormalDistribution normalDistribution) {
        //System.out.println("PHASE 4: " + service);
        // clients disappear
        for (int i = 0; i < num / 10; i++) {
            try {
                Thread.sleep((long) (normalDistribution.random()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void peakScenario(NodeFacade facade, long num, Service service, NormalDistribution normalDistribution) {
        //System.out.println("PHASE 3: " + service);
        // peak inter-arrival rate
        for (int i = 0; i < num / 3; i++) {
            facade.execute(new ServiceRequest(service, (long) normalDistribution.random()));
            try {
                Thread.sleep((long) (normalDistribution.random() * 0.3));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void decreasingScenario(NodeFacade facade, long num, Service service, NormalDistribution normalDistribution) {
        //System.out.println("PHASE 2: " + service);
        // decreasing inter-arrival rate
        for (int i = 0; i < num / 3; i++) {
            facade.execute(new ServiceRequest(service, (long) normalDistribution.random()));
            try {
                Thread.sleep((long) (normalDistribution.random() * 0.8));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
