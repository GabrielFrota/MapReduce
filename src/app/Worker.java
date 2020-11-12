package app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.Callable;

import deft.GatherRecordReader;
import deft.ScatterRecordWriter;
import interf.MapReduce;
import lib.CommandLine;
import lib.CommandLine.Command;

@Command(name = "core/Worker", mixinStandardHelpOptions = true, 
  description = "Worker proccess for distributed MapReduce jobs in a cluster.")
class Worker implements Callable<Integer> {
    
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
    
    private String masterIp;
    @Override
    public void setMasterIp(String ip) throws RemoteException {
      masterIp = ip;
    }
    
    @SuppressWarnings("rawtypes")
    private MapReduce mapRed;
    @Override
    public void downloadImpl() throws RemoteException, NotBoundException {
      var reg = LocateRegistry.getRegistry(masterIp, 1100);
      var master = (MasterRemote) reg.lookup(MasterRemote.NAME);
      mapRed = master.getMapReduceImpl();
    }
    
    @Override
    public String getFinished() throws RemoteException {
      return System.getProperty("java.rmi.server.hostname") + " finished execution";
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
      out = Files.newOutputStream(Paths.get(fileName));
    }
    
    public void write(byte[] b, int len) throws RemoteException, IOException {
      out.write(b, 0, len);
    }
    
    @Override
    public void closeOutputStream() throws RemoteException, IOException {
      out.close();
    }
    
    private InputStream in;
   
    public void initInputStream(String fileName) throws RemoteException, IOException {
      in = Files.newInputStream(Paths.get(fileName));
    }
        
    public byte[] read(int len) throws RemoteException, IOException {
      return in.readNBytes(len);
    }
    
    public void closeInputStream() throws RemoteException, IOException {
      in.close();
    };
        
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void doMap() throws RemoteException, IOException {
      var in = new File(mapRed.getInputName());
      var inputFormat = mapRed.getInputFormat();
      var recordReader = inputFormat.getRecordReader(in);
      var recordWriter = new ScatterRecordWriter(mapRed.getInputName() + ".mapout", mapRed.workers.size());
      while (recordReader.readOneAndAdvance()) {
        mapRed.map(recordReader.getCurrentKey(), recordReader.getCurrentValue(), recordWriter);
      }
      recordReader.close();
      recordWriter.close();
    }
    
    private Iterable<File> partitionChunks;
    
    @Override
    public void gatherChunks(int myIndex) throws RemoteException, IOException, NotBoundException {
      var myIp = System.getProperty("java.rmi.server.hostname");
      var myChunkName = mapRed.getInputName() + ".mapout." + myIndex;
      var chunksFromPartition = new LinkedList<File>();
      for (int i = 0; i < mapRed.workers.size() - 1; i++) {
        chunksFromPartition.add(new File(myChunkName + "." + i));
      }
      
      for (int i = 0; i < mapRed.workers.size(); i++) {
        var ip = (String) mapRed.workers.get(i);
        if (!ip.equals(myIp)) {
          var reg = LocateRegistry.getRegistry((String) ip);
          var worker = (WorkerRemote) reg.lookup(WorkerRemote.NAME);
          var path = chunksFromPartition.getFirst().toPath();
          Collections.rotate(chunksFromPartition, -1);
          var out = Files.newOutputStream(path);
          worker.initInputStream(myChunkName);
          for (byte[] b = worker.read(WorkerRemote.CHUNK_LENGTH); 
               b.length != 0;
               b = worker.read(WorkerRemote.CHUNK_LENGTH)) {
            out.write(b);
          }
          worker.closeInputStream();
          out.close();
        }
      }
      chunksFromPartition.add(new File(myChunkName));
      partitionChunks = chunksFromPartition;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void doReduce() throws RemoteException, IOException, ClassNotFoundException {
      var recordReader = new GatherRecordReader(partitionChunks);
      while (recordReader.readOneAndAdvance()) {
        mapRed.reduce(recordReader.getCurrentKey(), recordReader.getCurrentValue(), null);
      }
      recordReader.close();
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
