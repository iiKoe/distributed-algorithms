import java.util.*;

// The Schiper-Eggli-Sandoz algorithm Message structure
class SesMessage implements java.io.Serializable {
    private String processID;
    private List<Integer> processVector;
    private String message;

    public SesMessage(String id, List<Integer> vec, String msg) {
        this.processID = id;
        this.processVector = new ArrayList<Integer>(vec);
        this.message = msg;
    }

    public String getProcessID() {
        return this.processID;
    }

    public String getMessage() {
        return this.message;
    }
}
