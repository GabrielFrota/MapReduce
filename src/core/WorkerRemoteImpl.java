package core;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;

public class WorkerRemoteImpl extends UnicastRemoteObject implements WorkerRemote {
	
	private static final long serialVersionUID = 1L;

	public WorkerRemoteImpl() throws RemoteException {
		super();	
	}

	@Override
	public LocalDateTime getRemoteDate() throws RemoteException {
		return LocalDateTime.now();
	}

}
