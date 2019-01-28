package it.polimi.ppap.service;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.topology.FogNode;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

public class AggregateServiceDemand {

    final Set<ServiceDemand> sourceDemand = new TreeSet<>();

    public void addServiceDemand(FogNode source, Service service, float demand){
        ServiceDemand serviceDemand = new ServiceDemand(source, service, demand);
        sourceDemand.add(serviceDemand);
    }

    public float getAggregateDemand() {
        float aggregateDemand = (float) sourceDemand.stream().mapToDouble(e -> e.getDemand()).sum();
        return aggregateDemand;
    }

    public void forEach(Consumer<ServiceDemand> action) {
        Objects.requireNonNull(action);
        for (ServiceDemand t : sourceDemand) {
            action.accept(t);
        }
    }


}
