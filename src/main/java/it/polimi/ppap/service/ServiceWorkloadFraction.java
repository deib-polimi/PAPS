package it.polimi.ppap.service;

public class ServiceWorkloadFraction extends ServiceWorkload{

    final float workloadFraction;

    public ServiceWorkloadFraction(ServiceWorkload serviceWorkload, float workloadFraction) {
        super(serviceWorkload.getSource(), serviceWorkload.getService(), serviceWorkload.getWorkload());
        this.workloadFraction = workloadFraction;
    }

    @Override
    public float getWorkload() {
        return super.getWorkload() * (1 / workloadFraction);
    }
}
