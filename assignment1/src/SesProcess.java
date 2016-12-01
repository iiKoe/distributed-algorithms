/*
    Run using:
        rmiregisty &
        java -Djava.security.policy=my.policy SesProcess p1 p1 p2
        First argument (here p1) is the name of this process
        Next arguments (here p1 p2) are the vector clock ordering of the remaining processes
*/

import java.util.*;
import java.lang.*;
import java.rmi.Naming; 
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.lang.Math;


public class SesProcess {

    static void delay_ms(long ms) {
        try {
            Thread.sleep(ms);
        } catch(InterruptedException ex) {
            System.out.println("Sleep error");
            System.exit(-1);
        }
    }
    
    // The client part
    static class SesClient extends UnicastRemoteObject implements SesRmi {
        private String name;
        private List<Integer> vector;
        private SesMessageManager myMessageManager;
        
        public SesClient(String myName, SesMessageManager myManager) throws RemoteException {
            this.name = myName;
            this.myMessageManager = myManager;
        }
        public void sendMessage(SesMessage msg) {
            System.out.println("I am " + this.name + " and I received a message: " + msg.getMessage());
            System.out.println(msg);
            List<SesMessage> msgList = myMessageManager.add(msg);
            System.out.println("Ready messages are: " + msgList);
        }
    }

    static class SesMessageManager {
        public List<SesMessage> messageBuffer;
        private int myVectorIndex;

        private String localProcess;
        private List<Integer> localVector;
        private List <ProcessVectorContainer> localPvcList;
        
        SesMessageManager(String process, int vectorIndex, int vectorSize) {
            this.localProcess = process;
            this.localVector = new ArrayList<Integer>();
            this.myVectorIndex = vectorIndex;
            this.messageBuffer = new ArrayList<SesMessage>();
            this.localPvcList = new ArrayList<ProcessVectorContainer>();

            // Init vectors
            for (int i=0; i<vectorSize; i++) {
                this.localVector.add(0);
            }
        }
        // Add a new message to the buffer and return if a message can be
        // further used (returns the passed message if it is the next in line)
        // Returns null if no messages are OK
        public boolean add(SesMessage msg, List<SesMessage> readyMsgList) {
            // Check if the message can be accepted (vector for this one is OK)
            List<ProcessVectorContainer> newPvcList = msg.getPvcList();
            ProcessVectorContainer newPvc = findPvc(this.localProcess, newPvcList);

            System.out.println("Adding message, local vector: " + this.localVector);

            if (checkPvc(newPvc) == true) {
                System.out.println("Message can be accepted");

                // Remove unneeded entry local PVC list
                removeLocalNotNeeded(msg);

                // Update and merge the PVC list
                mergePvcList(newPvcList);
                
                // Increment the current clock
                incrementMyClock();

                // Merge the local clock
                mergeMyClock(msg.getLocalVector());

                System.out.println("New local clock: " + this.localVector);
                System.out.println("Updated history list:");
                for (ProcessVectorContainer lpvc : this.localPvcList) {
                    System.out.println("\t" + lpvc);
                }

                // Add the received message as the first element to the list
                readyMsgList.add(msg);

                // Remove the message from the buffer if it was in the buffer
                if (this.messageBuffer.contains(msg)) {
                    System.out.println("This was a buffered message, removing it from the buffer");
                    this.messageBuffer.remove(msg);
                } else {
                    System.out.println("This was an origional message, no need to remove from buffer");
                }

                for (int i=0; i<this.messageBuffer.size(); i++) {
                    add(this.messageBuffer.get(i), readyMsgList);
                }
                return true;

            } else {
                // Buffer the message
                System.out.println("Message can NOT be accepted");
                if (this.messageBuffer.contains(msg)) {
                    System.out.println("Message buffer already contains this message");
                } else {
                    System.out.println("Adding message to buffer");
                    this.messageBuffer.add(msg);
                }
            }

            return false;
        }

