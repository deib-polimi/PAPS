/*
 * Copyright (c) 2003-2005 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package it.polimi.ppap.control;

import it.polimi.deib.ppap.node.NodeFacade;
import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.service.ServiceCatalog;
import it.polimi.ppap.protocol.NodeStateHolder;
import it.polimi.ppap.random.initializer.FogNodeCapacityGenerator;
import it.polimi.ppap.random.initializer.ServiceWorkloadGenerator;
import it.polimi.ppap.random.workload.ServiceRequestGenerator;
import it.polimi.ppap.service.ServiceWorkload;
import it.polimi.ppap.topology.FogNode;
import it.polimi.ppap.topology.NodeFactory;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * TODO
 *
 * @author Danilo Filgueira
 * @version $Revision: 1.12 $
 */
public class NodeStateInitializer implements Control {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The control period.
     *
     * @config
     */
    private static final String PAR_DELTA = "delta";

    /**
     * The protocol to operate on.
     *
     * @config
     */
    private static final String PAR_PROT = "protocol";


    /**
     * The capacity available to this catalog.
     *
     * @config
     */
    private static final String PAR_CAPACITY = "capacity";

    /**
     * The number of distinct functions in the system.
     *
     * @config
     */
    private static final String PAR_ENTROPY = "entropy";

    /**
     * The mean for randomly generating the workload.
     *
     * @config
     */
    private static final String PAR_WORKLOAD = "workload";

    /**
     * If the control-theoretic scaling is activated.
     *
     * @config
     */
    private static final String PAR_CT = "control";

    /**
     * Defines the aggressiveness of the control theoretic allocation.
     *
     *
     * @config
     */
    private static final String PAR_ALPHA = "alpha";

    /**
     * Defines the probability of the workload for a function to be active on that source.
     *
     *
     * @config
     */
    private static final String PAR_GAMA = "gama";


    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** Protocol identifier; obtained from config property {@link #PAR_PROT}. */
    private final int pid;

    /**
     * Time of a short cycle in milliseconds; obtained
     * from config property {@link #PAR_DELTA}.
     */
    private final int delta;

    /** The memory capacity of the node in GB; obtained from config property {@link #PAR_CAPACITY}. */
    private final int capacity;

    /** The number of functions admitted in the system; obtained from config property {@link #PAR_ENTROPY}. */
    private final int entropy;

    /** The mean for generating the workload for the admitted functions*/
    private final int workload;

    /** The activation of the control-theoretic scaling; obtained from config property {@link #PAR_CT}. */
    private final boolean CT;

    /** The alpha parameter defining the aggressiveness of the control-theoretic scaling; obtained from config property {@link #PAR_ALPHA}. */
    private final float alpha;

    /** The gama parameter defining the probability of an active workload for a function at a source (node); obtained from config property {@link #PAR_GAMA}. */
    private final float gama;



    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Creates a new instance and read parameters from the config file.
     */
    public NodeStateInitializer(String prefix) {
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        delta = Configuration.getInt(prefix + "." + PAR_DELTA);
        capacity = Configuration.getInt(prefix + "." + PAR_CAPACITY);
        entropy = Configuration.getInt(prefix + "." + PAR_ENTROPY);
        workload = Configuration.getInt(prefix + "." + PAR_WORKLOAD);
        CT = Configuration.getInt(prefix + "." + PAR_CT) == 1;
        alpha = (float) Configuration.getDouble(prefix + "." + PAR_ALPHA);
        gama = (float) Configuration.getDouble(prefix + "." + PAR_GAMA);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
    * @return always false
    */
    public boolean execute() {
        //TODO parametrize in configs
        FogNodeCapacityGenerator fogNodeCapacityGenerator = new FogNodeCapacityGenerator(1024 * (capacity - 1), 1024, (short) 1);
        for(int i=0; i<Network.size(); ++i) {
            FogNode node = (FogNode) Network.get(i);
            initNodeCapacity(node, fogNodeCapacityGenerator);
            NodeStateHolder nodeProt = (NodeStateHolder) node.getProtocol(pid);
            nodeProt.setCurrentWorkloadAllocation(new TreeMap<>());
            Map<Service, ServiceWorkload> localServiceWorkload = initServiceWorkload(node, ServiceCatalog.getServiceCatalog());
            nodeProt.setLocalServiceWorkload(localServiceWorkload);
            NodeFacade nodeFacade = NodeFactory.createCTNodeFacade(node, delta, alpha, CT);
            nodeProt.setNodeFacade(nodeFacade);
            nodeFacade.setTickListener(nodeProt);
            nodeFacade.start();
            ServiceRequestGenerator serviceRequestGenerator = new ServiceRequestGenerator(node, nodeFacade);
            nodeProt.setServiceRequestGenerator(serviceRequestGenerator);
        }
        return false;
    }

    private Map<Service, ServiceWorkload> initServiceWorkload(FogNode fogNode, Set<Service> serviceCatalog){
        Map<Service, ServiceWorkload> serviceWorkload = new TreeMap<>();
        for(Service service : serviceCatalog){
            serviceWorkload.put(service, initServiceWorkloadForService(fogNode, service));
        }
        return serviceWorkload;
    }

    private ServiceWorkload initServiceWorkloadForService(FogNode fogNode, Service service) {
        float mean = workload;
        float std = workload * 0.1f;
        float activeWorkloadProbability = gama;
        ServiceWorkloadGenerator serviceWorkloadGenerator = new ServiceWorkloadGenerator(mean, std, activeWorkloadProbability);
        float initialWorkload = serviceWorkloadGenerator.nextWorkload();
        ServiceWorkload serviceWorkload = new ServiceWorkload(fogNode, service, initialWorkload);
        System.out.println("########### Initialized Workload for " + service.getId() + ": " + initialWorkload + " ##############");
        return serviceWorkload;
    }

    private void initNodeCapacity(FogNode node, FogNodeCapacityGenerator fogNodeCapacityGenerator) {
        long memoryCapacity = fogNodeCapacityGenerator.nextCapacity();
        node.setMemoryCapacity(memoryCapacity);
    }
}
