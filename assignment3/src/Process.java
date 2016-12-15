/*
    java -Djava.security.policy=my.policy -Djava.rmi.server.hostname=local_ip_addr Process 
*/

import java.util.*;
import java.lang.*;
import java.rmi.Naming; 
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Process {

    // TODO Manager Object
    static List<Rmi> rmiList;

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
    static class Client extends UnicastRemoteObject implements Rmi {
        private String name;

        public Client(String myName) throws RemoteException {
            this.name = myName;
        }

        public void sendMessage(Message msg) {
            System.out.println("I am " + this.name + " and I received message: " + msg.getMessage());
        }
    }

    public static void setupRmiClient(String clientName) {
        try {
            Client obj = new Client(clientName);
            Naming.rebind(clientName, obj);
        } catch (Exception e) {
            System.out.println("Client setup name: " + clientName + " err: " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    // Send a message
    public static void sendRmiMessage(Rmi RmiObj, Message msg) {
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
    public static void sendRmiMessage(int processIdx, Message msg) {
        Rmi RmiObj = rmiList.get(processIdx);
        System.out.println("Sending to index: " + processIdx);
        try {
            System.out.println("I am SENDING msg: " + msg.getMessage());
            RmiObj.sendMessage(msg);
        } catch (Exception e) {
            System.out.println("Send RMI message err: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized void multicast(List<Rmi> rmiSendList, Message msg) {
        // Send to all the RMI in rmiSendList emulating a multicast
        for (Rmi rmiObj : rmiSendList) {
            try {
                System.out.println("I am SENDING msg: " + msg.getMessage());
                rmiObj.sendMessage(msg);
            } catch (Exception e) {
                System.out.println("Send RMI message err: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static enum GhsNodeState {
        SLEEPING,
        FIND,
        FOUND
    }

    public static enum GhsNodeEdgeState {
        UNKNOWN_IN_MST,
        IN_MST,
        NOT_IN_MST
    }

    static class GhsEdge {
        GhsNodeEdgeState state;
        int weight;

        GhsEdge (int weight){
            this.state = GhsNodeEdgeState.UNKNOWN_IN_MST;
            this.weight = weight;
        }

        // What do you want to do here ?
        public GhsEdge() {

        }

        public void setState(GhsNodeEdgeState newState) {
            this.state = newState;
        }

        public GhsNodeEdgeState getState() {
            return this.state;
        }

        public int getWeight() {
            return this.weight;
        }
    }

    static class GhsNode {
        GhsNodeState state;
        
        List<GhsEdge> edges = new ArrayList<GhsEdge>();
        
        // (a) the name of the current fragment it belongs to
        String fragmentName;
        // (b) the level of the current fragment it belongs to
        int fragmentLvl;
        // (c) the edge adjacent to it that leads to the core of the current fragment it belongs to
        GhsEdge leadsCoreEdge;
        // (d) the number of *report* messages it still expects
        int pendingReportMessages;
        // (e) the edge adjacent to it that leads towords the best canidate for the MOE it knows about
        GhsEdge leadsBestCanidate;
        // (f) the weigt of the best canidate for the MOE it knows about
        int bestKnownWeight;
        // (g) the edge adjacent to it that is currently testing for being a canidate MOE
        GhsEdge testingEdge;


        public GhsNode() {
            
        }

        public void addFindCount(int delta) {
            this.pendingReportMessages += delta;
        }

        public GhsNodeState getState() {
            return this.state;
        }

        public int getFragmentLvl() {
            return this.fragmentLvl;
        }

        public String getFragmentName() {
            return this.fragmentName;
        }

        public GhsEdge getTestingEdge() {
            return this.testingEdge;
        }

        public GhsEdge getLeadsCoreEdge() {
            return this.leadsCoreEdge;
        }

        public void setState (GhsNodeState newState){
            this.state = newState;
        }

        public void setFragmentLvl(int newLevel){
            this.fragmentLvl = newLevel;
        }

        public void setFragmentName(String newName){
            this.fragmentName = newName;
        }

        public void setLeadsCoreEdge(GhsEdge newLead){
            this.leadsCoreEdge = newLead;
        }

        public void setBestKnownWeight(int newWeight){
            this.bestKnownWeigth = newWeight;
        }

    }

    // Manager
    static class GhsManager {
        

        public GhsManager() {
            System.out.println("Init Gallager's, Humblet's and Spira's algorithm");
        }

        // Send message
        public void send() {

        }

        // II. Wakeup
        public void wakeup(GhsNode node, GhsNodeState newState) {
            //node.state = GhsNodeState.FIND;
            node.setState(newState);
        }

        // III. Receiving a Connect Message
        public void receiveConnect(GhsNode node, GhsEdge edge, int lvl) {
            if (node.getState() == GhsNodeState.SLEEPING) {
                wakeup(node, GhsNodeState.FIND);
            }
            if (lvl < node.getFragmentLvl()) {
                edge.setState(GhsNodeEdgeState.IN_MST);
                // send Initiate
                send(node.getFragmentLvl(), node.getFragmentName(), node.getState());
                if (node.getState() == GhsNodeState.FIND) {
                    node.addFindCount(1);
                }
            } else if (edge.getState() == GhsNodeEdgeState.UNKNOWN_IN_MST) {
                // TODO
                messageQueue.append();
            } else {
                // send Initiate
                send(node.getFragmentLvl()+1, edge.getWeight(), GhsNodeState.FIND);
            }

        }

        // IV. Receiving an Initiate Message
        public void receiveInitiate(GhsNode node, GhsEdge edge, int lvl, String fragmentName, GhsNodeEdgeState S) {
            
            // upon receipt of (initiate; L,F,S) on edge j do 
            node.setFragmentLvl(lvl);
            node.setFragmentName(fragmentName);
            // doe iets fout hier denk ik node.setState(S);
            node.setLeadsCoreEdge(edge);
            node.setBestKnownWeight(Integer.MAX_VALUE);



        }

        // V. Test()
        public void test() {


        }

        // VI. Receiving a Test Message
        public void receiveTest(GhsNode node, GhsEdge edge, int lvl, String F) {
            if (node.getState() == GhsNodeState.SLEEPING){
                wakeup(node, GhsNodeState.FIND);
            }

            if (lvl > node.getFragmentLvl()){
                // Append msg to queue
            } else {
                if (! node.getFragmentName().equals(F)){
                    // send(accept) on edge j
                } else {
                    if (edge.getState() == GhsNodeEdgeState.UNKNOWN_IN_MST){
                        edge.setState(GhsNodeEdgeState.NOT_IN_MST);
                    }

                    // if (test-edge != j) then send(reject) on j...
                    // else test()
                }
            }

        }

        // VII. Receiving a Reject Message
        public void receiveReject() {

        }

        // VIII. Receiving an Accept Message
        public void receiveAccept() {

        }

        // IX. Report()
        public void report() {

        }

        // X. Receiving a Report Message
        public void receiveReport() {

        }

        // XI. Change-Root()
        public void changeRoot() {

        }
    }


    public static void main (String args[])
    {
        String name = "";
        List<String> processList = new ArrayList<String>();
        List<String> ipList = new ArrayList<String>();
        rmiList = new ArrayList<Rmi>();
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
                    Rmi obj = (Rmi)Naming.lookup("rmi://" + ipList.get(i) + "/" + processList.get(i));
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

        //singhalManager = new SinghalManager(name, processIndex, rmiList.size());
        // TODO Init manager
        

        Scanner scanner = new Scanner(System.in);
        boolean done = false;

        // For proving correctness
        while (!done) {
            System.out.println("Enter delay for CS and request T: ");
            int delay  = scanner.nextInt();
            System.out.println("Requesting token and running CS for " + delay + " seconds");
            CSDelay = delay;
        }

        System.out.println("End");
    }

}
