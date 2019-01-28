package it.polimi.ppap.scheme;

import it.polimi.deib.ppap.node.services.Service;

import java.util.HashMap;
import java.util.Map;

public class PlacementAllocationSchemaImpl implements PlacementAllocationSchema {


    Map<Service, Float> schema = new HashMap<>();

    /*public Map<Service, Float> getSchema() {
        return schema;
    }*/

    public void setSchema(Map<Service, Float> schema) {
        this.schema = schema;
    }

    @Override
    public void placeService(Service service) {
        schema.put(service, 0f);
    }

    @Override
    public void removeService(Service service) {
        schema.remove(service);
    }

    public float getServiceAllocation(Service service){
        if(schema.containsKey(service))
            return schema.get(service);
        else
            throw new ServiceNotFoundException("Service not found in this node's catalog.");
    }

    @Override
    public void updateServiceAllocation(Service service, float allocation) {
        schema.put(service, allocation);
    }


    public class ServiceNotFoundException extends RuntimeException {
        public ServiceNotFoundException(String msg){
            super(msg);
        }
    }

}