        public List<SesMessage> add(SesMessage msg) {
            List<SesMessage> readyMsgList = new ArrayList<SesMessage>();
            add(msg, readyMsgList);
            return readyMsgList;
        }

        public void sendMessage(SesRmi rmiObj, SesMessage msg) {
            sendRmiMessage(rmiObj, msg);
        }

        public SesMessage buildMessage(String receiverID, String message) {
            incrementMyClock();
            SesMessage msg = new SesMessage(message, this.localProcess, this.localVector, this.localPvcList);
            System.out.println("Build message:");
            System.out.println("\tHistory:");
            for (ProcessVectorContainer hpvc : msg.getPvcList()) {
                System.out.println("\t\t" + hpvc);
            }

            // Add send message to curren PVC List
            ProcessVectorContainer newPvc = new ProcessVectorContainer(receiverID, this.localVector);
            System.out.println("Adding new PVC to history: " + newPvc);

            // If excists, merge
            mergePvc(newPvc);

            System.out.println("Updated history list:");
            for (ProcessVectorContainer lpvc : this.localPvcList) {
                System.out.println("\t" + lpvc);
            }

            return msg;
        }

        public void removeLocalNotNeeded(SesMessage msg) {
            // If there already is a msg in the history present from the sender, we can assume that it is
            // not needed any more and can be removed?
            ProcessVectorContainer notNeededPvc = findPvc(msg.getSenderProcessID(), this.localPvcList);
            this.localPvcList.remove(notNeededPvc);
        }


        public void mergePvcList(List<ProcessVectorContainer> newPvcList) {
            // Merge the remaining lists
            for (ProcessVectorContainer pvc : newPvcList) {
                mergePvc(pvc);
            }
        }

        public void mergePvc(ProcessVectorContainer pvc) {
            /*
            System.out.println("Merging pvc: " + pvc + " into list:");
            for (ProcessVectorContainer lpvc : this.localPvcList) {
                System.out.println("\t" + lpvc);
            }
            */

            ProcessVectorContainer localPvc = findPvc(pvc.getProcessID(), this.localPvcList);
            if (localPvc == null) {
                // Not found, so add it
                this.localPvcList.add(pvc);
            } else {
                // Already an entry for the process, so update it
                List <Integer> maxv = maxVector(localPvc.getProcessVector(), pvc.getProcessVector());
                localPvc.setProcessVector(maxv);
            }
            
        }

        public List<Integer> maxVector(List<Integer> v1, List<Integer> v2) {
            List<Integer> res = new ArrayList<Integer>();
            for (int i=0; i<v1.size(); i++) {
                res.add(Math.max(v1.get(i), v2.get(i))); 
            }
            return res;
        }

        public void incrementMyClock() {
            int newClock = this.localVector.get(this.myVectorIndex) + 1;
            this.localVector.set(this.myVectorIndex, newClock);
        }

        public void mergeMyClock(List<Integer> mergeVector) {
            this.localVector = maxVector(mergeVector, this.localVector);
        }

        public ProcessVectorContainer findPvc(String process, List<ProcessVectorContainer> pvcList) {
            //System.out.println("findPvc, searching for : " + process);
            for (ProcessVectorContainer pvc: pvcList) {
                //System.out.println("\tChecking pvc: " + pvc);
                if (process.equals(pvc.getProcessID())) {
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

            for (int i=0; i<this.localVector.size(); i++) {
                int currentClock = this.localVector.get(i);
                int newClock = pvc.getProcessVector().get(i);

                if (!(currentClock >= newClock)) {
                    // Message needs to be buffered
                    return false;
                }
            }
            return true;
        }

        public void updateBufferAfterDelivery() {
            // Get buffer, take component wise max.
            
            for (SesMessage buff : messageBuffer){
                List <ProcessVectorContainer> buffList = buff.getPvcList();
            }
        }
     
    }


    public static void setupRmi() {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
            System.out.println(System.getSecurityManager());
        }
    }

