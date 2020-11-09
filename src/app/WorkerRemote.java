package app;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WorkerRemote extends Remote {
  
  // for setting up the system
  
  public final static String NAME = "WorkerRemote";
  
  public final static int CHUNK_LENGTH = 4096;
  
  public String getOK() throws RemoteException;
  
  public String getIp() throws RemoteException;
  
  public void setMasterIp(String ip) throws RemoteException;
  
  public void downloadImpl() throws RemoteException, NotBoundException;
  
  // file operations to be called remotely
  
  public boolean createNewFile(String fileName) throws RemoteException, IOException;
  
  public boolean delete(String fileName) throws RemoteException;
  
  public boolean exists(String fileName) throws RemoteException;
  
  // setting up a stream and writing chunks to a file remotely
  
  public void initOutputStream(String fileName) throws RemoteException, IOException;
    
  public void write(byte[] b, int len) throws RemoteException, IOException;
  
  public void closeOutputStream() throws RemoteException, IOException;
  
  // setting up a stream and reading chunks from a file remotely
  
  public void initInputStream(String fileName) throws RemoteException, IOException;
    
  public byte[] read(int len) throws RemoteException, IOException;
  
  public void closeInputStream() throws RemoteException, IOException;
  
  // MapReduce steps
  
  public void doMap() throws RemoteException, IOException;
  
  public void gatherPartition(int myIndex) throws RemoteException, IOException, NotBoundException;
  
}
