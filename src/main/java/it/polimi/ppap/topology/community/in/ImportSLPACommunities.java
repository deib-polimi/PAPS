package it.polimi.ppap.topology.community.in;

import it.polimi.ppap.topology.FogTopology;
import it.polimi.ppap.topology.community.Community;
import it.polimi.ppap.topology.node.FogNode;
import it.polimi.ppap.ui.RandomColorPicker;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

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

    public void importCommunities(peersim.graph.Graph graph, Graph gsGraph) throws IOException {
        openFile();
        Map<String, Color> communityColor = new HashMap<>();
        while (reader.hasNext()) {
            String line = reader.nextLine();
            String [] terms = line.split(" ");
            String nodeId = terms[0];
            String communityId = terms[1];
            Community community = FogTopology.initializeCommunity(communityId);
            initializeCommunityColor(communityColor, communityId);
            Node gsNode = gsGraph.getNode(nodeId);
            addCommunityColor(communityId, communityColor, gsNode);
            initializeMembership(communityId, gsNode);
            FogNode fogNode = (FogNode) graph.getNode(Integer.parseInt(nodeId));
            community.add(fogNode);
            fogNode.addToCommunity(community);
        }
        closeFile();
    }

    private void initializeMembership(String communityId, Node gsNode) {
        //TODO only works with up to 2 communities
        gsNode.addAttribute("membership", communityId);
    }

    private void initializeCommunityColor(Map<String, Color> communityColor, String communityId) {
        if(!communityColor.containsKey(communityId))
            communityColor.put(communityId, RandomColorPicker.pickRandomColor());
    }

    private void addCommunityColor(String communityId, Map<String, Color> communityColor, Node gsNode) {
        String rgb = getColorRGB(communityColor.get(communityId));
        if(gsNode.getAttribute("membership") != null) {
            String sharedRgb = getColorRGB(communityColor.get(gsNode.getAttribute("membership")));
            gsNode.addAttribute("ui.class", "shared");
            gsNode.addAttribute("ui.pie-values", new Float[]{0.5f, 0.5f});
            gsNode.addAttribute("ui.style", "fill-color: " + rgb + ", " + sharedRgb + ";");
            initializeMembership(communityId, gsNode);
        } else{
            gsNode.addAttribute("ui.style", "fill-color: " + rgb + ";");
        }
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
