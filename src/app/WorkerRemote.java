package app;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WorkerRemote extends Remote {
  
  public String getOK() throws RemoteException;
  
  public String getIp() throws RemoteException;
  
  public void setMasterIp(String ip) throws RemoteException;
  
  public boolean createNewFile(String fileName) throws RemoteException, IOException;
  
  public boolean delete(String fileName) throws RemoteException;
  
  public boolean exists(String fileName) throws RemoteException;
  
  public void initOutputStream(String fileName) throws RemoteException, IOException;
  
  public void write(byte[] chunk) throws RemoteException, IOException;
  
  public void write(byte[] b, int len) throws RemoteException, IOException;
  
  public void closeOutputStream() throws RemoteException, IOException;
  
  public void sendImplClass() throws Exception;
  
  public void doMap(String fileName) throws RemoteException, IOException;
  
  public boolean createInWorker(String ip, String filename) throws RemoteException, IOException, NotBoundException;
  
  public final static String NAME = "WorkerRemote";

}
