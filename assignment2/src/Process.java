/*
    java -Djava.security.policy=my.policy -Djava.rmi.server.hostname=145.94.141.198 Process 

    Process p1 p1 p2
    Process p2 p1 p2

*/

import java.util.*;
import java.lang.*;
import java.rmi.Naming; 
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Process {

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

        public void sendMessage() {
            System.out.println("I am " + this.name);
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


public static void main (String args[])
{
	String name = "";
	List<String> processList = new ArrayList<String>();
	List<SinghalRmi> rmiList = new ArrayList<SinghalRmi>();

	if (args.length < 1) {
		System.out.println("Please provide arguments.");
		return;
	}

	if (args.length < 1) {
            System.out.println("Provide arguments please");
            return;
        }

    for (String s: args) {
    	if (name.equals("")){
    		name = s;
    	} else {
    		if(!s.equals(name)){
    			processList.add(s);
    		}
    	}
    }

    System.out.println("Other processes:");
    for (String s: processList){
    	System.out.printf("%s\t", s);
    }
    System.out.printf("\n");

    // The SES Setup
    //SesMessageManager sesManager = new SesMessageManager(name, vectorIndex, vectorSize);

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
               	SinghalRmi obj = (SinghalRmi)Naming.lookup("//localhost/" + s);
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

    int cnt=0;
    while (rmiList.size() != 0) {
        System.out.println("Infinite loop");
        for (int i=0; i<rmiList.size(); i++) {
            System.out.println("Sending message");
            delay_ms(10000);
        }
    }

    System.out.println("End");
}

}