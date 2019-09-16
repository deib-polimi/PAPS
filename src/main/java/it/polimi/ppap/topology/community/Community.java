package it.polimi.ppap.topology.community;

public class Community {

    private final String id;
    public Community(String communityId) {
        this.id = communityId;
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(id);
    }

    @Override
    public boolean equals(Object obj) {
        return this.id.equals(((Community) obj).id);
    }
}
