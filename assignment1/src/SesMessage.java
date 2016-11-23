import java.util.*;

// The Schiper-Eggli-Sandoz algorithm Message structure
class SesMessage implements java.io.Serializable {
    private List <ProcessVectorContainer> pvcList;
    private String message;

    public SesMessage(String msg, List<ProcessVectorContainer> pvcList) {
        this.pvcList = new ArrayList<ProcessVectorContainer>(pvcList);
        this.message = msg;
    }

    public void incrementOwnClock(){
        // NOTE !! INCREMENTS CLOCKS OF BOTH SENDING AND RECEIVING PROCESS, SINCE 
        // NO LOSS, DELAY, AND IN-ORDER IS STILL ASSUMED.
        for (ProcessVectorContainer pvc : this.pvcList){
            List <Integer> pvc_l = pvc.getProcessVector();
            String ID_String = pvc.getProcessID();
            ID_String = ID_String.replaceAll("\\D+","");    // Strip off letters.
            int ID = Integer.parseInt(ID_String);
            int value = pvc_l.get(ID-1);                    // Increment own clock with 1.
            value += 1;
            pvc_l.set(ID-1, value);
        }           
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
