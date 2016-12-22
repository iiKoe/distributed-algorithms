import java.util.*;

public class GhsEdge {
    GhsNodeEdgeState state;
    int weight;
    String conNodeName;

    public GhsEdge(String conNodeName, int weight, GhsNodeEdgeState state) {
        this.state = state;
        this.weight = weight;
        this.conNodeName = conNodeName;
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

    public boolean equals(GhsEdge compare) {
        if (this.weight == compare.getWeight()) {
            return true;
        }
        return false;
    }
}
