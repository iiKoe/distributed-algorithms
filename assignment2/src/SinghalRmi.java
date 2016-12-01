import java.rmi.*;

public interface SinghalRmi extends java.rmi.Remote
{
    void sendMessage(SinghalMessage msg) throws RemoteException;
}

