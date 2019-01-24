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

import it.polimi.deib.ppap.node.services.Service;
import it.polimi.ppap.common.scheme.PlacementAllocationSchema;
import it.polimi.ppap.common.scheme.PlacementAllocationSchemaFactory;
import it.polimi.ppap.generator.service.ServiceCatalogGenerator;
import it.polimi.ppap.generator.service.ServiceDemandGenerator;
import it.polimi.ppap.protocol.MemberStateHolder;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.vector.SingleValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Initialize an aggregation protocol using a peak distribution; only one peak
 * is allowed. Note that any protocol implementing
 * {@link SingleValue} can be initialized by this component.
 *
 * @author Alberto Montresor
 * @version $Revision: 1.12 $
 */
public class MemberStateInitializer implements Control {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------


    /**
     * The protocol to operate on.
     *
     * @config
     */
    private static final String PAR_PROT = "protocol";

    /**
     * The number of distinct functions in the system.
     *
     * @config
     */
    private static final String PAR_ENTROPY = "entropy";

    /**
     * The capacity available to this catalog.
     *
     * @config
     */
    private static final String PAR_CAPACITY = "capacity";

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** Protocol identifier; obtained from config property {@link #PAR_PROT}. */
    private final int pid;

    /** Protocol identifier; obtained from config property {@link #PAR_ENTROPY}. */
    private final int entropy;

    /** Protocol identifier; obtained from config property {@link #PAR_CAPACITY}. */
    private final int capacity;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Creates a new instance and read parameters from the config file.
     */
    public MemberStateInitializer(String prefix) {

        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        entropy = Configuration.getInt(prefix + "." + PAR_ENTROPY);
        capacity = Configuration.getInt(prefix + "." + PAR_CAPACITY);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
    * @return always false
    */
    public boolean execute() {
        //TODO parametrize in configs
        int catalogSize = 100;
        long baseServiceMemory = 128;
        short serviceMemoryMultiplier = 2;
        float targetRT = 70;
        ServiceCatalogGenerator serviceCatalogGenerator = new ServiceCatalogGenerator(
                catalogSize, baseServiceMemory, serviceMemoryMultiplier, targetRT);
        Set<Service> serviceCatalog = serviceCatalogGenerator.generateCatalog();
        initializeLeader();
        initializeNodeServiceDemand(serviceCatalog);
        initializePlacementAllocation();
        solvePlacementAllocation();
        return false;
    }

    private void solvePlacementAllocation() {

    }

    private Node getLeader(){
        return Network.get(0);
    }

    private void initializeLeader(){
        Node leader = getLeader();
        MemberStateHolder memberProtocol = (MemberStateHolder) leader.getProtocol(pid);
        memberProtocol.setLeader(true);
    }

    private void initializePlacementAllocation() {
        for(int i = 0; i< Network.size(); ++i) {
            Node member = Network.get(i);
            MemberStateHolder memberProtocol = (MemberStateHolder) member.getProtocol(pid);
            PlacementAllocationSchemaFactory factory = PlacementAllocationSchemaFactory.getInstance();
            PlacementAllocationSchema placementAllocationSchema = factory.createPlacementAllocationSchema();
            memberProtocol.setPlacementAllocationSchema(placementAllocationSchema);
        }
    }


    private void initializeNodeServiceDemand(Set<Service> serviceCatalog){
        //TODO parametrize in configs
        Node leader = getLeader();
        int minServiceDemand = 0;
        int maxServiceDemand = 5;
        ServiceDemandGenerator serviceDemandGenerator = new ServiceDemandGenerator(minServiceDemand, maxServiceDemand);
        MemberStateHolder memberProtocol = (MemberStateHolder) leader.getProtocol(pid);
        for(int i=0; i<Network.size(); ++i) {
            Node member = Network.get(i);
            for(Service service : serviceCatalog){
                float demand = serviceDemandGenerator.nextDemand();
                memberProtocol.updateServiceDemand(member, service, demand);
            }
        }
    }
}
