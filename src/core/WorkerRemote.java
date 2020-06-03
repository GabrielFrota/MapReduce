package core;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDateTime;

public interface WorkerRemote extends Remote {

	/** The method used to get the current date on the remote */
	public LocalDateTime getRemoteDate() throws RemoteException;

	/** The name used in the RMI registry service. */
	public final static String LOOKUPNAME = "WorkerRemote";
	
}
