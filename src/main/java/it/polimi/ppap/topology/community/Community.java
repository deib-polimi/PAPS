package it.polimi.ppap.topology.community;

import it.polimi.ppap.topology.node.FogNode;

import java.util.Set;
import java.util.TreeSet;

public class Community {

    private final String id;
    private Set<FogNode> nodes = new TreeSet<>();
    private FogNode leader = null;

    public Community(String communityId) {
        this.id = communityId;
    }

    public String getId() {
        return id;
    }

    public boolean add(FogNode fogNode) {
        return nodes.add(fogNode);
    }

    public FogNode electLeader() throws CommunityLeaderNotFoundException {
        for(FogNode node : nodes) {
            setLeader(node);
            return node;
        }
        throw new CommunityLeaderNotFoundException("The community is empty or has no electable node");
    }

    public void setLeader(FogNode leader) {
        this.leader = leader;
    }

    public FogNode getLeader() throws CommunityLeaderNotFoundException {
        if(leader != null)
            return leader;
        else
            throw new CommunityLeaderNotFoundException("The community has no elected leader");
    }

    public boolean isLeader(FogNode node) {
        return this.leader.equals(node);
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(id);
    }

    @Override
    public boolean equals(Object obj) {
        return this.id.equals(((Community) obj).id);
    }

    public int size() {
        return this.nodes.size();
    }
}