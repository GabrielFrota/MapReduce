package app;

import java.rmi.Remote;
import java.rmi.RemoteException;

import interf.MapReduce;

interface MasterRemote extends Remote {
  
  @SuppressWarnings("rawtypes")
  MapReduce getMapReduceImpl() throws RemoteException;
  
  String NAME = "MasterRemote";

}
