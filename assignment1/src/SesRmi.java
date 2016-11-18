import java.rmi.*;

public interface SesRmi extends java.rmi.Remote
{
    void sendMessage(SesMessage msg) throws RemoteException;
}
