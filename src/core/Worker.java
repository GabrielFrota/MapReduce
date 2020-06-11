package core;

import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class Worker implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {
    try (var sock = new Socket("www.google.com", 80)) {
      System.setProperty("java.rmi.server.hostname", sock.getLocalAddress().getHostAddress());
      System.out.println(sock.getLocalAddress().getHostAddress());
    }
    var impl = new WorkerRemoteImpl();
    var reg = LocateRegistry.createRegistry(1099);
    reg.bind(WorkerRemote.LOOKUP_NAME, impl);
    System.out.println("RMI Registry binded to port 1099 exporting WorkerRemote interface.\n"
        + "Type \"quit\" to stop the server and close the JVM.");

    try (var scan = new Scanner(System.in)) {
      while (!scan.nextLine().equals("quit"));
    }
    return 0;
  }

}
