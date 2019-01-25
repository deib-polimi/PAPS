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
import it.polimi.ppap.generator.service.FogNodeCapacityGenerator;
import it.polimi.ppap.model.FogNode;
import it.polimi.ppap.protocol.NodeStateHolder;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.vector.SingleValue;

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

    /** Protocol identifier; obtained from config property {@link #PAR_CAPACITY}. */
    private final int capacity;


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
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
    * @return always false
    */
    public boolean execute() {
        //TODO parametrize in configs
        FogNodeCapacityGenerator fogNodeCapacityGenerator = new FogNodeCapacityGenerator(1024, (short) 256);
        for(int i=0; i<Network.size(); ++i) {
            FogNode node = (FogNode) Network.get(i);
            NodeStateHolder nodeProt = (NodeStateHolder) node.getProtocol(pid);
            setupNodeState(node, fogNodeCapacityGenerator);
            NodeFacade nodeFacade = createNodeFacade(node);
            nodeProt.setNodeFacade(nodeFacade);
        }
        return false;
    }

    private void setupNodeState(FogNode node, FogNodeCapacityGenerator fogNodeCapacityGenerator) {
        long memoryCapacity = fogNodeCapacityGenerator.nextCapacity();
        node.setMemoryCapacity(memoryCapacity);
    }

    //TODO this is a factory method
    private NodeFacade createNodeFacade(FogNode node) {
        long controlPeriodMillis = delta;
        float alpha = 0.9f;//TODO parametrize in configs
        return new NodeFacade(node.getMemoryCapacity(), controlPeriodMillis, alpha);
    }
}
