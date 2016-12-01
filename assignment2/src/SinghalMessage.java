import java.util.*;

// Singhal message structure
class SinghalMessage implements java.io.Serializable {
    private String processID;
    private String message;

    public SinghalMessage (String processID, String msg) {
        this.processID = processID;
        this.message = msg;
    }

    static void send(){
        System.out.println("Sending..");
    }
}