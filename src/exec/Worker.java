package exec;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.concurrent.Callable;

import params.MapReduce;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "core/Worker", mixinStandardHelpOptions = true, 
  description = "Worker proccess for distributed MapReduce jobs in a cluster.")
public class Worker implements Callable<Integer> {
    
  private class WorkerRemoteImpl extends UnicastRemoteObject implements WorkerRemote {

    private static final long serialVersionUID = 1L;

    public WorkerRemoteImpl() throws RemoteException {
      super(1099);
    }
    
    @Override
    public String getOK() throws RemoteException {
      return "OK";
    }
    
    @Override
    public String getIp() throws RemoteException {
      return System.getProperty("java.rmi.server.hostname");
    }

    @Override
    public boolean createNewFile(File f) throws RemoteException, IOException {
      var file = new File(f.getName());
      return file.createNewFile();
    }
    
    @Override
    public boolean delete(File f) throws RemoteException {
      var file = new File(f.getName());
      return file.delete();
    }
    
    @Override
    public boolean exists(File f) throws RemoteException {
      var file = new File(f.getName());
      return file.exists();
    }
    
    private OutputStream out;
    
    @Override
    public void initWrite(File f) throws RemoteException, IOException {
      var file = new File(f.getName());
      out = Files.newOutputStream(file.toPath(), StandardOpenOption.APPEND);
    }
    
    @Override
    public void write(byte[] chunk) throws RemoteException, IOException {  
      out.write(chunk);
    }
    
    @Override
    public void doneWrite() throws RemoteException, IOException {
      out.close();
    }
    
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void doMap(File f, MapReduce mapRed) throws RemoteException, IOException {
      var in = new File(f.getName());
      var inputFormat = mapRed.getInputFormat();
      var recordWriter = mapRed.getMapWriter(new File(in.getName() + ".out"));
      var recordReader = inputFormat.getRecordReader(in);
      while (recordReader.readOneAndAdvance()) {
        mapRed.map(recordReader.getCurrentKey(), recordReader.getCurrentValue(), recordWriter);
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
