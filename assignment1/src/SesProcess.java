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
        } catch (Exception e) {
            System.out.println("Send RMI message err: " + e.getMessage());
            e.printStackTrace();
        }
    }

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
                    delay_ms(1000);
                }
            }
        }

        int cnt=0;
        while (rmiList.size() != 0) {
            System.out.println("Infinite loop");
            for (SesRmi obj: rmiList) {
                System.out.println("Sending message");
                sendMessage(obj, new SesMessage("test", new ArrayList<Integer>(), "msg: count=" + (cnt++)));
                delay_ms(5000);
            }
        }

        System.out.println("End");
    }
}
