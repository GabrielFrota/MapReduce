package core;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WorkerRemote extends Remote {
  
  public String getOK() throws RemoteException;

  public int sendMapChunk(String chunk) throws RemoteException;
  
  public final static String NAME = "WorkerRemote";

}
