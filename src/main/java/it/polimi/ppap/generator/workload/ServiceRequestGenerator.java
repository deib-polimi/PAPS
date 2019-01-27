package it.polimi.ppap.generator.workload;

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

    /** Variables **/

    final Map<Service, Float> activeServices = new ConcurrentHashMap<>();
    final NodeFacade nodeFacade;
    final Random random = CommonState.r;

    /** Interface **/

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

    /** Methods **/

    private final static short MAX_WORKLOAD_SCENARIOS = 4;

    private void generateRequests(ServiceWorkload serviceDemand){
        if(nodeFacade.isServing(serviceDemand.getService())) {
            Thread t = new Thread(executeRequestsStableScenario(serviceDemand.getService(), 250)); //TODO 1000 to 250
            t.start();
        }else
            throw new ServiceNotRunningExecption();
    }

    private Runnable executeRequestsStableScenario(Service service, long num){
        return () -> {
            while(activeServices.containsKey(service)) {
                float demand = activeServices.get(service);
                NormalDistribution normalDistribution = Utils.getNormalDistribution(service.getSLA() * 0.8, service.getSLA() * 0.8 * 0.1);
                ExponentialDistribution exponentialDistribution = new ExponentialDistribution(demand);
                stableScenario(num, service, normalDistribution, exponentialDistribution);
            }
        };
    }

    private Runnable executeRequestsRandomScenario(ServiceWorkload serviceDemand, long num){
        Service service = serviceDemand.getService();
        return () -> {
            float demand = activeServices.get(service);
            NormalDistribution normalDistribution = Utils.getNormalDistribution(service.getSLA()*0.8, service.getSLA()*0.8*0.1);
            ExponentialDistribution exponentialDistribution = new ExponentialDistribution(demand);
            short nextScenario = 0; //start with stable rate
            while(activeServices.containsKey(service)) {
                switch (nextScenario){
                    case 0:
                        stableScenario(200, service, normalDistribution, exponentialDistribution);
                        break;
                    case 1:
                        decreasingScenario(num, service, normalDistribution);
                    case 2:
                        peakScenario(num, service, normalDistribution);
                        break;
                    default:
                        quietScenario(num, normalDistribution);
                        break;
                }
                nextScenario = (short)random.nextInt(MAX_WORKLOAD_SCENARIOS);
            }
        };
    }

    private void stableScenario(long num, Service service, NormalDistribution normalDistribution, ExponentialDistribution exponentialDistribution) {
        // stable system
        for (int i = 0; i < num && activeServices.containsKey(service); i++) {
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

    private void quietScenario(long num, NormalDistribution normalDistribution) {
        // clients disappear
        for (int i = 0; i < num / 10; i++) {
            try {
                Thread.sleep((long) (normalDistribution.random()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void peakScenario(long num, Service service, NormalDistribution normalDistribution) {
        // peak inter-arrival rate
        for (int i = 0; i < num / 3; i++) {
            nodeFacade.execute(new ServiceRequest(service, (long) normalDistribution.random()));
            try {
                Thread.sleep((long) (normalDistribution.random() * 0.3));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void decreasingScenario(long num, Service service, NormalDistribution normalDistribution) {
        // decreasing inter-arrival rate
        for (int i = 0; i < num / 3; i++) {
            nodeFacade.execute(new ServiceRequest(service, (long) normalDistribution.random()));
            try {
                Thread.sleep((long) (normalDistribution.random() * 0.8));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
