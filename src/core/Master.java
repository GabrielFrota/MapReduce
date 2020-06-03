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
		System.out.println("Scanning for reachable workers in subnetwork. This will take a few minutes.");
		byte[] localAddr;
		try {
			Socket s = new Socket("www.google.com", 80);
			localAddr = s.getLocalAddress().getAddress();
			s.close(); 
		} catch (IOException ioe) {
			throw new RuntimeException("Connection to www.google.com failed, aborting execution.");
		}
		IntStream.rangeClosed(1, 254).parallel().forEach(i -> {
			byte[] dest = localAddr;
			dest[3] = (byte)i;
			try {
				InetAddress addr = InetAddress.getByAddress(dest);
				if (addr.isReachable(1000)) {
					Registry reg = LocateRegistry.getRegistry(addr.getHostAddress());
					WorkerRemote rem = (WorkerRemote) reg.lookup(WorkerRemote.LOOKUP_NAME);
					System.out.println(rem.getRemoteDate() + "    " + addr.getHostAddress());
				}
			} catch (IOException | NotBoundException ex) {}
		});
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
