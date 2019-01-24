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

import it.polimi.ppap.common.random.RandomDemand;
import it.polimi.ppap.protocol.NodeProtocol;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.util.IncrementalStats;

/**
 * Print statistics for an average aggregation computation. Statistics printed
 * are defined by {@link IncrementalStats#toString}
 * 
 * @author Alberto Montresor
 * @version $Revision: 1.17 $
 */
public class DemandFluctuation implements Control {

    // /////////////////////////////////////////////////////////////////////
    // Constants
    // /////////////////////////////////////////////////////////////////////

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

    // /////////////////////////////////////////////////////////////////////
    // Fields
    // /////////////////////////////////////////////////////////////////////

    /**
     * The name of this observer in the configuration. Initialized by the
     * constructor parameter.
     */
    private final String name;

    /**
     * Accuracy for standard deviation used to stop the simulation; obtained
     * from config property {@link #PAR_DELTA}.
     */
    private final double delta;

    /** Protocol identifier; obtained from config property {@link #PAR_PROT}. */
    private final int pid;

    // /////////////////////////////////////////////////////////////////////
    // Constructor
    // /////////////////////////////////////////////////////////////////////

    /**
     * Creates a new observer reading configuration parameters.
     */
    public DemandFluctuation(String name) {
        this.name = name;
        pid = Configuration.getPid(name + "." + PAR_PROT);
        delta = Configuration.getDouble(name + "." + PAR_DELTA, -1);
    }

    // /////////////////////////////////////////////////////////////////////
    // Methods
    // /////////////////////////////////////////////////////////////////////

    /**
     * Print statistics for an average aggregation computation. Statistics
     * printed are defined by {@link IncrementalStats#toString}. The current
     * timestamp is also printed as a first field.
     *
     * @return if the standard deviation is less than the given
     *         {@value #PAR_DELTA}.
     */
    public boolean execute() {

        //TODO
        for (int i = 0; i < Network.size(); i++) {
            NodeProtocol protocol = (NodeProtocol) Network.get(i)
                    .getProtocol(pid);

            /*for(String functionName : protocol.getCatalog().getDemands().keySet()) {
                double actualDemand = protocol.getCatalog().getDemands().get(functionName);
                double nextDemand = getNextDemand(actualDemand);
                protocol.getCatalog().updateDemand(functionName, nextDemand);
            }*/
        }
        return false;
    }

    private double getNextDemand(double actualDemand) {
        double variation = RandomDemand.getVariation(delta);
        return Math.max(0, actualDemand + variation);
    }

}