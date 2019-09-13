package it.polimi.ppap.topology.community.in;

import it.polimi.ppap.ui.RandomColorPicker;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.view.Viewer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ImportSLPACommunities {

    String communitiesInputFilePath;
    Scanner reader;

    public ImportSLPACommunities(String communitiesInputFilePath){
        this.communitiesInputFilePath = communitiesInputFilePath;
    }

    public void importCommunities(Graph graph, Viewer viewer) throws IOException {
        openFile();
        Map<String, Color> communityColor = new HashMap<>();
        while (reader.hasNext()) {
            String line = reader.nextLine();
            String [] terms = line.split(" ");
            String nodeId = terms[0];
            String communityId = terms[1];
            Node node = graph.getNode(nodeId);
            if(!communityColor.containsKey(communityId))
                communityColor.put(communityId, RandomColorPicker.pickRandomColor());
            String rgb = getColorRGB(communityColor.get(communityId));
            if(node.getAttribute("membership") != null) {
                String sharedRgb = getColorRGB(communityColor.get(node.getAttribute("membership")));
                node.addAttribute("ui.class", "shared");
                node.addAttribute("ui.pie-values", new Float[]{0.5f, 0.5f});
                node.addAttribute("ui.style", "fill-color: " + rgb + ", " + sharedRgb + ";");
                node.addAttribute("membership", communityId);
            }else{
                node.addAttribute("ui.style", "fill-color: " + rgb + ";");
            }
            node.addAttribute("membership", communityId);
        }
        closeFile();
    }



    private String getColorRGB(Color color){
        return "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")";
    }

    private void openFile() throws IOException {
        File graphFile = new File(communitiesInputFilePath);
        reader = new Scanner(graphFile);
        //reader.useDelimiter(" ");
    }

    private void closeFile() throws IOException {
        reader.close();
    }
}
