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
import it.polimi.ppap.generator.initializer.ServiceDemandGenerator;
import it.polimi.ppap.protocol.MemberStateHolder;
import it.polimi.ppap.topology.FogNode;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.vector.SingleValue;

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


    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** Protocol identifier; obtained from config property {@link #PAR_PROT}. */
    private final int pid;


    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Creates a new instance and read parameters from the config file.
     */
    public MemberStateInitializer(String prefix) {

        pid = Configuration.getPid(prefix + "." + PAR_PROT);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
    * @return always false
    */
    public boolean execute() {
        initializeLeader();
        //initializeNodeServiceDemand(serviceCatalog);
        return false;
    }

    private Node getLeader(){
        return Network.get(0);
    }

    private void initializeLeader(){
        Node leader = getLeader();
        MemberStateHolder memberProtocol = (MemberStateHolder) leader.getProtocol(pid);
        memberProtocol.setLeader(true);
    }

    private void initializeNodeServiceDemand(Set<Service> serviceCatalog){
        //TODO parametrize in configs
        Node leader = getLeader();
        int minServiceDemand = 0;
        int maxServiceDemand = 5;
        ServiceDemandGenerator serviceDemandGenerator = new ServiceDemandGenerator(minServiceDemand, maxServiceDemand);
        MemberStateHolder memberProtocol = (MemberStateHolder) leader.getProtocol(pid);
        for(int i=0; i<Network.size(); ++i) {
            FogNode member = (FogNode)Network.get(i);
            for(Service service : serviceCatalog){
                float demand = serviceDemandGenerator.nextDemand();
                memberProtocol.updateServiceDemand(member, service, demand);
            }
        }
    }
}
