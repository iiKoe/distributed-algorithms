import java.util.*;

// Singhal message structure
public class GhsMessage implements java.io.Serializable {
    public GhsMessageType msgType;

    public int fragmentLvl;
    public int weight;
    public int bestWeight;
    public int  fragmentName;
    public GhsNodeState nodeState;
}