    public static void setupRmiClient(String clientName, SesMessageManager manager) {
        try {
            SesClient obj = new SesClient(clientName, manager);
            Naming.rebind(clientName, obj);
        } catch (Exception e) {
            System.out.println("Client setup name: " + clientName + " err: " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    // Send a message
    public static void sendRmiMessage(SesRmi RmiObj, SesMessage msg) {
        try {
            System.out.println("I am SENDING msg: " + msg.getMessage());
            RmiObj.sendMessage(msg);
            //msg.incrementOwnClock();
        } catch (Exception e) {
            System.out.println("Send RMI message err: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void main(String args[]) {
        String name = "";
        int vectorIndex = 0;
        int vectorSize = 0;
        List<String> processList = new ArrayList<String>();
        List<SesRmi> rmiList = new ArrayList<SesRmi>();


        if (args.length < 1) {
            System.out.println("Provide arguments please");
            return;
        }

        vectorSize = args.length - 1;
        System.out.println("Using vector size: " + vectorSize);
        
        int j = 0;
        for (String s: args) {
            if (name.equals("")) {
                name = s;
            }
            else {
                if (!s.equals(name)) {
                    processList.add(s);
                    j++;
                } else {
                    vectorIndex = j;
                    System.out.println("Found vector index: " + vectorIndex);
                }
            }
        }


        System.out.println("Using name: " + name);

        System.out.println("Other processes:");
        for (String s: processList) {
            System.out.println("\t" + s);
        }
        
        // Find the vector index



        // The SES Setup
        SesMessageManager sesManager = new SesMessageManager(name, vectorIndex, vectorSize);

        System.out.println("Setup local RMI client: " + name);
        setupRmiClient(name, sesManager);

        System.out.println("Setup RMI security manager");
        setupRmi();

        System.out.println("Opening RMI Connection for provided servers");

        for (String s: processList) {
            System.out.println("\tOpening RMI Connection for: " + s);
            boolean bound = false;
            while (!bound) {
                try {
                    // ****Change localhost to the IP of the process****
                    SesRmi obj = (SesRmi)Naming.lookup("rmi://localhost/" + s);
                    rmiList.add(obj);
                    bound = true;
                    System.out.println("Binding " + s + " succeded!");
                } catch (Exception e) {
                    System.out.println("Bound Error for " + s);
                    //System.out.println("Send RMI message err: " + e.getMessage());
                    //e.printStackTrace();
                    delay_ms(1000);
                }
            }
        }

        Scanner scanner = new Scanner(System.in);
        SesMessage[] msgSendBuffer = new SesMessage[100];
        String[] sendToBuffer = new String[100];
        int cnt = 0;
        boolean done = false;

        while (!done) {
            System.out.println("Command (b=build, s=send): ");
            String command = scanner.next();
            if (command.equals("b")) {
                System.out.println("Building message");

                System.out.println("Enter process name to send to: ");
                String id = scanner.next();
                int pi = processList.indexOf(id);
                if (pi < 0) {
                    System.out.println("Not a correct process name?");
                    continue;
                }
                System.out.println("Enter message: ");
                String send_msg = scanner.next();
                SesMessage msg = sesManager.buildMessage(id, send_msg);
                System.out.println("Created message with index: " + cnt);
                msgSendBuffer[cnt] = msg;
                sendToBuffer[cnt] = id;
                cnt++;

            } else if (command.equals("s")) {
                System.out.println("Sending message");

                System.out.println("Send message index: ");
                int idx = scanner.nextInt();
                SesMessage msg = msgSendBuffer[idx];
                String id = sendToBuffer[idx];
                int pi = processList.indexOf(id);
                System.out.println("Sending message: " + msg);
                sesManager.sendMessage(rmiList.get(pi), msg);
            }
        }

        System.out.println("End");
    }
}
