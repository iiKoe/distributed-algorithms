import java.rmi.*;

public interface Rmi extends java.rmi.Remote
{
    void sendMessage(GhsMessage msg) throws RemoteException;
}

