package core;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

public class Master implements Callable<Integer> {
	
	private final File input;
	private final File output;
	
	private final List<InetAddress> workers = new LinkedList<>();
	
	public Master(File in, File out) {
		input = in;
		output = out;
	}
	
	// TODO: check subnet mask to scan ips
	private void findWorkers() {
		System.out.println("Scanning for reachable IPs in subnetwork");
		byte[] localAddr;
		try {
			Socket s = new Socket("www.google.com", 80);
			localAddr = s.getLocalAddress().getAddress();
			s.close(); 
		} catch (IOException ioe) {
			throw new RuntimeException("Connection to www.google.com failed, aborting execution.");
		}
//		InetAddress addr = InetAddress.getByAddress(dest);
//		System.out.println(addr.getHostAddress());
		try {
			Registry reg = LocateRegistry.getRegistry("192.168.25.7", 1099);
			WorkerRemote rem = (WorkerRemote) reg.lookup(WorkerRemote.LOOKUPNAME);
			System.out.println(rem.getRemoteDate());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
//		IntStream.rangeClosed(1, 254).parallel().forEach(i -> {
//			byte[] dest = localAddr;
//			dest[3] = (byte)i;
//			try {
//				
//			} catch (IOException | NotBoundException ex) {}
//		});
	}
	
	@Override
	public Integer call() throws Exception {
		findWorkers();
		for (InetAddress iadr : workers) {
			System.out.println(iadr);
		}
	    return 0;
	}

}
