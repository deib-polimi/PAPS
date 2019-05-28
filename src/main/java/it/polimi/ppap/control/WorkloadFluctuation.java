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
import it.polimi.ppap.random.initializer.ServiceDemandGenerator;
import it.polimi.ppap.protocol.NodeProtocol;
import it.polimi.ppap.random.initializer.ServiceWorkloadGenerator;
import it.polimi.ppap.service.ServiceCatalog;
import it.polimi.ppap.service.ServiceWorkload;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.util.IncrementalStats;

import java.util.Map;

/**
 * Print statistics for an average aggregation computation. Statistics printed
 * are defined by {@link IncrementalStats#toString}
 * 
 * @author Alberto Montresor
 * @version $Revision: 1.17 $
 */
public class WorkloadFluctuation implements Control {

    // /////////////////////////////////////////////////////////////////////
    // Constants
    // /////////////////////////////////////////////////////////////////////

    private static final String PAR_DELTA = "delta";

    private static final String PAR_PROT = "protocol";

    private static final String PAR_GAMA = "gama";

    private static final String PAR_WORKLOAD = "workload";

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

    /** The gama parameter defining the probability of an active workload for a function at a source (node); obtained from config property {@link #PAR_GAMA}. */
    private final float gama;

    /** The mean for generating the workload for the admitted functions*/
    private final int workload;



    /** Protocol identifier; obtained from config property {@link #PAR_PROT}. */
    private final int pid;

    // /////////////////////////////////////////////////////////////////////
    // Constructor
    // /////////////////////////////////////////////////////////////////////

    /**
     * Creates a new observer reading configuration parameters.
     */
    public WorkloadFluctuation(String prefix) {
        this.name = prefix;
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        delta = Configuration.getDouble(prefix + "." + PAR_DELTA, -1);
        gama = (float) Configuration.getDouble(prefix + "." + PAR_GAMA);
        workload = Configuration.getInt(prefix + "." + PAR_WORKLOAD);
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

        float mean = workload;
        float std = workload * 0.1f;
        float changeProbability = gama;
        for (int i = 0; i < Network.size(); i++) {
            NodeProtocol protocol = (NodeProtocol) Network.get(i)
                    .getProtocol(pid);
            Map<Service, ServiceWorkload> nodeServiceWorkload = protocol.getLocalServiceWorkload();
            fluctuateWorkload(mean, std, changeProbability, nodeServiceWorkload);
        }
        return false;
    }

    private void fluctuateWorkload(float mean, float std, float activateProbability, Map<Service, ServiceWorkload> nodeServiceWorkload) {
        ServiceWorkloadGenerator serviceWorkloadGenerator = new ServiceWorkloadGenerator(mean, std, activateProbability);
        float changeProbability = activateProbability / 10;
        for(Service service : ServiceCatalog.getServiceCatalog()) {
            ServiceWorkload serviceWorkload = nodeServiceWorkload.get(service);
            if(CommonState.r.nextFloat() < changeProbability){
                if(serviceWorkload.isActive())
                    serviceWorkload.setWorkload(0);
                else
                    serviceWorkload.setWorkload(serviceWorkloadGenerator.nextWorkload());
            }
        }
    }
}