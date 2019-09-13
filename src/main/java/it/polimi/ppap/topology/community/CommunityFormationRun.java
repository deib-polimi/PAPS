package it.polimi.ppap.topology.community;

import it.polimi.ppap.topology.community.out.ExportGSGraph;
import it.polimi.ppap.topology.community.out.ExportSPLAGraph;
import org.graphstream.graph.Graph;

import java.io.IOException;

public class CommunityFormationRun {

    public static void run(Graph graph){
        /*Graph graph = new SingleGraph("DorogovtsevMendes");
        //Generator gen = new DorogovtsevMendesGenerator();
        //Generator gen = new BarabasiAlbertGenerator(1);
        Generator gen = new RandomEuclideanGenerator();
        gen.addSink(graph);
        gen.begin();
        for(int i=0; i<500; i++) {
            gen.nextEvents();
        }

        gen.end();
        graph.display(true);*/

        String rootPath = System.getProperty("user.dir");
        String outputSPLAFile = "/splaGraph.ipairs";
        String outputGSFile = "/gsGraph.dgs";
        try {
            ExportSPLAGraph exportSPLAGraph = new ExportSPLAGraph(rootPath + outputSPLAFile);
            ExportGSGraph exportGSGraph = new ExportGSGraph(rootPath + outputGSFile);
            exportGSGraph.exportGraph(graph);
            exportSPLAGraph.exportGraph(graph);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
