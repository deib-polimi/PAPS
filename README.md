# PPAP SIMULATOR

## PPAP

A PeerSim based implementation of the PPAP framework. The actual version features two of the three framework levels, namely:

* The community Level Self-Adaptation (Regional Planner Decentralized MAPE)
* The Node Level Self-Adaptation (Control-Theoretic Loop)

### Community Level Self-Adaptation

This level consists of a decentralized MAPE loop responsible for the self-adaptation of a FogNode community (i.e., fog nodes whose inter-node latency is within a threshold). A community is formed by normal members and a single leader. Each member performs the *monitoring* activity by collecting data about the workload -> allocation tuple (a.k.a. service allocation performance). The leader in turn *analyzes* these data and *plans* for the capacity to be allocated by different members to cope with the workload of a subset of services among those admitted into the fog system. Finally, this *placement and allocation* decision is executed by each member with the update of their catalog of hosted services and targeted capacity allocation.

### Node Level Self-Adaptation

This level is responsible for timely actuation of the number of compute runtime for each hosted service. Its goals is to keep the response time as close as possible to a set point defined by the SLA of each service.
The node level self-adaptation is materialized as a set of control-theoretic controllers (one for each hosted service) supervised by a single entity. Only in case of resource contention, the supervisor enforces the allocation scheme to respect the target values defined by the community level allocation. In all other cases, actual allocation for each hosted service will change according to unpredictable fluctuations in its workload.


## Simulator Instructions

