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

import it.polimi.ppap.protocol.NodeStateHolder;
import peersim.config.Configuration;
import peersim.core.CommonState;
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
public class TimeTick implements Control {

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
     * Time of a short cycle in milliseconds; obtained
     * from config property {@link #PAR_DELTA}.
     */
    private final int delta;

    /** Protocol identifier; obtained from config property {@link #PAR_PROT}. */
    private final int pid;

    // /////////////////////////////////////////////////////////////////////
    // Constructor
    // /////////////////////////////////////////////////////////////////////

    /**
     * Creates a new observer reading configuration parameters.
     */
    public TimeTick(String name) {
        this.name = name;
        pid = Configuration.getPid(name + "." + PAR_PROT);
        delta = Configuration.getInt(name + "." + PAR_DELTA);
    }

    // /////////////////////////////////////////////////////////////////////
    // Methods
    // /////////////////////////////////////////////////////////////////////

    /**
     * Pauses the simulation for representing the pass of physical time at each cycle;
     * used to synchronize the simulation with the control period at each node.
     *
     * @return if the standard deviation is less than the given
     *         {@value #PAR_DELTA}.
     */
    public boolean execute() {

        int count = Network.size();
        try {
            //Thread.sleep(delta);
            while(count > 0) {
                synchronized (CommonState.r) {
                    CommonState.r.wait();//TODO common object, testing wait-notify
                }
                count--;
            }
            System.out.println("TICK +" + delta + "ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

}