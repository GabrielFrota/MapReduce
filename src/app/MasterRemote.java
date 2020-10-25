package app;

import java.rmi.Remote;
import java.rmi.RemoteException;

import inter.MapReduce;

public interface MasterRemote extends Remote {
  
  @SuppressWarnings("rawtypes")
  public MapReduce getMapReduceImpl() throws RemoteException;
  
  public final static String NAME = "MasterRemote";

}
