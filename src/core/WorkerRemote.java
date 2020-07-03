package core;

import java.io.File;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WorkerRemote extends Remote {
  
  public String getOK() throws RemoteException;
  
  public boolean createNewFile(File f) throws RemoteException, IOException;
  
  public boolean delete(File f) throws RemoteException;
  
  public void writeChunk(File f, byte[] chunk) throws RemoteException, IOException;
  
  public final static String NAME = "WorkerRemote";

}
