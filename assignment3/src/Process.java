/*
    java -Djava.security.policy=my.policy -Djava.rmi.server.hostname=local_ip_addr Process 
*/

import java.util.*;
import java.util.concurrent.*;
import java.lang.*;
import java.rmi.Naming; 
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Process {

    static Map<String, Rmi> rmiMap = new HashMap<String, Rmi>();
    static GhsManager ghsManager;
    static GhsNode thisNode;
    static String name = "";
    static  ConcurrentLinkedQueue<GhsMessage> messageQueue = new ConcurrentLinkedQueue<GhsMessage>();
    //static List<GhsMessage> pendingMessages = new ArrayList<GhsMessage>();


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

        //public synchronized void sendMessage(GhsMessage msg) {
        public void sendMessage(GhsMessage msg) {
            System.out.println(">>I am " + this.name + " and I received message: " + msg.msgType + " from: " + msg.weight);
            //pendingMessages.add(msg);
            messageQueue.add(msg);
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
    public static void sendRmiMessage(Rmi RmiObj, GhsMessage msg) {
        try {
            System.out.println(">>I am " + name + " and I am SENDING a msg: " + msg.msgType + " on edge: " + msg.weight + " lvl: " + msg.fragmentLvl);
            //System.out.println("Sending on edge: " + msg.weight);
            RmiObj.sendMessage(msg);
        } catch (Exception e) {
            System.out.println("Send RMI message err: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void sendRmiMessage(String rmiName, GhsMessage msg) {
        sendRmiMessage(rmiMap.get(rmiName), msg);
    }


    static class GhsNode {
        GhsNodeState state;
        
        //List<GhsEdge> edges = new ArrayList<GhsEdge>();
        List<GhsEdge> edges;
        
        // (a) the name of the current fragment it belongs to
        int fragmentName;
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


        public GhsNode(List<GhsEdge> edges) {
            this.state = GhsNodeState.SLEEPING; 
            this.edges = edges;
        }

        public int edgeToId(GhsEdge edge) {
            return edge.getWeight();
        }

        public GhsEdge idToEdge(int weight) {
            for (GhsEdge edge : edges) {
                if (edge.getWeight() == weight) {
                    return edge;
                }
            }
            System.out.println("EDGE NOT FOUND\n\n\n");
            return null;
        }

        public void addFindCount(int delta) {
            this.pendingReportMessages += delta;
            System.out.println("fc: " + this.pendingReportMessages);
        }

        public void setFindCount(int fc) {
            this.pendingReportMessages = fc;
            System.out.println("fc: " + this.pendingReportMessages);
        }

        public int getFindCount() {
            return this.pendingReportMessages;
        }

        public GhsNodeState getState() {
            return this.state;
        }

        public int getFragmentLvl() {
            return this.fragmentLvl;
        }

        public int getFragmentName() {
            return this.fragmentName;
        }

        public int getBestKnownWeight() {
            return this.bestKnownWeight;
        }

        public GhsEdge getTestingEdge() {
            return this.testingEdge;
        }

        public GhsEdge getLeadsCoreEdge() {
            return this.leadsCoreEdge;
        }

        public void setFragmentLvl(int lvl) {
            this.fragmentLvl = lvl;
        }

        public void setFragmentName(int  name) {
            this.fragmentName = name;
        }

        public void setState(GhsNodeState state) {
            this.state = state;
        }

        public void setLeadsCoreEdge(GhsEdge edge) {
            this.leadsCoreEdge = edge;
        }
        
        public void setLeadsBestCanidate(GhsEdge edge) {
            this.leadsBestCanidate = edge;
        }
        
        public GhsEdge getLeadsBestCanidate() {
            return this.leadsBestCanidate;
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
        
        GhsNode node;

        GhsMessage currentMessage;

        public GhsManager(GhsNode node) {
            //System.out.println("Init Gallager's, Humblet's and Spira's algorithm");
            this.node = node;
        }

        public void sendStopAll() {
            for (GhsEdge adjEdge : node.getEdges()) {
                GhsMessage msg = new GhsMessage();
                msg.weight = adjEdge.getWeight();
                msg.stop = true;
                sendMsg(msg);
            }
        }


        public void parseMessage(GhsMessage msg) {
        //public void parseMessage(GhsMessage msg) {
            this.currentMessage = msg;

            if (msg.stop == true) {
                // Propegate stop and stop
                System.out.println("Got HALT, someone found the MST");
                sendStopAll();
                System.exit(1);
            }

            System.out.println(">>I am " + name + " and I am parsing message: " + msg.msgType + " from: " + msg.weight);

            switch (msg.msgType) {
                case CONNECT:
                    receiveConnect(node.idToEdge(msg.weight), msg.fragmentLvl);
                    break;
                case INITIATE:
                    receiveInitiate(node.idToEdge(msg.weight), msg.fragmentLvl, msg.fragmentName, msg.nodeState);
                    break;
                case TEST:
                    receiveTest(node.idToEdge(msg.weight), msg.fragmentLvl, msg.fragmentName);
                    break;
                case ACCEPT:
                    receiveAccept(node.idToEdge(msg.weight));
                    break;
                case REJECT:
                    receiveReject(node.idToEdge(msg.weight));
                    break;
                case REPORT:
                    receiveReport(node.idToEdge(msg.weight), msg.bestWeight);
                    break;
                case CHANGEROOT:
                    receiveChangeRoot();
                    break;
                default:
                    System.out.println("Unknown message type");
            }
            //printNode();
            //printEdges(thisNode.getEdges());
            //System.out.flush();
            //System.out.println("\n****************");
        }

        // II. Wakeup
        public void wakeup() {
            System.out.println(">>Wakeup");
            int minWeight = Integer.MAX_VALUE;
            GhsEdge minEdge = null;

            for (GhsEdge adjEdge : node.getEdges()) {
                if (adjEdge.getWeight() < minWeight) {
                    minWeight = adjEdge.getWeight();
                    minEdge = adjEdge;
                }
            }

            minEdge.setState(GhsNodeEdgeState.IN_MST);
            node.setFragmentLvl(0);
            node.setState(GhsNodeState.FOUND);
            node.setFindCount(0);
            // send connect
            sendConnect(minEdge, 0);
        }

        // III. Receiving a Connect Message
        public void receiveConnect(GhsEdge edge, int lvl) {
            if (node.getState() == GhsNodeState.SLEEPING) {
                wakeup();
            }
            if (lvl < node.getFragmentLvl()) {
                edge.setState(GhsNodeEdgeState.IN_MST);
                // send Initiate
                sendInitiate(edge, node.getFragmentLvl(), node.getFragmentName(), node.getState());
                if (node.getState() == GhsNodeState.FIND) {
                    node.addFindCount(1);
                }
            } else {
                if (edge.getState() == GhsNodeEdgeState.UNKNOWN_IN_MST) {
                    System.out.println(">> Add queue: receiveConnect");
                    messageQueue.add(this.currentMessage);
                } else {
                    // send Initiate
                    sendInitiate(edge, node.getFragmentLvl()+1, edge.getWeight(), GhsNodeState.FIND);
                }
            }

        }

        // IV. Receiving an Initiate Message
        public void receiveInitiate(GhsEdge edge, int lvl, int fragmentName, GhsNodeState nodeState) {
            node.setFragmentLvl(lvl); 
            node.setFragmentName(fragmentName);
            node.setState(nodeState);

            node.setLeadsCoreEdge(edge);
            node.setLeadsBestCanidate(null);
            node.setBestKnownWeight(Integer.MAX_VALUE);

            for (GhsEdge adjEdge : node.getEdges()) {
                if ((adjEdge.getState() == GhsNodeEdgeState.IN_MST) && (adjEdge != edge)) {
                    // Send initiate
                    sendInitiate(adjEdge, lvl, fragmentName, nodeState);
                    if (nodeState == GhsNodeState.FIND) {
                        node.addFindCount(1);
                    }
                }
            }

            if (nodeState == GhsNodeState.FIND) {
                test();
            }
        }

        // V. Test()
        public void test() {
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
                sendTest(minEdge, node.getFragmentLvl(), node.getFragmentName());
            } else {
                // No edge in ?_IN_MST
                node.setTestingEdge(null);
                report();
            }
        }

        // VI. Receiving a Test Message
        public void receiveTest(GhsEdge edge, int fragmentLvl, int fragmentName) {
            if (node.getState() == GhsNodeState.SLEEPING) {
                wakeup();
            } if (fragmentLvl > node.getFragmentLvl()) {
                // Append message to the queue
                System.out.println(">> Add queue: receiveTest");
                messageQueue.add(this.currentMessage);
            } else {
                //System.out.println(">> Got fragmentName: " + fragmentName + " my fragmentName: " + node.getFragmentName());
                if (fragmentName != node.getFragmentName()) {
                    // Send accept
                    //System.out.println(">> Send accept");
                    sendAccept(edge);
                } else {
                    if (edge.getState() == GhsNodeEdgeState.UNKNOWN_IN_MST) {
                        edge.setState(GhsNodeEdgeState.NOT_IN_MST);
                    //} else if (node.getTestingEdge().getWeight() != edge.getWeight()) {
                    }
                    // NB. Null test added!
                    if ((node.getTestingEdge() != null) && (node.getTestingEdge().getWeight() != edge.getWeight())) {
                        // Send reject
                        sendReject(edge);
                    } else {
                        test();
                    }
                }
            }
        }

        // VII. Receiving a Reject Message
        public void receiveReject(GhsEdge edge) {
            if (edge.getState() == GhsNodeEdgeState.UNKNOWN_IN_MST) {
                edge.setState(GhsNodeEdgeState.NOT_IN_MST);
            }
            test();
        }

        // VIII. Receiving an Accept Message
        public void receiveAccept(GhsEdge edge) {
            node.setTestingEdge(null);
            System.out.println(">> Accept: edgeWeight: " + edge.getWeight() + " bkw: " + node.getBestKnownWeight());
            if (edge.getWeight() < node.getBestKnownWeight()) {
                node.setLeadsBestCanidate(edge);
                node.setBestKnownWeight(edge.getWeight());
            }
            report(); 
        }

        // IX. Report()
        public void report() {
            System.out.println(">> Report findCount: " + node.getFindCount());
            if ((node.getFindCount() == 0) && (node.getTestingEdge() == null)) {
                node.setState(GhsNodeState.FOUND);
                // Send report
                sendReport(node.getLeadsCoreEdge(), node.getBestKnownWeight());
            }
        }

        // X. Receiving a Report Message
        public void receiveReport(GhsEdge edge, int bestKnownWeight) {
            if (edge.getWeight() != node.getLeadsCoreEdge().getWeight()) {
                node.addFindCount(-1);
                if (bestKnownWeight < node.getBestKnownWeight()) {
                    node.setBestKnownWeight(bestKnownWeight);
                    node.setLeadsBestCanidate(edge);
                }
                report();
            } else {
                if (node.getState() == GhsNodeState.FIND) {
                    // Put message on queue
                    System.out.println(">> Add queue: receiveReport");
                    messageQueue.add(this.currentMessage);
                } else {
                    if (bestKnownWeight > node.getBestKnownWeight()) {
                        changeRoot();
                    } else {
                        if (bestKnownWeight == node.getBestKnownWeight() && bestKnownWeight == Integer.MAX_VALUE) {
                            System.out.println("$$$$HALT\n\n\n");
                            sendStopAll();
                            System.exit(1);
                        }
                    }
                }
            }
        }

        // XI. Change-Root()
        public void changeRoot() {
            if (node.getLeadsBestCanidate().getState() == GhsNodeEdgeState.IN_MST) {
                // Send change root on best edge
                sendChangeRoot(node.getLeadsBestCanidate());
            } else {
                // Send connect on best edge
                sendConnect(node.getLeadsBestCanidate(), node.getFragmentLvl());
                node.getLeadsBestCanidate().setState(GhsNodeEdgeState.IN_MST);
            }
        }

        public void receiveChangeRoot() {
            changeRoot();
        }

        public void sendMsg(GhsMessage msg) {
            // Get the receiver
            String recName = node.idToEdge(msg.weight).getConNodeName();
            sendRmiMessage(recName, msg);

            //sendRmiMessage(recName, msg);
            
        }

        public void sendConnect(GhsEdge edge, int fragmentLvl) {
            GhsMessage msg = new GhsMessage();
            msg.msgType = GhsMessageType.CONNECT;
            msg.weight = edge.getWeight();
            msg.fragmentLvl = fragmentLvl;
            sendMsg(msg);
        }

        public void sendInitiate(GhsEdge edge, int lvl, int fragmentName, GhsNodeState state) {
            GhsMessage msg = new GhsMessage();
            msg.msgType = GhsMessageType.INITIATE;
            msg.weight = edge.getWeight();
            msg.fragmentLvl = lvl;
            msg.fragmentName = fragmentName;
            msg.nodeState = state;
            sendMsg(msg);
        }

        public void sendTest(GhsEdge edge, int lvl, int fragmentName) {
            GhsMessage msg = new GhsMessage();
            msg.msgType = GhsMessageType.TEST;
            msg.weight = edge.getWeight();
            msg.fragmentLvl = lvl;
            msg.fragmentName = fragmentName;
            sendMsg(msg);
        }

        public void sendAccept(GhsEdge edge) {
            GhsMessage msg = new GhsMessage();
            msg.msgType = GhsMessageType.ACCEPT;
            msg.weight = edge.getWeight();
            sendMsg(msg);
        }

        public void sendReject(GhsEdge edge) {
            GhsMessage msg = new GhsMessage();
            msg.msgType = GhsMessageType.REJECT;
            msg.weight = edge.getWeight();
            sendMsg(msg);
        }

        // Todo is this OK?
        public void sendReport(GhsEdge edge, int bestWeight) {
            GhsMessage msg = new GhsMessage();
            msg.msgType = GhsMessageType.REPORT;
            msg.weight = edge.getWeight();
            msg.bestWeight = bestWeight;
            sendMsg(msg);
        }

        public void sendChangeRoot(GhsEdge edge) {
            GhsMessage msg = new GhsMessage();
            msg.msgType = GhsMessageType.CHANGEROOT;
            msg.weight = edge.getWeight();
            sendMsg(msg);
        }
    }


    public static void printEdges(List<GhsEdge> edges) {
        for (GhsEdge e : edges) {
            System.out.println("Weight: " + e.getWeight());
            System.out.println("State: " + e.getState());
            System.out.println("ConNodeName: " + e.getConNodeName());
            System.out.println("IP: " + e.getConNodeIP());
            System.out.println();
        }
    }

    public static void printNode() {
        try {
            System.out.println("------NODE INFO------");
            System.out.println("Name: " + name);
            System.out.println("Fragment name: " + thisNode.getFragmentName());
            System.out.println("Fragment lvl: " + thisNode.getFragmentLvl());
            System.out.println("State: " + thisNode.getState());
            System.out.println("Best known weight: " + thisNode.getBestKnownWeight());
            if (thisNode.getLeadsCoreEdge() != null)
                System.out.println("LeadsCoreEdge: " + thisNode.getLeadsCoreEdge().getConNodeName());
            if (thisNode.getLeadsBestCanidate() != null)
                System.out.println("LeadsBestCanidate: " + thisNode.getLeadsBestCanidate().getConNodeName());
            if (thisNode.getTestingEdge() != null)
                System.out.println("Testing edge: " + thisNode.getTestingEdge().getConNodeName());
        } catch (Exception e) {

        }
        System.out.println();
    }


    public static void main (String args[])
    {
        List<String> nodeList = new ArrayList<String>();
        List<String> ipList = new ArrayList<String>();
        int processIndex = -1;

        List<GhsEdge> edges = new ArrayList<GhsEdge>();

        if (args.length < 1) {
            System.out.println("Provide arguments please");
            return;
        }

        for (String s: args) {
            String [] components = s.split(":");
            if (components.length == 1) {
                name = components[0];
                System.out.println("Found name: " + name);
            } else if (components.length == 3) {
                int edgeWeight = Integer.parseInt(components[2]);
                edges.add(new GhsEdge(components[0], edgeWeight, components[1]));
            } else {
                System.out.println("Unknown component: " + s);
            }
        }
        //System.out.println("--------------------------------------");
        //printEdges(edges);

        System.out.printf("\n");

        System.out.println("Setup local RMI client: " + name);
        setupRmiClient(name);

        System.out.println("Setup RMI security manager");
        setupRmi();

        System.out.println("Opening RMI Connection for provided servers");

        for (int i=0; i<edges.size(); i++) {
            GhsEdge conEdge = edges.get(i);
            System.out.println("\tOpening RMI Connection for: " + conEdge.getConNodeName());
            System.out.println("\tOn IP: " + conEdge.getConNodeIP());
            boolean bound = false;
            while (!bound) {
                try {
                    Rmi obj = (Rmi)Naming.lookup("rmi://" + conEdge.getConNodeIP() + "/" + conEdge.getConNodeName());
                    rmiMap.put(conEdge.getConNodeName(), obj);
                    bound = true;
                    System.out.println("Binding " + conEdge.getConNodeName() + " succeded!");
                } catch (Exception e) {
                    System.out.println("Bound Error for " + conEdge.getConNodeName());
                    //System.out.println("Send RMI message err: " + e.getMessage());
                    //e.printStackTrace();
                    delay_ms(1000);
                }
            }
        }

        //singhalManager = new SinghalManager(name, processIndex, rmiList.size());
        thisNode = new GhsNode(edges);
        ghsManager = new GhsManager(thisNode);
        

        Scanner scanner = new Scanner(System.in);
        boolean done = false;
        boolean wakeup = true;


        while (!done) {
            if (name.equals("n1") && wakeup == true) {
                delay_ms(10000);
                System.out.println("Node: " + name + " waking up (main)");
                ghsManager.wakeup();
                wakeup = false;
            }

            if (messageQueue.size() != 0) {
                System.out.println("Queue size: " + messageQueue.size());
                GhsMessage msg = messageQueue.remove();
                ghsManager.parseMessage(msg);
                printNode();
                printEdges(thisNode.getEdges());
            }
            delay_ms(100);

        }

        System.out.println("End");
    }

}
