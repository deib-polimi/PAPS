package it.polimi.ppap.random.workload;

import it.polimi.deib.ppap.node.NodeFacade;
import it.polimi.deib.ppap.node.commons.NormalDistribution;
import it.polimi.deib.ppap.node.commons.Utils;
import it.polimi.deib.ppap.node.services.Service;
import it.polimi.deib.ppap.node.services.ServiceRequest;
import it.polimi.ppap.service.ServiceWorkload;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import peersim.core.CommonState;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.DoubleStream;

public class ServiceRequestGenerator {

    // State

    final Map<Service, Set<ServiceWorkload>> activeServices = new ConcurrentHashMap<>();
    final NodeFacade nodeFacade;
    final Random random = CommonState.r;

    // Interface

    public ServiceRequestGenerator(NodeFacade nodeFacade){
        this.nodeFacade = nodeFacade;
    }

    public void activateServiceWorkload(ServiceWorkload serviceWorkload){
        Set<ServiceWorkload> aggregateWorkload = activeServices.getOrDefault(
                serviceWorkload.getService(), new HashSet<>());
        aggregateWorkload.add(serviceWorkload);
        activeServices.put(serviceWorkload.getService(), aggregateWorkload);
        generateRequests(serviceWorkload);
    }

    public void disableServiceWorkload(Service service){
        activeServices.remove(service);
    }

    public void updateWorkloadForService(ServiceWorkload serviceWorkload){
        activeServices.get(serviceWorkload.getService()).add(serviceWorkload);
    }

    public float getAggregateWorkload(Service service){
        //100ms -> 10/s
        //200ms -> 5/s
        //total: 15/s -> 66,666ms
        DoubleStream workloadStream = activeServices.get(service).stream().mapToDouble(e -> 1000 / e.getWorkload());
        Double frequencySum = workloadStream.sum();
        return (float) (1000 / frequencySum);
    }

    public void forEach(Consumer<Service> action) {
        Objects.requireNonNull(action);
        for (Service t : activeServices.keySet()) {
            action.accept(t);
        }
    }

    public class ServiceNotRunningExecption extends RuntimeException{}


    // Internal

    private void generateRequests(ServiceWorkload serviceWorkload){
        if(nodeFacade.isServing(serviceWorkload.getService())) {
            Thread t = new Thread(executeRequestsRandomScenario(serviceWorkload)); //TODO 1000 to 250
            t.start();
        }else
            throw new ServiceNotRunningExecption();
    }

    private Runnable executeRequestsStableScenario(ServiceWorkload serviceWorkload){
        return () -> {
            while(activeServices.containsKey(serviceWorkload.getService())) {
                long workload = (long) serviceWorkload.getWorkload();
                stableScenario(serviceWorkload.getService(), workload, 250);
            }
        };
    }

    private final static short MAX_WORKLOAD_SCENARIOS = 4;

    private Runnable executeRequestsRandomScenario(ServiceWorkload serviceWorkload){
        return () -> {
            short nextScenario = 0; //start with stable rate
            while(activeServices.containsKey(serviceWorkload.getService())) {
                long workload = (long) serviceWorkload.getWorkload();
                long iterations = random.nextInt(250);
                System.out.println("Next scenario: " + iterations);
                switch (nextScenario){
                    case 0:
                        stableScenario(serviceWorkload.getService(), workload, iterations);
                        break;
                    case 1:
                        peakScenario(serviceWorkload.getService(), workload, iterations);
                    case 2:
                        decreasingScenario(serviceWorkload.getService(), workload, iterations);
                        break;
                    default:
                        quietScenario(serviceWorkload.getService(),100,iterations);
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
