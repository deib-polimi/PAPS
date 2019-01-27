package it.polimi.ppap.generator.workload;

import cern.jet.random.Normal;
import it.polimi.deib.ppap.node.NodeFacade;
import it.polimi.deib.ppap.node.commons.NormalDistribution;
import it.polimi.deib.ppap.node.commons.Utils;
import it.polimi.deib.ppap.node.services.Service;
import it.polimi.deib.ppap.node.services.ServiceRequest;
import it.polimi.ppap.common.scheme.ServiceWorkload;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import peersim.core.CommonState;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRequestGenerator {

    // State

    final Map<Service, Float> activeServices = new ConcurrentHashMap<>();
    final NodeFacade nodeFacade;
    final Random random = CommonState.r;

    // Interface

    public ServiceRequestGenerator(NodeFacade nodeFacade){
        this.nodeFacade = nodeFacade;
    }

    public void activateWorkloadForService(ServiceWorkload serviceWorkload){
        activeServices.put(serviceWorkload.getService(), serviceWorkload.getWokload());
        generateRequests(serviceWorkload);
    }

    public void disableWorkloadForService(Service service){
        activeServices.remove(service);
    }

    public void updateWorkloadForService(ServiceWorkload serviceWorkload){
        activeServices.put(serviceWorkload.getService(), serviceWorkload.getWokload());
    }

    public class ServiceNotRunningExecption extends RuntimeException{}


    // Internal

    private void generateRequests(ServiceWorkload serviceDemand){
        if(nodeFacade.isServing(serviceDemand.getService())) {
            Thread t = new Thread(executeRequestsRandomScenario(serviceDemand.getService())); //TODO 1000 to 250
            t.start();
        }else
            throw new ServiceNotRunningExecption();
    }

    private Runnable executeRequestsStableScenario(Service service){
        return () -> {
            while(activeServices.containsKey(service)) {
                long workload = activeServices.get(service).longValue();//TODO
                stableScenario(service, workload, 250);
            }
        };
    }

    private final static short MAX_WORKLOAD_SCENARIOS = 4;

    private Runnable executeRequestsRandomScenario(Service service){
        return () -> {
            short nextScenario = 0; //start with stable rate
            while(activeServices.containsKey(service)) {
                long workload = activeServices.get(service).longValue();//TODO
                switch (nextScenario){
                    case 0:
                        stableScenario(service, workload, 250);
                        break;
                    case 1:
                        decreasingScenario(service, workload, 200);
                    case 2:
                        peakScenario(service, workload, 200);
                        break;
                    default:
                        quietScenario(service,100,100);
                        break;
                }
                nextScenario = (short)random.nextInt(MAX_WORKLOAD_SCENARIOS);
            }
        };
    }

    private void stableScenario(Service service, long workload, long iterations) {
        // stable system
        System.out.println("### " + service + " entered the STABLE SCENARIO ###");
        NormalDistribution normalDistribution = Utils.getNormalDistribution(service.getSLA() * 0.8, service.getSLA() * 0.8 *0.1);
        ExponentialDistribution exponentialDistribution = new ExponentialDistribution(workload);
        for (int i = 0; i < iterations && activeServices.containsKey(service); i++) {
            long executionTime = (long) normalDistribution.random();//execution time <= SLA
            nodeFacade.execute(new ServiceRequest(service, executionTime));
            try {
                long nextArrivalTime = (long) exponentialDistribution.sample();
                Thread.sleep(nextArrivalTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void peakScenario(Service service, long workload, long iterations) {
        // peak inter-arrival rate
        System.out.println("### " + service + " entered the PEAK SCENARIO ###");
        NormalDistribution normalDistribution = Utils.getNormalDistribution(service.getSLA() * 0.8, service.getSLA() * 0.8 *0.1);
        ExponentialDistribution exponentialDistribution = new ExponentialDistribution(workload);
        for (int i = 0; i < iterations && activeServices.containsKey(service); i++) {
            nodeFacade.execute(new ServiceRequest(service, (long) normalDistribution.random()));
            try {
                Thread.sleep((long) (exponentialDistribution.sample() * 0.3));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void decreasingScenario(Service service, long workload, long iterations) {
        // decreasing inter-arrival rate
        System.out.println("### " + service + " entered the DECREASING SCENARIO ###");
        NormalDistribution normalDistribution = Utils.getNormalDistribution(service.getSLA() * 0.8, service.getSLA() * 0.8 *0.1);
        ExponentialDistribution exponentialDistribution = new ExponentialDistribution(workload);
        for (int i = 0; i < iterations && activeServices.containsKey(service); i++) {
            nodeFacade.execute(new ServiceRequest(service, (long) normalDistribution.random()));
            try {
                Thread.sleep((long) (exponentialDistribution.sample() * 0.8));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void quietScenario(Service service, long meanInterval, long iterations) {
        // clients disappear
        System.out.println("### " + service + " entered the QUIET SCENARIO ###");
        ExponentialDistribution exponentialDistribution = new ExponentialDistribution(meanInterval);
        for (int i = 0; i < iterations / 10; i++) {
            try {
                Thread.sleep((long) (exponentialDistribution.sample()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
