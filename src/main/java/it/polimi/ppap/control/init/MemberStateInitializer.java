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

package it.polimi.ppap.control.init;

import it.polimi.ppap.protocol.community.MemberStateHolder;
import it.polimi.ppap.protocol.community.CommunityLeaderBehaviour;
import it.polimi.ppap.protocol.community.CommunityMemberBehaviour;
import it.polimi.ppap.generators.LinearNetworkDelayGenerator;
import it.polimi.ppap.topology.node.FogNode;
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

    final LinearNetworkDelayGenerator networkDelayGenerator = new LinearNetworkDelayGenerator();

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

    /**
     * The reference control period variable name in the simulation config file.
     */
    private static final String PAR_REF_CONTROL_PERIOD = "rcp";


    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** Protocol identifier; obtained from config property {@link #PAR_PROT}. */
    private final int pid;

    /** Optimization parameter beta; obtained from config property {@link #PAR_BETA}. */
    private final float beta;

    /**
     * The reference control period used at the node level.
     * @see CommunityLeaderBehaviour#updateDemandFromStaticAllocation
     */
    private final int referenceControlPeriod;


    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Creates a new instance and read parameters from the config file.
     */
    public MemberStateInitializer(String prefix) {

        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        beta = (float) Configuration.getDouble(prefix + "." + PAR_BETA);
        referenceControlPeriod = Configuration.getInt(prefix + "." + PAR_REF_CONTROL_PERIOD);

    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
    * @return always false
    */
    public boolean execute() {
        initializeLeader();
        for(int i=0; i<Network.size(); ++i) {
            FogNode fogNode = (FogNode) Network.get(i);
            initInterNodeDelay(fogNode);
            initCommunityMemberBehavior(fogNode);
        }
        return false;
    }

    private void initCommunityMemberBehavior(FogNode fogNode) {
        MemberStateHolder memberProtocol = (MemberStateHolder) fogNode.getProtocol(pid);
        memberProtocol.setCommunityMemberBehaviour(new CommunityMemberBehaviour(memberProtocol));
    }

    private Node pickCommunityLeader(){
        return Network.get(0);
    }

    private void initializeLeader(){
        Node leader = pickCommunityLeader();
        MemberStateHolder memberProtocol = (MemberStateHolder) leader.getProtocol(pid);
        memberProtocol.initializeLeader(beta, referenceControlPeriod);
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
