/*
    Terminal 1:
        rmiregisty &
        java -Djava.security.policy=my.policy SesProcess p1

    lsof -i :1099
    kill -9 <PID>
*/

import java.util.*;
import java.rmi.Naming; 
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class SesProcess {

    static void delay_ms(long ms) {
        try {
            Thread.sleep(ms);
        } catch(InterruptedException ex) {
            System.out.println("Sleep error");
            System.exit(-1);
        }
    }
    
    // The message buffer
    static class sesMessageBuffer {
        public List<SesMessage> messageBuffer;

        public sesMessageBuffer() {
            this.messageBuffer = new ArrayList<SesMessage>();
        }

        public void add(SesMessage msg) {
            this.messageBuffer.add(msg);
        }
    }

    // The client part
    static class SesClient extends UnicastRemoteObject implements SesRmi {
        private String name;
        private List<Integer> vector;
        
        public SesClient(String myName) throws RemoteException {
            this.name = myName;
            this.vector = new ArrayList<Integer>();
        }
        public void sendMessage(SesMessage msg) {
            System.out.println("I am " + this.name + " and I received a message: " + msg.getMessage());
            System.out.println(msg);
        }
    }

    static class SesMessageManager {
        public List<SesMessage> messageBuffer;
        private int myVectorIndex;

        private String localProcess;
        private List<Integer> localVector;
        private List <ProcessVectorContainer> localPvcList;
        
        SesMessageManager(String process, int vectorIndex) {
            this.localProcess = process;
            this.localVector = new ArrayList<Integer>();
            this.myVectorIndex = vectorIndex;
            this.messageBuffer = new ArrayList<SesMessage>();
            this.localPvcList = new ArrayList<ProcessVectorContainer>();
        }
        // Add a new message to the buffer and return if a message can be
        // further used (returns the passed message if it is the next in line)
        // Returns null if no messages are OK
        public SesMessage add(SesMessage msg) {
            // Check if the message can be accepted (vector for this one is OK)
            List<ProcessVectorContainer> newPvcList = msg.getPvcList();
            ProcessVectorContainer newPvc = findPvc(this.localProcess, newPvcList);
            
            if (checkPvc(newPvc) == true) {
                // Update and merge the PVC list
                //updatePvc();
                
                // Increment the current clock
                incrementMyClock();

                // Return this message as it is ready to be accepted
                return msg;
            } else {
                // Buffer the message
                this.messageBuffer.add(msg);
            }

            return null;
        }

        public void incrementMyClock() {
            int newClock = this.localVector.get(this.myVectorIndex) + 1;
            this.localVector.set(this.myVectorIndex, newClock);
        }

        public ProcessVectorContainer findPvc(String process, List<ProcessVectorContainer> pvcList) {
            for (ProcessVectorContainer pvc: pvcList) {
                if (process == pvc.getProcessID()) {
                    return pvc;
                }
            }
            return null;
        }

        public boolean checkPvc(ProcessVectorContainer pvc) {
            if (pvc == null) {
                // The process was not in the pvc list, so the message can be accepted. 
                return true;
            }

            int currentClock = this.localVector.get(this.myVectorIndex);
            int newClock = pvc.getProcessVector().get(this.myVectorIndex);
        
            if (currentClock >= newClock) {
                // Message is older or the same, can be processed
                return true;
            } else {
                // Message needs to be buffered
                return false;
            }
        }
        
    }


    public static void setupRmi() {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
    }

    public static void setupRmiClient(String clientName) {
        try {
            SesClient obj = new SesClient(clientName);
            Naming.rebind(clientName, obj);
        } catch (Exception e) {
            System.out.println("Client setup name: " + clientName + " err: " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    // Send a message
    public static void sendMessage(SesRmi RmiObj, SesMessage msg) {
        try {
            System.out.println("I am SENDING msg: " + msg.getMessage());
            RmiObj.sendMessage(msg);
            msg.incrementOwnClock();
        } catch (Exception e) {
            System.out.println("Send RMI message err: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Input sentence to send
    public static ArrayList<String> inputSentence(){
        ArrayList<String> sentence = new ArrayList<String>();
        Scanner scan = new Scanner(System.in);

        try {
            System.out.println("Input your sentence to send. \n words separated with enter, end is indicated with a dot.");

            String word = scan.nextLine();
            while(!word.equalsIgnoreCase("."))
            {
                sentence.add(word);
                word = scan.nextLine();
            }
        } catch (Exception e) {
            System.out.println("Input err: ");
            e.printStackTrace();
        }
        return sentence;
    }

    // Print sentence as check
    public static void printSentence (ArrayList<String> sentence){
        for (String s: sentence){
                System.out.printf("%s ",s);
        }
        System.out.printf("\n");
    }

    // Send all strings in ArrayList<String> once.
    // Hardcoded out of order and delay ?

    public static void main(String args[]) {
        String name = "";
        List<String> processList = new ArrayList<String>();
        List<SesRmi> rmiList = new ArrayList<SesRmi>();


        if (args.length < 1) {
            System.out.println("Provide arguments please");
            return;
        }
        
        for (String s: args) {
            if (name == "")
                name = s;
            else
                processList.add(s);
        }

        System.out.println("Using name: " + name);

        System.out.println("Other processes:");
        for (String s: processList) {
            System.out.println("\t" + s);
        }

        System.out.println("Setup local RMI client: " + name);
        setupRmiClient(name);

        System.out.println("Setup RMI security manager");
        setupRmi();

        System.out.println("Opening RMI Connection for provided servers");

        for (String s: processList) {
            System.out.println("\tOpening RMI Connection for: " + s);
            boolean bound = false;
            while (!bound) {
                try {
                    SesRmi obj = (SesRmi)Naming.lookup("//localhost/" + s);
                    rmiList.add(obj);
                    bound = true;
                    System.out.println("Binding " + s + " succeded!");
                } catch (Exception e) {
                    System.out.println("Bound Error for " + s);
                    System.out.println("Send RMI message err: " + e.getMessage());
                    e.printStackTrace();
                    delay_ms(1000);
                }
            }
        }

        // Input sentence which will be send with random delays to other process.
        //ArrayList <String> sentence = inputSentence();
    
        int cnt=0;

        List<Integer> vector1 = new ArrayList<Integer>();
        List<Integer> vector2 = new ArrayList<Integer>();
        vector1.add(0);
        vector1.add(0);
        vector1.add(0);

        vector2.add(0);
        vector2.add(0);
        vector2.add(0);

        ProcessVectorContainer pvc1 = new ProcessVectorContainer("Test process 1", vector1);
        ProcessVectorContainer pvc2 = new ProcessVectorContainer("Test process 2", vector2);

        List<ProcessVectorContainer> processVectorList = new ArrayList<ProcessVectorContainer>();
        processVectorList.add(pvc1);
        processVectorList.add(pvc2);

        while (rmiList.size() != 0) {
            System.out.println("Infinite loop");
            for (SesRmi obj: rmiList) {
                System.out.println("Sending message");
                SesMessage msg = new SesMessage("Test message: " + (cnt++), processVectorList);
                sendMessage(obj, msg);
                delay_ms(5000);
            }
        }

        System.out.println("End");
    }
}
