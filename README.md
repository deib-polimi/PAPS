# PPAP SIMULATOR

<img src="https://github.com/deib-polimi/ppap-simulation/raw/master/docs/PPAP_communities.png" alt="Communities in a complex fog node topology" width="200px"/>

## PPAP

A PeerSim based implementation of the PPAP framework. The actual version features two of the three framework levels, namely:

* The community Level Self-Adaptation (Regional Planner Decentralized MAPE)
* The Node Level Self-Adaptation (Control-Theoretic Loop)

### Community Level Self-Adaptation

This level consists of a decentralized MAPE loop responsible for the self-adaptation of a FogNode community (i.e., fog nodes whose inter-node latency is within a threshold). A community is formed by normal members and a single leader. Each member performs the *monitoring* activity by collecting data about the workload -> allocation tuple (a.k.a. service allocation performance). The leader in turn *analyzes* these data and *plans* for the capacity to be allocated by different members to cope with the workload of a subset of services among those admitted into the fog system. Finally, this *placement and allocation* decision is executed by each member with the update of their catalog of hosted services and targeted capacity allocation.

### Node Level Self-Adaptation

This level is responsible for timely actuation of the number of compute runtime for each hosted service. Its goals is to keep the response time as close as possible to a set point defined by the SLA of each service.
The node level self-adaptation is materialized as a set of control-theoretic controllers (one for each hosted service) supervised by a single entity. Only in case of resource contention, the supervisor enforces the allocation scheme to respect the target values defined by the community level allocation. In all other cases, actual allocation for each hosted service will change according to unpredictable fluctuations in its workload.


## Simulator Instructions (Development)

### Dependencies

The project (or a module in IntelliJ Idea) has a dependency with another project (module), which can be found [in this repo](https://github.com/deib-polimi/ppap-node). Additionally, you must include in your path the (still missing) libraries in the lib folder (to be updated soon with MVN). 

### Simulation Execution

The simulator is implemented on top of PeerSim, which requires the specification of the simulation parameters in a file (in our case, ppap.txt found in the root folder). To execute the simulation, you must pass the name of the file (relative path is ok) as the first parameter for the *Simulator* class found in the peersim package (to be updated soon with a subclass in the root src).

### Simulation Parameters

There are many parameters you can use to adjust the simulation. They are:

* SIZE how many fog nodes within the community 
* ENTROPY how many services (for now, functions) are admitted in the system 
* BASE_CAPACITY the minimum capacity of the fog nodes in the community
* DELTATICK the physical time in milliseconds for the short simulatio cycle, which is used as a control period 

### Runtime Parameters

## Program Argument
ppap.txt

## Environment Variables
LD_LIBRARY_PATH=/opt/ibm/ILOG/CPLEX_Studio128/opl/bin/x86-64_linux

<img src="https://github.com/deib-polimi/ppap-simulation/raw/master/docs/Idea_Run_Parameters.png" alt="Communities in a complex fog node topology" />

## Known Caveats 

The simulator uses Java threads to mimick the execution of functions. In its default configuration, the JVM will allocate a considerable amount of memory to each thread stack. To prevent exausting the JVM memory and running into *OutOfMemoryError: unable to create new native thread* errors, we recommend lowering the thread memory through the JVM -Xss parameters (e.g., -Xss228k).

