/*
    java -Djava.security.policy=my.policy -Djava.rmi.server.hostname=local_ip_addr Process 

    Process <This Process> <P0> <ip> <P1> <ip> ... <This Process> <Pn-1> <Pn> <ip>

    Example 3 processes, P0 and P1 on 1 machine and P2 on remote:
        Process P0 P0 P1 localhost P2 <remoteIP_P2>
        Process P1 P0 localhost P1 P2 <remoteIP_P2>
        Process P2 P0 <remoteIP_P0> P1 <remoteIP_P1> P2

*/

import java.util.*;
import java.lang.*;
import java.rmi.Naming; 
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Process {

    static SinghalManager singhalManager;
    static List<SinghalRmi> rmiList;

    static int CSDelay = 0; // For testing

    static void delay_ms(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            System.out.println("Sleep error");
            System.exit(-1);
        }
    }

    public static void setupRmi() {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
    }

    // The client part
    static class SinghalClient extends UnicastRemoteObject implements SinghalRmi {
        private String name;

        public SinghalClient(String myName) throws RemoteException {
            this.name = myName;
        }

        public void sendMessage(SinghalMessage msg) {
            System.out.println("I am " + this.name + " and I received message: " + msg.getMessage());
            // Pass to the algorithm
            new Thread(() -> {
                processMessage(msg);
            }).start();
        }

        public void processMessage(SinghalMessage msg) {
            if (msg.isRequest() == true) {
                singhalManager.receive(msg.getFromIdx(), msg.getFromN());
            } else {
                singhalManager.receive(msg.getToken());
            }
            singhalManager.printStatus();
        }
    }

    public static void setupRmiClient(String clientName) {
        try {
            SinghalClient obj = new SinghalClient(clientName);
            Naming.rebind(clientName, obj);
        } catch (Exception e) {
            System.out.println("Client setup name: " + clientName + " err: " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    // Send a message
    public static void sendRmiMessage(SinghalRmi RmiObj, SinghalMessage msg) {
        try {
            System.out.println("I am SENDING msg: " + msg.getMessage());
            RmiObj.sendMessage(msg);
        } catch (Exception e) {
            System.out.println("Send RMI message err: " + e.getMessage());
            e.printStackTrace();
        }
    }
    //
    // Send a message
    public static void sendRmiMessage(int processIdx, SinghalMessage msg) {
        SinghalRmi RmiObj = rmiList.get(processIdx);
        System.out.println("Sending to index: " + processIdx);
        try {
            System.out.println("I am SENDING msg: " + msg.getMessage());
            RmiObj.sendMessage(msg);
        } catch (Exception e) {
            System.out.println("Send RMI message err: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized void multicast(List<SinghalRmi> rmiSendList, SinghalMessage msg) {
        // Send to all the RMI in rmiSendList emulating a multicast
        for (SinghalRmi rmiObj : rmiSendList) {
            try {
                System.out.println("I am SENDING msg: " + msg.getMessage());
                rmiObj.sendMessage(msg);
            } catch (Exception e) {
                System.out.println("Send RMI message err: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    static class SinghalManager {
        String pid;
        int pidx;
        int size;

        Integer[] N;            // For every PID index the request numbers
        SinghalState[] S;       // State array for n-1 (not self)

        SinghalToken token;

        SinghalManager(String pid, int processIndex, int size) {
            this.pid = pid;
            this.pidx = processIndex;
            this.size = size;

            this.N = new Integer[size];
            this.S = new SinghalState[size];
            System.out.println("Size: " + size);
        }

        public void init(boolean hasToken) {
            int start_o = pidx;
            if (hasToken) {
                this.S[pidx] = SinghalState.H;
                start_o = pidx+1;
                this.token = new SinghalToken(this.size);
            }

            for (int j=0; j<pidx; j++) {
                this.S[j] = SinghalState.R;
            }

            for (int j=start_o; j<this.size; j++) {
                this.S[j] = SinghalState.O;
            }

            for (int j=0; j<this.size; j++) {
                this.N[j] = 0;
            }
        }

        public synchronized void request() {
            List<Integer> mcList = new ArrayList<Integer>();
            this.S[this.pidx] = SinghalState.R;
            this.N[this.pidx] +=1;
            for (int j=0; j<this.size; j++) {
                System.out.println("idx: " + j);
                // Ignore pidx
                if (j==pidx) {
                    continue;
                }
                //System.out.println("Start with idx: " + j);
                if (this.S[j] == SinghalState.R) {
                    send(j, this.pidx, this.N[this.pidx]);
                }
                //System.out.println("End with idx: " + j);
                //System.out.println("");
            }
        }

        /* Send REQUEST */
        public void send(int sendToIdx, int sendFromIdx, int sendFromN) {
            String msgText = "This is a REQUEST message from: " + sendFromIdx;
            SinghalMessage msg = new SinghalMessage(sendFromIdx, sendFromN, null, true, msgText);
            sendRmiMessage(sendToIdx, msg);
        }

        /* Send TOKEN */
        public void send(int sendToIdx, SinghalToken token) {
            String msgText = "This is a TOKEN message from: " + this.pidx;
            SinghalMessage msg = new SinghalMessage(-1, -1, token, false, msgText);
            sendRmiMessage(sendToIdx, msg);
        }

        /* Receive a REQUEST */
        public synchronized void receive(int fromIdx, int fromN) {
            this.N[fromIdx] = fromN;
            switch (this.S[this.pidx]) {
                case E:
                case O:
                    this.S[fromIdx] = SinghalState.R;
                    break;
                case R:
                    if (this.S[fromIdx] != SinghalState.R) {
                        this.S[fromIdx] = SinghalState.R;
                        send(fromIdx, this.pidx, this.N[this.pidx]);
                    }
                    break;
                case H:
                    this.S[fromIdx] = SinghalState.R;
                    this.S[pidx] = SinghalState.O;
                    send(fromIdx, this.token);
            }
        }

        /* Reveive the TOKEN */
        public synchronized void receive(SinghalToken token) {
            /* Save the token to local */
            this.token = token;
        
            this.S[this.pidx] = SinghalState.E;

            /* Critical Section code */
            System.out.println("I am now in the CS! for: " + CSDelay + " s");
            delay_ms(CSDelay * 1000);
            System.out.println("I am now done with the CS");
            /* END critical Section code */

            Integer[] tokenTN = this.token.getTN();
            SinghalState[] tokenTS = this.token.getTS();

            tokenTS[this.pidx] = SinghalState.O;
            this.S[this.pidx] = SinghalState.O;

            for (int j=0; j<this.size; j++) {
                if (this.N[j] > tokenTN[j]) {
                    tokenTN[j] = this.N[j];
                    tokenTS[j] = this.S[j];
                } else {
                    this.N[j] = tokenTN[j];
                    this.S[j] = tokenTS[j];
                }
            }

            // Could increase performance by utting this in the loop above
            boolean noRequests = true;
            int j;
            for (j=0; j<this.size; j++) {
                if (this.S[j] != SinghalState.O) {
                    noRequests = false;
                    break;
                }
            }
            if (noRequests == true) {
                this.S[this.pidx] = SinghalState.H;
            } else {
                send(j, token);
            }
        }

        /* Print status */
        // Quite crude, but ok
        public void printStatus(){
            System.out.println("-----------------------------------");
            System.out.println("PID: " + this.pid);
            
            String sdata = "";
            sdata += ("S[0..." + (this.size - 1) + "]: ");
            sdata += "(";
            for(int i=0; i<this.size; i++){
                switch (this.S[i]) {
                    case R:
                        sdata += "R";
                        break;
                    case E:
                        sdata += "E";
                        break;
                    case H:
                        sdata += "H";
                        break;
                    case O:
                        sdata += "O";
                        break;
                    default:
                        sdata += "Unknown";
                }
                sdata += ",";
            }
            sdata += ")";
            System.out.println(sdata); 

            String ndata = "";
            ndata += ("N[0..." + (this.size - 1) + "]: ");
            ndata += "(";
            for(int i=0; i<this.size; i++){
                ndata += this.N[i];
                ndata += ",";
            }
            ndata += ")";
            System.out.println(ndata); 

            // If this process has the token
            if (this.S[this.pidx] == SinghalState.H) {

                String tsdata = "";
                tsdata += ("TS[0..." + (this.size - 1) + "]: ");
                tsdata += "(";
                SinghalState[] tokenTS = this.token.getTS();
                for(int i=0; i<this.size; i++){
                    switch (tokenTS[i]) {
                        case R:
                            tsdata += "R";
                            break;
                        case E:
                            tsdata += "E";
                            break;
                        case H:
                            tsdata += "H";
                            break;
                        case O:
                            tsdata += "O";
                            break;
                        default:
                            tsdata += "Unknown";
                    }
                    tsdata += ",";
                }
                tsdata += ")";
                System.out.println(tsdata); 

                String tndata = "";
                tndata += ("TN[0..." + (this.size - 1) + "]: ");
                tndata += "(";
                Integer[] tokenTN = this.token.getTN();
                for(int i=0; i<this.size; i++){
                    tndata += tokenTN[i];
                    tndata += ",";
                }
                tndata += ")";
                System.out.println(tndata); 
            }
        }
}
    public static void main (String args[])
    {
        String name = "";
        List<String> processList = new ArrayList<String>();
        List<String> ipList = new ArrayList<String>();
        rmiList = new ArrayList<SinghalRmi>();
        int processIndex = -1;

        if (args.length < 1) {
            System.out.println("Provide arguments please");
            return;
        }

        int j = 0;
        boolean ip = false;
        for (String s: args) {
            // Arg is IP
            if (ip) {
                ip = false;
                ipList.add(s);
                continue;
            }

            // Arg is process
            if (name.equals("")){
                name = s;
            } else {
                if(!s.equals(name)){
                    processList.add(s);
                    ip = true;
                    j++;
                } else {
                    processIndex = j;
                    System.out.println("My index: " + processIndex);
                }
            }
        }

        System.out.println("Other processes:");
        for (String s: processList){
            System.out.printf("%s\t", s);
        }
        System.out.printf("\n");

        System.out.println("Setup local RMI client: " + name);
        setupRmiClient(name);

        System.out.println("Setup RMI security manager");
        setupRmi();

        System.out.println("Opening RMI Connection for provided servers");

        for (int i=0; i<processList.size(); i++) {
            System.out.println("\tOpening RMI Connection for: " + processList.get(i));
            System.out.println("\tOn IP: " + ipList.get(i));
            boolean bound = false;
            while (!bound) {
                try {
                    SinghalRmi obj = (SinghalRmi)Naming.lookup("rmi://" + ipList.get(i) + "/" + processList.get(i));
                    rmiList.add(obj);
                    bound = true;
                    System.out.println("Binding " + processList.get(i) + " succeded!");
                } catch (Exception e) {
                    System.out.println("Bound Error for " + processList.get(i));
                    //System.out.println("Send RMI message err: " + e.getMessage());
                    //e.printStackTrace();
                    delay_ms(1000);
                }
            }
        }

        // Add own rmi entry (null) to keep the list ok for indexing
        rmiList.add(processIndex, null);

        singhalManager = new SinghalManager(name, processIndex, rmiList.size());
        if (processIndex == 0) {
            singhalManager.init(true);
        } else {
            singhalManager.init(false);
        }

        singhalManager.printStatus();
        System.out.println("Start status");
        

        Scanner scanner = new Scanner(System.in);
        boolean done = false;

        while (!done) {
            System.out.println("Enter delay for CS and request T: ");
            int delay  = scanner.nextInt();
            System.out.println("Requesting token and running CS for " + delay + " seconds");
            CSDelay = delay;
            singhalManager.request();
        }

        System.out.println("End");
    }

}
