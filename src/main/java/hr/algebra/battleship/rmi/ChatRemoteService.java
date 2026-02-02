package hr.algebra.battleship.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatRemoteService extends Remote { // again marker interface, like Serializable
    String REMOTE_OBJECT_NAME = "hr.algebra.rmi.service";
    void sendChatMessage(String message) throws RemoteException;
    List<String> getAllMessages() throws RemoteException;
}
