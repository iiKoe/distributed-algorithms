
import java.rmi.*;

public interface Rmi extends java.rmi.Remote
{
    void sendMessage(Message msg) throws RemoteException;
}

