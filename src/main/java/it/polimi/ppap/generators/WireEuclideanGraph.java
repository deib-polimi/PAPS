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

package it.polimi.ppap.generators;

import it.polimi.ppap.topology.FogTopologyFactory;
import it.polimi.ppap.topology.community.CommunityFormationRun;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.dynamics.WireGraph;
import peersim.graph.Graph;
import peersim.graph.GraphFactory;

/**
 * Takes a {@link Linkable} protocol and adds random connections. Note that no
 * connections are removed, they are only added. So it can be used in
 * combination with other initializers.
 * @see GraphFactory#wireKOut
 */
public class WireEuclideanGraph extends WireGraph {

//--------------------------------------------------------------------------
//Parameters
//--------------------------------------------------------------------------

/**
 * The number of outgoing edges to generate from each node.
 * Passed to {@link GraphFactory#wireKOut}.
 * No loop edges are generated.
 * In the undirected case, the degree
 * of nodes will be on average almost twice as much because the incoming links
 * also become links out of each node.
 * @config
 */
private static final String PAR_DEGREE = "k";

//--------------------------------------------------------------------------
//Fields
//--------------------------------------------------------------------------

/**
 * The number of outgoing edges to generate from each node.
 */
private final int k;

//--------------------------------------------------------------------------
//Initialization
//--------------------------------------------------------------------------

/**
 * Standard constructor that reads the configuration parameters.
 * Invoked by the simulation engine.
 * @param prefix the configuration prefix for this class
 */
public WireEuclideanGraph(String prefix)
{
	super(prefix);
	k = Configuration.getInt(prefix + "." + PAR_DEGREE);
}

//--------------------------------------------------------------------------
//Methods
//--------------------------------------------------------------------------

/** Calls {@link GraphFactory#wireKOut}. */
public void wire(Graph g) {

	FogTopologyFactory.wireEucledeanGraph(g);
}

}
