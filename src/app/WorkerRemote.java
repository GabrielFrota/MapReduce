package app;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

interface WorkerRemote extends Remote {
  
  // for setting up the system
  
  String NAME = "WorkerRemote";
  
  int CHUNK_LENGTH = 4096;
  
  String getOK() throws RemoteException;
  
  String getIp() throws RemoteException;
  
  void setMasterIp(String ip) throws RemoteException;
  
  void downloadImpl() throws RemoteException, NotBoundException;
  
  // file operations to be called remotely
  
  boolean createNewFile(String fileName) throws RemoteException, IOException;
  
  boolean delete(String fileName) throws RemoteException;
  
  boolean exists(String fileName) throws RemoteException;
  
  // setting up a stream and writing chunks to a file remotely
  
  void initOutputStream(String fileName) throws RemoteException, IOException;
    
  void write(byte[] b, int len) throws RemoteException, IOException;
  
  void closeOutputStream() throws RemoteException, IOException;
  
  // setting up a stream and reading chunks from a file remotely
  
  void initInputStream(String fileName) throws RemoteException, IOException;
    
  byte[] read(int len) throws RemoteException, IOException;
  
  void closeInputStream() throws RemoteException, IOException;
  
  // MapReduce steps
  
  void doMap() throws RemoteException, IOException;
  
  void gatherPartition(int myIndex) throws RemoteException, IOException, NotBoundException;
  
}
