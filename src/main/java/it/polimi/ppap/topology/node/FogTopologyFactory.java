package it.polimi.ppap.topology.node;

import it.polimi.ppap.topology.community.CommunityFormationRun;
import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.RandomEuclideanGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import peersim.core.Network;
import peersim.graph.Graph;

public class FogTopologyFactory {

    /**
     * Euclidean graph.
     * @see RandomEuclideanGenerator
     * @param graph the graph to be wired
     * @return returns g for convenience
     */
    public static Graph wireEucledeanGraph(Graph graph) {
        org.graphstream.graph.Graph gsGraph = new SingleGraph("DorogovtsevMendes");
        //Generator gen = new RandomEuclideanGenerator();
        Generator gen = new DorogovtsevMendesGenerator();
        //Generator gen = new BarabasiAlbertGenerator(1);
        gen.addSink(gsGraph);
        gen.begin();
        for(int i = 0; i< Network.size() - gsGraph.getNodeCount(); i++) {
            gen.nextEvents();
        }
        gen.end();
        //gsGraph.display(true);

        CommunityFormationRun.run(gsGraph);
        wireFromGSGraph(gsGraph, graph);
        return graph;
    }

    public static void wireFromGSGraph(org.graphstream.graph.Graph gsGraph, Graph graph){
        for(Node gsNode : gsGraph.getNodeSet()){
            for(Edge gsEdge : gsNode.getEdgeSet()){
                graph.setEdge(gsEdge.getSourceNode().getIndex(), gsEdge.getTargetNode().getIndex());
            }
        }
    }
}
