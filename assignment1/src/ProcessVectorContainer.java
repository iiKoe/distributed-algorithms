import java.util.*;

class ProcessVectorContainer implements java.io.Serializable {
    private String processID;
    private List<Integer> processVector;

    public ProcessVectorContainer(String ProcessID, List<Integer> processVector) {
        this.processID = ProcessID;
        this.processVector = new ArrayList<Integer>(processVector);
    }

    public String getProcessID() {
        return this.processID;
    }

    public List<Integer> getProcessVector() {
        return this.processVector;
    }
}
