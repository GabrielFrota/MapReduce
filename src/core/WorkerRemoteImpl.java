package core;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class WorkerRemoteImpl extends UnicastRemoteObject implements WorkerRemote {

  private static final long serialVersionUID = 1L;

  public WorkerRemoteImpl() throws RemoteException {
    super();
  }

  @Override
  public String getOK() throws RemoteException {
    return "OK";
  }

}
