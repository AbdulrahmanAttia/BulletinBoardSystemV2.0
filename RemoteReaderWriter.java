import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteReaderWriter extends Remote {

      String read(String request) throws RemoteException;
      String write(String request) throws  RemoteException;
}
