package container;

public class ReplicationInfo {
    private String replicationID;
    private long offset;

    public ReplicationInfo() {
        this.replicationID = "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";
        this.offset = 0;
    }

    public String getReplicationID() {
        return replicationID;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }
}
