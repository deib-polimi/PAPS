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
import it.polimi.ppap.random.initializer.ServiceCatalogGenerator;
import it.polimi.ppap.service.ServiceCatalog;
import peersim.config.Configuration;
import peersim.core.Control;
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
public class ServiceCatalogInitializer implements Control {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The number of distinct functions in the system.
     *
     * @config
     */
    private static final String PAR_ENTROPY = "entropy";

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** TODO; obtained from config property {@link #PAR_ENTROPY}. */
    private final int entropy;


    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Creates a new instance and read parameters from the config file.
     */
    public ServiceCatalogInitializer(String prefix) {
        entropy = Configuration.getInt(prefix + "." + PAR_ENTROPY);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
    * @return always false
    */
    public boolean execute() {
        ServiceCatalogGenerator serviceCatalogGenerator = createServiceCatalogGenerator();
        Set<Service> serviceCatalog = serviceCatalogGenerator.generateCatalog();
        ServiceCatalog.setServiceCatalog(serviceCatalog);
        return false;
    }

    private ServiceCatalogGenerator createServiceCatalogGenerator(){
        //TODO parametrize in configs
        int catalogSize = entropy;
        long baseServiceMemory = 128;
        short randomServiceMemoryMultiplier = 2;
        float rtSLA = 100;
        float etMax = 70;
        ServiceCatalogGenerator serviceCatalogGenerator = new ServiceCatalogGenerator(
                catalogSize, baseServiceMemory, randomServiceMemoryMultiplier, rtSLA, etMax);
        return serviceCatalogGenerator;
    }

}
