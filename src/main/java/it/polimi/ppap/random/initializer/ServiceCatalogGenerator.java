package it.polimi.ppap.random.initializer;

import it.polimi.deib.ppap.node.services.Service;
import peersim.core.CommonState;

import java.util.*;

public class ServiceCatalogGenerator {

    static Random random = CommonState.r;

    final int catalogSize;
    final long baseServiceMemory;
    final short serviceMemoryMultiplier;
    final float targetRT;

    public ServiceCatalogGenerator(
            int catalogSize,
            long baseServiceMemory,
            short serviceMemoryMultiplier,
            float targetRT){
        this.catalogSize = catalogSize;
        this.baseServiceMemory = baseServiceMemory;
        this.serviceMemoryMultiplier = serviceMemoryMultiplier;
        this.targetRT = targetRT;
    }

    public  Set<Service> generateCatalog(){
        Set<Service> serviceCatalog = new TreeSet<>();
        ServiceGenerator serviceGenerator = new ServiceGenerator(baseServiceMemory, serviceMemoryMultiplier);
        for(int i = 0; i < catalogSize; i++){
            Service service = serviceGenerator.nextService(targetRT);
            serviceCatalog.add(service);
        }
        return serviceCatalog;
    }
}
