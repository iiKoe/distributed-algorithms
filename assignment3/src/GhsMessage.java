import java.util.*;

// Singhal message structure
public class GhsMessage implements java.io.Serializable {
    public GhsMessageType msgType;

    public GhsEdge edge;
    public int weight;
    public int fragmentLvl;
    public String fragmentName;
    public GhsNodeState nodeState;
}
