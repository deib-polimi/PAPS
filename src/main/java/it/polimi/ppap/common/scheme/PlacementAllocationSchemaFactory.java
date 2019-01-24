package it.polimi.ppap.common.scheme;

public class PlacementAllocationSchemaFactory {

    static PlacementAllocationSchemaFactory instance = null;
    public static PlacementAllocationSchemaFactory getInstance(){
        if(instance != null)
            return instance;
        else{
            instance = new PlacementAllocationSchemaFactory();
            return instance;
        }
    }

    public PlacementAllocationSchema createPlacementAllocationSchema(){
        PlacementAllocationSchema scheme = new PlacementAllocationSchemaImpl();
        return scheme;
    }
}
