package exec;

import java.rmi.Remote;
import java.rmi.RemoteException;

import params.MapReduce;

public interface MasterRemote extends Remote {
  
  public MapReduce getTaskConf() throws RemoteException;
  
  public final static String NAME = "MasterRemote";

}
