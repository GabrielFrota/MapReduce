package core;

import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class Worker implements Callable<Integer> {
		
	@Override
	public Integer call() throws Exception {
		Socket s = new Socket("www.google.com", 80);
		System.setProperty("java.rmi.server.hostname", s.getLocalAddress().getHostAddress());
		s.close();
		
		WorkerRemoteImpl impl = new WorkerRemoteImpl();
		Registry reg = LocateRegistry.createRegistry(1099);
		reg.bind(WorkerRemote.LOOKUPNAME, impl);	
		System.out.println("RMI Registry binded to port 1099 exporting WorkerRemote interface.\n"
				+ "Type \"quit\" to stop the server and close the JVM.");
		
		Scanner scan = new Scanner(System.in);
		while (!scan.nextLine().equals("quit"));
		scan.close();		
		return 0;
	}
	
}
