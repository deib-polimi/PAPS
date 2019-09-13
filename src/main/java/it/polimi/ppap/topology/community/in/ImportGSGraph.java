package it.polimi.ppap.topology.community.in;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;

import java.io.IOException;

public class ImportGSGraph {

    String graphInputFilePath;


    public ImportGSGraph(String graphInputFilePath){
        this.graphInputFilePath = graphInputFilePath;
    }

    public Graph importGraph(String graphId) throws IOException {
        Graph graph = new DefaultGraph(graphId);
        FileSource fs = FileSourceFactory.sourceFor(graphInputFilePath);
        fs.addSink(graph);
        fs.readAll(graphInputFilePath);
        fs.removeSink(graph);
        return graph;
    }
}
