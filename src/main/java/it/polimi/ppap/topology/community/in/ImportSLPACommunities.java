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

    public static final String BASE_ID = "0";

    String communitiesInputFilePath;
    Scanner reader;
    Map<String, Color> communityColor = new HashMap<>();

    public ImportSLPACommunities(String communitiesInputFilePath){
        this.communitiesInputFilePath = communitiesInputFilePath;
    }

    public void importCommunities(peersim.graph.Graph graph, Graph gsGraph) throws IOException {
        openFile();
        while (reader.hasNext()) {
            String line = reader.nextLine();
            String [] terms = line.split(" ");
            String nodeId = terms[0];
            String communityId = terms[1];
            importCommunity(graph, gsGraph, nodeId, communityId);
        }
        closeFile();
    }

    private void importCommunity(
            peersim.graph.Graph graph, Graph gsGraph,
            String nodeId, String communityId) {
        Community community = FogTopology.initializeCommunity(communityId);
        initializeCommunityColor(communityColor, communityId);
        Node gsNode = gsGraph.getNode(nodeId);
        addMembershipAttribute(communityId, gsNode);
        addCommunityColor(gsNode);
        FogNode fogNode = (FogNode) graph.getNode(Integer.parseInt(nodeId));
        community.addMember(fogNode);
        fogNode.addCommunity(community);
    }

    public void createSingleMemberCommunities(
            peersim.graph.Graph graph,
            Graph gsGraph){
        for(FogNode node : FogTopology.getFogNodes()){
            if(!node.isInCommunity()){
                importCommunity(graph, gsGraph, node.getID() + "", FogTopology.getNextCommunityId());
            }
        }
    }

    private void addMembershipAttribute(String communityId, Node gsNode) {
        short membershipCount = 0;
        if(gsNode.getAttribute("membership-count") != null)
            membershipCount = gsNode.getAttribute("membership-count");
        gsNode.addAttribute("membership-count", ++membershipCount);
        gsNode.addAttribute("membership-" + membershipCount, communityId);
    }

    private void initializeCommunityColor(Map<String, Color> communityColor, String communityId) {
        if(!communityColor.containsKey(communityId))
            communityColor.put(communityId, RandomColorPicker.pickRandomColor());
    }

    private void addCommunityColor(Node gsNode) {
        if(gsNode.getAttribute("membership-count") != null) {
            short membershipCount = gsNode.getAttribute("membership-count");
            if(membershipCount <= 1) {
                String rgb = getColorRGB(communityColor.get(gsNode.getAttribute("membership-" + 1)));
                gsNode.addAttribute("ui.style", "fill-color: " + rgb + ";");
            }else {
                float pieValues[] = new float[membershipCount];
                String rgb = "fill-color: ";
                for (short i = 1; i <= membershipCount; i++) {
                    rgb+= getColorRGB(communityColor.get(gsNode.getAttribute("membership-" + i)));
                    rgb+= i < membershipCount ? "," : ";";
                    pieValues[i - 1] = 1f/membershipCount;
                }
                gsNode.addAttribute("ui.class", "shared");
                gsNode.addAttribute("ui.pie-values", pieValues);
                gsNode.addAttribute("ui.style",  rgb);
            }
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
