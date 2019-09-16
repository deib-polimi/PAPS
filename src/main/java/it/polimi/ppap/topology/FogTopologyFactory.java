package it.polimi.ppap.topology;

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
        org.graphstream.graph.Graph gsGraph = new SingleGraph("FogTopology");
        //Generator gen = new RandomEuclideanGenerator();
        Generator generator = new DorogovtsevMendesGenerator();
        //Generator gen = new BarabasiAlbertGenerator(1);
        generator.addSink(gsGraph);
        generator.begin();
        for(int i = 0; i< Network.size() - gsGraph.getNodeCount(); i++) {
            generator.nextEvents();
        }
        generator.end();
        //gsGraph.display(true);

        CommunityFormationRun.run(graph, gsGraph);
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