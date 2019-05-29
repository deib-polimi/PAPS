package it.polimi.ppap.service;

public class ServiceWorkloadFraction extends ServiceWorkload{

    final float workloadFraction;
    ServiceWorkload serviceWorkload;

    public ServiceWorkloadFraction(ServiceWorkload serviceWorkload, float workloadFraction) {
        super(serviceWorkload.getSource(), serviceWorkload.getService(), serviceWorkload.getWorkload(), serviceWorkload.getWorkload());
        this.serviceWorkload = serviceWorkload;
        this.workloadFraction = workloadFraction;
    }

    @Override
    public float getWorkload() {
        return serviceWorkload.getWorkload() * (1 / workloadFraction);
    }

    @Override
    public float getReference() {
        return serviceWorkload.getReference();
    }
}
