package app;

import java.io.File;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WorkerRemote extends Remote {
  
  public String getOK() throws RemoteException;
  
  public String getIp() throws RemoteException;
  
  public void setMasterIp(String ip) throws RemoteException;
  
  public boolean createNewFile(File f) throws RemoteException, IOException;
  
  public boolean delete(File f) throws RemoteException;
  
  public boolean exists(File f) throws RemoteException;
  
  public void initWrite(File f) throws RemoteException, IOException;
  
  public void write(byte[] chunk) throws RemoteException, IOException;
  
  public void write(byte[] b, int len) throws RemoteException, IOException;
  
  public void doneWrite() throws RemoteException, IOException;
  
  public void sendImplClass() throws Exception;
  
  public void doMap(File f) throws RemoteException, IOException;
  
  public final static String NAME = "WorkerRemote";

}