import java.rmi.*;

public interface Rmi extends java.rmi.Remote
{
    void sendMessage(SinghalMessage msg) throws RemoteException;
}

