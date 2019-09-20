package it.polimi.ppap.protocol.community;

import it.polimi.ppap.topology.FogTopology;
import it.polimi.ppap.topology.community.Community;
import peersim.core.Protocol;

import java.util.Map;
import java.util.TreeMap;

public abstract class MemberStateHolder implements Protocol {

    //Community
    CommunityMemberBehavior communityMemberBehaviour;
    Map<String, MemberState> communityMemberState;
    Map<String, CommunityLeaderBehavior> communityLeaderBehavior;

    public void initializeMember(){
        this.communityMemberBehaviour = new CommunityMemberBehavior(this);
        this.communityMemberState = new TreeMap<>();
        this.communityMemberState = new TreeMap<>();
        this.communityLeaderBehavior = new TreeMap<>();
        for(Community community : FogTopology.getCommunities())
            this.communityMemberState.put(community.getId(), new MemberState(community.getId()));
    }

    public void initializeLeader(String communityId, float optimizationBeta, int referenceControlPeriod){
        this.communityLeaderBehavior.put(
            communityId,
            new CommunityLeaderBehavior(
                getMemberState(communityId),
                optimizationBeta,
                referenceControlPeriod
            )
        );
    }

    public MemberState getMemberState(String communityId) {
        return communityMemberState.get(communityId);
    }

    /**
     * Overrides the clone method, as required by PeerSim.
     * TODO Check if we need to properly implement this (i.e. clone the member's state)
     * @return
     */
    @Override
    public Object clone() {
        MemberStateHolder svh=null;
        try {
            svh=(MemberStateHolder)super.clone();
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return svh;
    }
}
