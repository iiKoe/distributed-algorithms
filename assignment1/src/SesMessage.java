import java.util.*;

// The Schiper-Eggli-Sandoz algorithm Message structure
class SesMessage implements java.io.Serializable {
    private List <Integer> localVector;
    private String processID;
    private List <ProcessVectorContainer> pvcList;
    private String message;

    public SesMessage(String msg, String processID, List<Integer> localVector, List<ProcessVectorContainer> pvcList) {
        this.localVector = new ArrayList<Integer>(localVector);
        this.processID = processID;
        this.pvcList = new ArrayList<ProcessVectorContainer>(pvcList);
        this.message = msg;
    }

    public String getMessage() {
        return this.message;
    }


    public List<ProcessVectorContainer> getPvcList() {
        return this.pvcList;
    }
    
    public String getSenderProcessID() {
        return this.processID;
    }

    public List<Integer> getLocalVector() {
        return this.localVector;
    }

    public String toString() {
        String res = "";
        res += "Message: " + this.message + "\n";
        res += "Process vector combinations:\n";
        for (ProcessVectorContainer pvc: this.pvcList) {
            res += "\tProcess: " + pvc.getProcessID();
            res += "\tVector: " + pvc.getProcessVector();
        }
        res += "\n";
        return res;
    }
}
