package it.polimi.ppap.topology;

import it.polimi.ppap.topology.community.Community;
import it.polimi.ppap.ui.RandomColorPicker;

import java.awt.*;
import java.util.Map;
import java.util.TreeMap;

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
}
