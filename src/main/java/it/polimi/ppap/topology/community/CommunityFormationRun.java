package it.polimi.ppap.topology.community;

import it.polimi.ppap.topology.community.in.ImportSLPACommunities;
import it.polimi.ppap.topology.community.out.ExportGSGraph;
import it.polimi.ppap.topology.community.out.ExportSPLAGraph;
import org.graphstream.graph.Graph;
import org.graphstream.ui.view.Viewer;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class CommunityFormationRun {

    public static void run(peersim.graph.Graph graph, Graph gsGraph){
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
        String outputSLPAFile = rootPath + "/res/data/slpaGraph.ipairs";
        String outputGSFile = rootPath + "/res/data/gsGraph.dgs";
        exportFiles(gsGraph, outputSLPAFile, outputGSFile);

        String slpaPath = rootPath + "/res/communitydetection/slpa/";
        String outputSLPAPath = rootPath + "/res/data/";
        runSPLACommunityDetection(slpaPath, outputSLPAFile, outputSLPAPath);

        //String inputGSFile = rootPath + "/res/data/gsGraph.dgs";
        //String inputSPLACommunitiesFile = rootPath + "/res/data/SLPAw_slpaGraph_run1_r0.35_v3_T100.icpm";
        String inputSPLACommunitiesFile = rootPath + "/res/data/SLPAw_slpaGraph_run1_r0.35_v3_T100.icpm.node-com.txt";
        importCommunities(graph, gsGraph, inputSPLACommunitiesFile);
    }

    private static void exportFiles(Graph gsGraph, String outputSLPAFile, String outputGSFile) {
        try {
            ExportSPLAGraph exportSLPAGraph = new ExportSPLAGraph(outputSLPAFile);
            ExportGSGraph exportGSGraph = new ExportGSGraph(outputGSFile);
            exportGSGraph.exportGraph(gsGraph);
            exportSLPAGraph.exportGraph(gsGraph);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void importCommunities(peersim.graph.Graph graph, Graph gsGraph, String inputSLPACommunitiesFile){
        String rootPath = System.getProperty("user.dir");
        try {
            ImportSLPACommunities importSLPACommunities = new ImportSLPACommunities((inputSLPACommunitiesFile));
            String cssPath = rootPath + "/src/main/resources/css/stylesheet.css";
            gsGraph.addAttribute("ui.stylesheet", "url('file://" + cssPath +  "')");
            Viewer viewer =  gsGraph.display(true);
            importSLPACommunities.importCommunities(graph, gsGraph);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void runSPLACommunityDetection(String slpaPath, String inputSPLAFile, String outputSPLAPath){
        ProcessBuilder builder = new ProcessBuilder();
        //if (isWindows) {
            //builder.command("cmd.exe", "/c", "dir");
        //} else {
        builder.command("java", "-jar", slpaPath + "GANXiSw.jar", "-i", inputSPLAFile, "-d", outputSPLAPath, "-Sym", "1", "-Onc", "1");
        //}
        builder.directory(new File(slpaPath));
        Process process = null;
        try {
            process = builder.start();
            StreamGobbler streamGobbler =
                    new StreamGobbler(process.getInputStream(), System.out::println);
            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), System.out::println);
            Executors.newSingleThreadExecutor().submit(streamGobbler);
            Executors.newSingleThreadExecutor().submit(errorGobbler);
            int exitCode = process.waitFor();
            assert exitCode == 0;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }

}
