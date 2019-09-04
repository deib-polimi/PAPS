int Fixed = ...;
{string} Nodes = ...;
{string} Functions = ...;
int NbDemandSources = ...;
range DemandSources = 0..NbDemandSources-1;
float DemandLevel[DemandSources][Functions] = ...;
int Capacity[Nodes] = ...; //same as 'memory'
int SupplyCost[DemandSources][Nodes] = ...; //same as 'delay'
int DelayLimit[Functions] = ...;

//Decision Variables
//dvar boolean Open[Nodes]; //same as 'place'
dvar float+ Supply[DemandSources][Nodes][Functions]; //same as 'serve'

//Objective
minimize
  //sum( w in Nodes ) 
    //Fixed * Open[w] +
  sum( w in Nodes , s in DemandSources, f in Functions ) 
    SupplyCost[s][w] * Supply[s][w][f];
    
//Constraints
subject to{
  //Maximum network delay
  forall( w in Nodes, s in DemandSources, f in Functions)
    ctMaxDelay:
      SupplyCost[s][w] * Supply[s][w][f] <= DelayLimit[f] * Supply[s][w][f];
  //Maximum supply equals 100% node capacity
  forall( s in DemandSources, f in Functions )
	ctEachContainerIsHosted:
      sum( w in  Nodes )
    	Supply[s][w][f] == 1;
  //Maximum supply equals 100% node capacity
  forall( w in Nodes )
	ctMaxUseOfNodeCapacity:
      sum( s in DemandSources, f in Functions ) 
        DemandLevel[s][f] * Supply[s][w][f] <= Capacity[w];
  //Min allocation integrality
  //forall( w in Nodes, s in DemandSources, f in Functions )
	//ctMinAllocationIntegrality:
      //(DemandLevel[s][f] >= 0) => (DemandLevel[s][f] * Supply[s][w][f] == 0 || DemandLevel[s][f] * Supply[s][w][f] >= 1);
  //Supply can not be negative (redundant, since Supply is float+)
    //forall( w in Nodes, s in DemandSources, f in Functions )
      //ctSupplyGEZero:
        //Supply[s][w][f] >= 0;
}

//{int} DemandSourcesof[w in Nodes] = { s | s in DemandSources : Supply[s][w][f] == 1};
	
execute DISPLAY_RESULTS{
	//writeln("Open=",Open);
	for(var s in DemandSources)
	    for(var f in Functions)
			for(var w in Nodes)
		      	if(Supply[s][w][f] > 0)
			  		writeln("Demand of level ", DemandLevel[s][f] * Supply[s][w][f],
			  				" from source ", s,
			  				" for ", f,
			  				" served by node ", w,
			  				" with delay ", SupplyCost[s][w]);
}
