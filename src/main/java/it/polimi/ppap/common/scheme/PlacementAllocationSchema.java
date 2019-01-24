package it.polimi.ppap.common.scheme;

import it.polimi.deib.ppap.node.services.Service;

import java.util.Map;

public interface PlacementAllocationSchema {


    //public Map<Service, Float> getSchema();

    public void setSchema(Map<Service, Float> schema);

    public void placeService(Service service);

    public void removeService(Service service);

    public float getServiceAllocation(Service service);

    public void updateServiceAllocation(Service service, float allocation);

}
