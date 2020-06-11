package core;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WorkerRemote extends Remote {

  public int sendMapChunk(String chunk) throws RemoteException;

  public final static String LOOKUP_NAME = "WorkerRemote";

}
