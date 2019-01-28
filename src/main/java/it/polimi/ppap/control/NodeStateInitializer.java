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
import it.polimi.ppap.generator.initializer.FogNodeCapacityGenerator;
import it.polimi.ppap.generator.initializer.ServiceCatalogGenerator;
import it.polimi.ppap.generator.initializer.ServiceWorkloadGenerator;
import it.polimi.ppap.generator.workload.ServiceRequestGenerator;
import it.polimi.ppap.service.ServiceWorkload;
import it.polimi.ppap.topology.FogNode;
import it.polimi.ppap.protocol.NodeStateHolder;
import it.polimi.ppap.topology.NodeFactory;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.vector.SingleValue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Initialize an aggregation protocol using a peak distribution; only one peak
 * is allowed. Note that any protocol implementing
 * {@link SingleValue} can be initialized by this component.
 *
 * @author Alberto Montresor
 * @version $Revision: 1.12 $
 */
public class NodeStateInitializer implements Control {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /** TODO
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

    /** TODO; obtained from config property {@link #PAR_CAPACITY}. */
    private final int capacity;

    /** TODO; obtained from config property {@link #PAR_ENTROPY}. */
    private final int entropy;


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
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
    * @return always false
    */
    public boolean execute() {
        //TODO parametrize in configs
        FogNodeCapacityGenerator fogNodeCapacityGenerator = new FogNodeCapacityGenerator(1024* 32, 1024, (short) capacity);
        ServiceCatalogGenerator serviceCatalogGenerator = createServiceCatalogGenerator();
        Set<Service> serviceCatalog = serviceCatalogGenerator.generateCatalog();
        for(int i=0; i<Network.size(); ++i) {
            FogNode node = (FogNode) Network.get(i);
            setupNodeCapacity(node, fogNodeCapacityGenerator);
            NodeStateHolder nodeProt = (NodeStateHolder) node.getProtocol(pid);
            nodeProt.setCurrentWorkloadAllocation(new TreeMap<>());
            Map<Service, ServiceWorkload> localServiceWorkload = initServiceWorkload(node, serviceCatalog);
            nodeProt.setLocalServiceWorkload(localServiceWorkload);
            NodeFacade nodeFacade = NodeFactory.createCTNodeFacade(node, 3000, 0.9f);
            nodeProt.setNodeFacade(nodeFacade);
            nodeFacade.setTickListener(nodeProt);
            nodeFacade.start();
            ServiceRequestGenerator serviceRequestGenerator = new ServiceRequestGenerator(nodeFacade);
            nodeProt.setServiceRequestGenerator(serviceRequestGenerator);
        }
        return false;
    }

    private ServiceCatalogGenerator createServiceCatalogGenerator(){
        //TODO parametrize in configs
        int catalogSize = entropy;
        long baseServiceMemory = 128;
        short randomServiceMemoryMultiplier = 2;
        float targetRT = 70;
        ServiceCatalogGenerator serviceCatalogGenerator = new ServiceCatalogGenerator(
                catalogSize, baseServiceMemory, randomServiceMemoryMultiplier, targetRT);
        return serviceCatalogGenerator;
    }

    private Map<Service, ServiceWorkload> initServiceWorkload(FogNode fogNode, Set<Service> serviceCatalog){
        Map<Service, ServiceWorkload> serviceWorkload = new TreeMap<>();
        for(Service service : serviceCatalog){
            serviceWorkload.put(service, initServiceWorkloadForService(fogNode, service));
        }
        return serviceWorkload;
    }

    private ServiceWorkload initServiceWorkloadForService(FogNode fogNode, Service service) {
        float mean = service.getSLA() * 5f;
        float std = service.getSLA() * 0.1f;
        float activeWorkloadProbability = 0.6f;
        ServiceWorkloadGenerator serviceWorkloadGenerator = new ServiceWorkloadGenerator(mean, std, activeWorkloadProbability);
        float initialWorkload = serviceWorkloadGenerator.nextWorkload();
        ServiceWorkload serviceWorkload = new ServiceWorkload(fogNode, service, initialWorkload);
        System.out.println("########### Initialized Workload for " + service.getId() + ": " + initialWorkload + " ##############");
        return serviceWorkload;
    }

    private void setupNodeCapacity(FogNode node, FogNodeCapacityGenerator fogNodeCapacityGenerator) {
        long memoryCapacity = fogNodeCapacityGenerator.nextCapacity();
        node.setMemoryCapacity(memoryCapacity);
    }
}
