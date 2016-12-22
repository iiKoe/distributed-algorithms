import java.util.*;

// Singhal message structure
public class GhsMessage implements java.io.Serializable {
    public GhsMessageType msgType;

    public GhsEdge edge;
    public int fragmentLvl;
    public int weight;
    public int  fragmentName;
    public GhsNodeState nodeState;
}
