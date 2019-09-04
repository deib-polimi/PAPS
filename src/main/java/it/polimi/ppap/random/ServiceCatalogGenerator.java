package it.polimi.ppap.random;

import it.polimi.deib.ppap.node.services.Service;

import java.util.*;

public class ServiceCatalogGenerator {

    final int catalogSize;
    final long baseServiceMemory;
    final short serviceMemoryMultiplier;
    final float rtSLA;
    final float etMax;

    public ServiceCatalogGenerator(
            int catalogSize,
            long baseServiceMemory,
            short serviceMemoryMultiplier,
            float rtSLA,
            float etMax){
        this.catalogSize = catalogSize;
        this.baseServiceMemory = baseServiceMemory;
        this.serviceMemoryMultiplier = serviceMemoryMultiplier;
        this.rtSLA = rtSLA;
        this.etMax = etMax;
    }

    public  Set<Service> generateCatalog(){
        Set<Service> serviceCatalog = new TreeSet<>();
        ServiceGenerator serviceGenerator = new ServiceGenerator(baseServiceMemory, serviceMemoryMultiplier);
        for(int i = 0; i < catalogSize; i++){
            Service service = serviceGenerator.nextService(rtSLA, etMax);
            serviceCatalog.add(service);
        }
        return serviceCatalog;
    }
}
