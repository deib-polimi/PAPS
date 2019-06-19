package it.polimi.ppap.service;

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.topology.FogNode;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class AggregateServiceAllocation {

    final Set<ServiceAllocation> sourceAllocation = new TreeSet<>();

    public void addServiceAllocation(FogNode source, Service service, float allocation, float fraction){
        ServiceAllocation serviceAllocation = new ServiceAllocation(source, service, allocation, fraction);
        sourceAllocation.add(serviceAllocation);
    }

    public float getAggregateAllocation() {
        float aggregateDemand = (float) sourceAllocation.stream().mapToDouble(e -> e.getAllocation()).sum();
        return aggregateDemand;
    }

    public void forEach(Consumer<ServiceAllocation> action) {
        Objects.requireNonNull(action);
        for (ServiceAllocation t : sourceAllocation) {
            action.accept(t);
        }
    }

    public Stream<ServiceAllocation> stream(){
        return sourceAllocation.stream();
    }


}
