import java.util.*;

// The Schiper-Eggli-Sandoz algorithm Message structure
class SesMessage implements java.io.Serializable {
    private List <ProcessVectorContainer> pvcList;
    private String message;

    public SesMessage(String msg, List<ProcessVectorContainer> pvcList) {
        this.pvcList = new ArrayList<ProcessVectorContainer>(pvcList);
        this.message = msg;
    }

    public String getMessage() {
        return this.message;
    }

    public List<ProcessVectorContainer> getPvcList() {
        return this.pvcList;
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
