package it.polimi.ppap.topology;

import it.polimi.ppap.topology.community.Community;
import it.polimi.ppap.topology.community.in.ImportSLPACommunities;
import it.polimi.ppap.topology.node.FogNode;
import peersim.core.Network;
import scala.Int;

import java.util.*;


public class FogTopology{

    private static Map<String, Community> communities = new TreeMap<>();

    public static void addCommunity(Community community){
        communities.put(community.getId(), community);
    }

    public static Community initializeCommunity(String communityId) {
        if(!communities.containsKey(communityId))
            addCommunity(new Community(communityId));

        return communities.get(communityId);
    }

    public static Community getCommunity(String id){
        return communities.get(id);
    }

    public static Collection<Community> getCommunities() {
        return communities.values();
    }

    public static Set<FogNode> getFogNodes(){
        Set<FogNode> nodes = new TreeSet<>();
        for(int i = 0; i < Network.size(); i++)
            nodes.add((FogNode) Network.get(i));
        return nodes;
    }

    //TODO possibly refactor this
    public static String getNextCommunityId(){
        Optional<Integer> opt = communities.keySet().stream().map(Integer::valueOf).max(Integer::compareTo);
        if(opt.isPresent())
            return "" + (opt.get() + 1);
        else
            return ImportSLPACommunities.BASE_ID;
    }
}
