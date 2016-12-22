import java.util.*;

public class GhsEdge implements java.io.Serializable {
    GhsNodeEdgeState state;
    int weight;
    String conNodeName;
    String ip;

    public GhsEdge(String conNodeName, int weight, String ip) {
        this.weight = weight;
        this.conNodeName = conNodeName;
        this.ip = ip;
        this.state = GhsNodeEdgeState.UNKNOWN_IN_MST;
    }

    public void setState(GhsNodeEdgeState newState) {
        this.state = newState;
    }

    public GhsNodeEdgeState getState() {
        return this.state;
    }

    public int getWeight() {
        return this.weight;
    }

    public String getConNodeName() {
        return this.conNodeName;
    }

    public String getConNodeIP() {
        return this.ip;
    }

    public boolean equals(GhsEdge compare) {
        if (this.weight == compare.getWeight()) {
            return true;
        }
        return false;
    }
}
