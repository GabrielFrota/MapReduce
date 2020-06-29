package core;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "core/Worker", mixinStandardHelpOptions = true, 
  description = "Worker proccess for distributed MapReduce jobs in a cluster.")
public class Worker implements Callable<Integer> {
    
  private class WorkerRemoteImpl extends UnicastRemoteObject implements WorkerRemote {

    private static final long serialVersionUID = 1L;

    public WorkerRemoteImpl() throws RemoteException {
      super();
    }
    
    @Override
    public String getOK() throws RemoteException {
      return "OK";
    }

    @Override
    public boolean createNewFile(File f) throws RemoteException {
      try {
        return f.createNewFile();
      } catch (Exception ex) {
        return error(ex);
      }
    }
    
    @Override
    public boolean delete(File f) throws RemoteException {
      try {
        return f.delete();
      } catch (Exception ex) {
        return error(ex);
      }
    }
    
    @Override
    public void writeChunk(File f, byte[] chunk) throws RemoteException {
      try {
        var out = Files.newOutputStream(f.toPath(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        out.write(chunk);
      } catch (Exception ex) {
        error(ex);
      }
    }   
  }
  
  @Override
  public Integer call() throws Exception {
    try (var sock = new Socket("www.google.com", 80)) {
      System.setProperty("java.rmi.server.hostname", sock.getLocalAddress().getHostAddress());
    }
    var impl = new WorkerRemoteImpl();
    var reg = LocateRegistry.createRegistry(1099);
    var ip = System.getProperty("java.rmi.server.hostname");
    reg.bind(WorkerRemote.NAME, impl);
    System.out.println("RMI Registry is binded to address " + ip + ":1099 exporting WorkerRemote interface.\n"
        + "Type \"quit\" to stop the server and close the JVM.");

    try (var scan = new Scanner(System.in)) {
      while (!scan.nextLine().equals("quit"));
    }
    return 0;
  }
  
  private final static CommandLine comm = new CommandLine(new Worker());
  
  public static void main(String[] args) {
    int exitCode = comm.execute(args);
    System.exit(exitCode);
  }

}
