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
        //this.pvcList = new ArrayList<ProcessVectorContainer>(pvcList);
        this.pvcList = new ArrayList<ProcessVectorContainer>();
        for (ProcessVectorContainer pvc : pvcList) {
            this.pvcList.add(new ProcessVectorContainer(pvc.getProcessID(), new ArrayList<Integer>(pvc.getProcessVector())));
        }
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
        res += "Message vector: " + this.localVector + "\n";
        res += "Message Sender: " + this.processID + "\n";
        res += "Process vector combinations (history):\n";
        for (ProcessVectorContainer pvc: this.pvcList) {
            res += "\tProcessID: " + pvc.getProcessID() + "\n";
            res += "\tVector: " + pvc.getProcessVector() + "\n";
        }
        res += "\n";
        return res;
    }
}
