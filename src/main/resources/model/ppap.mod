// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
//
// 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55
// Copyright IBM Corporation 1998, 2013. All Rights Reserved.
//
// Note to U.S. Government Users Restricted Rights:
// Use, duplication or disclosure restricted by GSA ADP Schedule
// Contract with IBM Corp.
// --------------------------------------------------------------------------

int Fixed = ...;
{string} Nodes = ...;
{string} Functions = ...;
int NbDemandSources = ...;
range DemandSources = 0..NbDemandSources-1;
int DemandLevel[DemandSources][Functions] = ...;
int Capacity[Nodes] = ...; //same as 'memory'
int SupplyCost[DemandSources][Nodes] = ...; //same as 'delay'

//Decision Variables
dvar boolean Open[Nodes]; //same as 'place'
dvar boolean Supply[DemandSources][Nodes][Functions]; //same as 'serve'

//Objective
minimize
  //sum( w in Nodes ) 
    //Fixed * Open[w] +
  sum( w in Nodes , s in DemandSources, f in Functions ) 
    SupplyCost[s][w] * Supply[s][w][f];
    
//Constraints
subject to{
  forall( s in DemandSources, f in Functions )
	ctEachStoreHasOneWarehouse:
      sum( w in  Nodes ) 
    	Supply[s][w][f] == 1;
  forall( w in Nodes, s in DemandSources, f in Functions )
    ctUseOpenNodes:
      Supply[s][w][f] <= Open[w];
  forall( w in Nodes )
	ctMaxUseOfWarehouse:         
      sum( s in DemandSources, f in Functions ) 
        DemandLevel[s][f] * Supply[s][w][f] <= Capacity[w];
  forall( w in Nodes, f in Functions )
    ctMaxCostOfWarehouse:    
      sum( s in DemandSources ) 
        SupplyCost[s][w] * Supply[s][w][f] <= 30;
}


//{int} DemandSourcesof[w in Nodes] = { s | s in DemandSources : Supply[s][w][f] == 1};
	
execute DISPLAY_RESULTS{
	writeln("Open=",Open);
	for(var s in DemandSources)
	    for(var f in Functions)
			for(var w in Nodes)
		      	if(Supply[s][w][f] == 1)
			  		writeln("Demand of level ", DemandLevel[s][f],
			  				" from source ", s,
			  				" for ", f,
			  				" served by node ", w,
			  				" with delay ", SupplyCost[s][w]);
}
