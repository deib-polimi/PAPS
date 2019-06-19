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

import it.polimi.ppap.protocol.MemberStateHolder;
import it.polimi.ppap.random.initializer.NetworkDelayGenerator;
import it.polimi.ppap.topology.FogNode;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.vector.SingleValue;

/**
 * Initialize an aggregation protocol using a peak distribution; only one peak
 * is allowed. Note that any protocol implementing
 * {@link SingleValue} can be initialized by this component.
 *
 * @author Alberto Montresor
 * @version $Revision: 1.12 $
 */
public class MemberStateInitializer implements Control {

    final NetworkDelayGenerator networkDelayGenerator = new NetworkDelayGenerator();

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
     * The (RT_SLA - ET_MAX) fraction used as constraint for the inter-node delay in the optimization formulation.
     *
     * @config
     */
    private static final String PAR_BETA = "beta";


    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** Protocol identifier; obtained from config property {@link #PAR_PROT}. */
    private final int pid;

    /** Optimization parameter beta; obtained from config property {@link #PAR_BETA}. */
    private final float beta;


    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Creates a new instance and read parameters from the config file.
     */
    public MemberStateInitializer(String prefix) {

        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        beta = (float) Configuration.getDouble(prefix + "." + PAR_BETA);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
    * @return always false
    */
    public boolean execute() {
        initLeader();
        return false;
    }

    private Node getLeader(){
        return Network.get(0);
    }

    private void initLeader(){
        Node leader = getLeader();
        MemberStateHolder memberProtocol = (MemberStateHolder) leader.getProtocol(pid);
        memberProtocol.initializeLeader(beta);
        for(int i=0; i<Network.size(); ++i) {
            FogNode fogNode = (FogNode) Network.get(i);
            initInterNodeDelay(fogNode);
        }

    }

    private void initInterNodeDelay(FogNode fogNode){
        Linkable linkable =
                (Linkable) fogNode.getProtocol(FastConfig.getLinkable(pid));
        fogNode.addLinkDelay(fogNode.getID(), networkDelayGenerator.nextDelay(10));//TODO
        for(int i = 0; i < linkable.degree(); i++) {
            FogNode member = (FogNode) linkable.getNeighbor(i);
            fogNode.addLinkDelay(member.getID(), networkDelayGenerator.nextDelay(30));//TODO
        }
    }
}
