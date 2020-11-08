package app;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.concurrent.Callable;

import inter.MapReduce;
import lib.CommandLine;
import lib.CommandLine.Command;

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
    public boolean createNewFile(String fileName) throws RemoteException, IOException {
      return new File(fileName).createNewFile();
    }
    
    @Override
    public boolean delete(String fileName) throws RemoteException {
      return new File(fileName).delete();
    }
    
    @Override
    public boolean exists(String fileName) throws RemoteException {
      return new File(fileName).exists();
    }
    
    private OutputStream out;
    
    @Override
    public void initOutputStream(String fileName) throws RemoteException, IOException {
      out = Files.newOutputStream(Paths.get(fileName), StandardOpenOption.APPEND);
    }
    
    @Override
    public void write(byte[] chunk) throws RemoteException, IOException {  
      out.write(chunk);
    }
    
    public void write(byte[] b, int len) throws RemoteException, IOException {
      out.write(b, 0, len);
    }
    
    @Override
    public void closeOutputStream() throws RemoteException, IOException {
      out.close();
    }
    
    private String masterIp;
    @Override
    public void setMasterIp(String ip) throws RemoteException {
      masterIp = ip;
    }
    
    @SuppressWarnings("rawtypes")
    private MapReduce mapRed;
    @Override
    public void sendImplClass() throws Exception {
      var reg = LocateRegistry.getRegistry(masterIp, 1100);
      var master = (MasterRemote) reg.lookup(MasterRemote.NAME);
      mapRed = master.getMapReduceImpl();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void doMap(String fileName) throws RemoteException, IOException {
      var in = new File(fileName);
      var mapOut = new File(fileName + ".mapout");
      var inputFormat = mapRed.getInputFormat();
      var recordReader = inputFormat.getRecordReader(in);
      var recordWriter = mapRed.getMapWriter();
      recordWriter.init(mapOut, mapRed.getWorkersNum());
      while (recordReader.readOneAndAdvance()) 
        mapRed.map(recordReader.getCurrentKey(), recordReader.getCurrentValue(), recordWriter);
      recordReader.close();
      recordWriter.close();
    }

  }
  
  @Override
  public Integer call() throws Exception {
    System.setProperty("java.security.policy", "sec.policy");
    System.setSecurityManager(new SecurityManager());
    try (var sock = new Socket("www.google.com", 80)) {
      System.setProperty("java.rmi.server.hostname", sock.getLocalAddress().getHostAddress());
      System.setProperty("java.rmi.server.useCodebaseOnly", "false");
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
