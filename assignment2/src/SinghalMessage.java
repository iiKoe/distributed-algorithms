import java.util.*;

// Singhal message structure
class SinghalMessage implements java.io.Serializable {
    private SinghalToken token;
    private int fromIdx;
    private int fromN;
    private String message;
    private boolean isRequest;

    // TODO make different constructors for REQUEST and TOKEN message
    public SinghalMessage (int fromIdx, int fromN, SinghalToken token, boolean isRequest, String msg) {
        this.fromIdx = fromIdx;
        this.fromN = fromN;
        this.isRequest = isRequest;
        this.token = token;
        this.message = msg;
    }

    public String getMessage() {
        return this.message;
    }

    public int getFromIdx() {
        return this.fromIdx;
    }

    public int getFromN() {
        return this.fromN;
    }

    public SinghalToken getToken() {
        return this.token;
    }

    public boolean isRequest() {
        return this.isRequest;
    }
}
