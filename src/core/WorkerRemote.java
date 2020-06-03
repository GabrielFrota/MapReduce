package core;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDateTime;

public interface WorkerRemote extends Remote {

	public LocalDateTime getRemoteDate() throws RemoteException;

	public final static String LOOKUP_NAME = "WorkerRemote";
	
}
