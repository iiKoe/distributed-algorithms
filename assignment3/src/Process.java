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

        public boolean equals(GhsEdge compare) {
            if (this.weight == compare.getWeight()) {
                return true;
            }
            return false;
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

        public int getBestKnownWeight() {
            return this.bestKnownWeight;
        }

        public void setFragmentLvl(int lvl) {
            this.fragmentLvl = lvl;
        }

        public void setFragmentName(String name) {
            this.fragmentName = name;
        }

        public void setState(GhsNodeState state) {
            this.state = state;
        }

        public void setLeadsCoreEdge(GhsEdge edge) {
            this.leadsCoreEdge = edge;
        }

        public void setTestingEdge(GhsEdge edge) {
            this.testingEdge = edge;
        }

        public void setBestKnownWeight(int weight) {
            this.bestKnownWeight = weight;
        }

        public List<GhsEdge> getEdges() {
            return this.edges;
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
        public void wakeup() {
            int minWeight = Integer.MAX_VALUE;
            GhsEdge minEdge = null;

            for (GhsEdge adjEdge : node.getEdges()) {
                if (adjEdge.getState() == GhsNodeEdgeState.UNKNOWN_IN_MST) {
                    if (adjEdge.getWeight() < minWeight) {
                        minWeight = adjEdge.getWeight();
                        minEdge = adjEdge;
                    }
                }
            }

            if (minEdge == null) {
                return;
            }

            minEdge.setState(GhsNodeEdgeState.IN_MST);
            node.setFragmentLvl(0);
            node.setState(GhsNodeState.FOUND);
            node.setFindCount(0);
            // send connect
            send(0, minEdge);
        }

        // III. Receiving a Connect Message
        public void receiveConnect(GhsNode node, GhsEdge edge, int lvl) {
            if (node.getState() == GhsNodeState.SLEEPING) {
                wakeup();
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
        public void receiveInitiate(GhsNode node, int lvl, String fragmentName, GhsNodeState nodeState, GhsEdge edge) {
            node.setFragmentLvl(lvl); 
            node.setFragmentName(fragmentName);
            node.setState(nodeState);

            node.setLeadsCoreEdge(edge);
            node.setTestingEdge(null);
            node.setBestKnownWeight(Integer.MAX_VALUE);

            for (GhsEdge adjEdge : node.getEdges()) {
                if (adjEdge.getState() != GhsNodeEdgeState.IN_MST) {
                    continue;
                }

                // Send initiate
                send(adjEdge, lvl, fragmentName, nodeState);
                if (nodeState == GhsNodeState.FIND) {
                    node.addFindCount(1);
                }
            }

            if (nodeState == GhsNodeState.FIND) {
                test();
            }
        }

        // V. Test()
        public void test(GhsNode node) {
            int minWeight = Integer.MAX_VALUE;
            GhsEdge minEdge = null;

            for (GhsEdge adjEdge : node.getEdges()) {
                if (adjEdge.getState() == GhsNodeEdgeState.UNKNOWN_IN_MST) {
                    if (adjEdge.getWeight() < minWeight) {
                        minWeight = adjEdge.getWeight();
                        minEdge = adjEdge;
                    }
                }
            }
            if (minEdge != null) {
                node.setTestingEdge(minEdge);
                // Send test
                send(node.getFragmentLvl(), node.getFragmentName(), minEdge);
            } else {
                // No edge in ?_IN_MST
                node.setTestingEdge(null);
                report();
            }

        }

        // VI. Receiving a Test Message
        public void receiveTest(int fragmentLvl, String fragmentName, GhsEdge edge) {
            if (node.getState() == GhsNodeState.SLEEPING) {
                wakeup();
            } if (fragmentLvl > node.getFragmentLvl()) {
                // Append message to the queue
            } else {
                if (!fragmentName.equals(node.getFragmentName())) {
                    // Send accept
                    send(edge);
                } else {
                    if (edge.getState() == GhsNodeEdgeState.UNKNOWN_IN_MST) {
                        edge.setState(GhsNodeEdgeState.NOT_IN_MST);
                    } else if (!node.getTestingEdge().equals(edge)) {
                        // Send reject
                        send(edge);
                    } else {
                        test();
                    }
                }
            }
        }

        // VII. Receiving a Reject Message
        public void receiveReject(GhsNode node, GhsEdge edge) {
            if (edge.getState() == GhsNodeEdgeState.UNKNOWN_IN_MST) {
                edge.setState(GhsNodeEdgeState.NOT_IN_MST);
            }
            test();
        }

        // VIII. Receiving an Accept Message
        public void receiveAccept(GhsNode node, GhsEdge edge) {
            node.setTestingEdge(null);
            if (edge.getWeight() < node.getBestKnownWeight()) {
                node.setLeadsBestCanidate(edge);
                node.setBestKnownWeight(edge.getWeight());
            }
            report(); 
        }

        // IX. Report()
        public void report(GhsNode node) {
            if ((node.getFindCount() == 0) && (node.getTestingEdge() == null)) {
                node.setState(GhsNodeState.FOUND);
                // Send report
                send(node.getLeadsCoreEdge(), node.getBestKnownWeight())
            }
        }

        // X. Receiving a Report Message
        public void receiveReport(GhsEdge edge, int bestKnownWeight) {
            if (!edge.equals(node.getLeadsCoreEdge())) {
                node.addFindCount(-1);
                if (bestKnownWeight < node.getBestKnownWeight()) {
                    node.setBestKnownWeight(bestKnownWeight);
                    node.setLeadsBestCanidate(edge);
                }
                report();
            } else {
                if (node.getState() == GhsNodeState.FIND) {
                    // Put message on queue
                } else {
                    if (bestKnownWeight > node.getBestKnownWeight()) {
                        changeRoot();
                    } else {
                        if (bestKnownWeight == node.getBestKnownWeight() == Integer.MAX_VALUE) {
                            System.out.println("$$$$HALT\n\n\n");
                        }
                    }
                }
            }
             
        }

        // XI. Change-Root()
        public void changeRoot() {
            if (edge.getState() == GhsNodeEdgeState.IN_MST) {
                // Send change root on best edge
                send(node.getLeadsBestCanidate());
            } else {
                // Send connect on best edge
                send(node.getLeadsBestCanidate());
                edge.setState(GhsNodeEdgeState.IN_MST);
            }
        }

        public void receiveChangeRoot() {
            changeRoot();
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
