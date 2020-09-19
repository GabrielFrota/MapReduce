package app;

import java.io.File;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import lib.ClassFileServer;
import lib.CommandLine;
import lib.CommandLine.Command;
import lib.CommandLine.Option;
import params.MapReduce;
import test.TestImpl;

@Command(name = "core/Master", mixinStandardHelpOptions = false, 
    description = "Master proccess for distributed MapReduce jobs in a cluster.")
public class Master implements Callable<Integer> {
  
  private class MasterRemoteImpl extends UnicastRemoteObject implements MasterRemote {
    
    private static final long serialVersionUID = 1L;

    protected MasterRemoteImpl() throws RemoteException {
      super(1100);
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public MapReduce getMapReduceImpl() throws RemoteException {
      return new TestImpl();
    }

  }
  
  @Option(names = {"-i", "--input"}, description = "Input file path.", 
      paramLabel = "FILE", required = true)
  private File input;

  @Option(names = {"-o", "--output"}, description = "Output file path.", 
      paramLabel = "FILE", required = true)
  private File output;

  @Option(names = {"-w", "--workers"}, description = "Workers IP addresses file path.", 
      paramLabel = "FILE", required = true)
  private File workersFile;
  
  private final List<String> workers = new ArrayList<String>();
  
  @Option(names = {"-v", "--overwrite"}, description = "Overwrite splits in Workers.")
  private boolean overwrite;

  private final static CommandLine comm = new CommandLine(new Master());
  
  private void printErr(String msg) {
    var str = comm.getColorScheme().errorText(msg).toString();
    comm.getErr().println(str);
  }
  
  private WorkerRemote getWorkerRemote(String ip) 
    throws RemoteException, AccessException, NotBoundException {
    var reg = LocateRegistry.getRegistry(ip);
    return (WorkerRemote) reg.lookup(WorkerRemote.NAME);
  }
  
  @Override
  public Integer call() throws Exception {
    if (!input.exists() || !input.canRead()) {
      printErr("Invalid --input parameter value. Correct file path to an existing "
          + "file with read permission is required.");
      return -1;
    }
    if (!workersFile.exists() || !workersFile.canRead()) {
      printErr("Invalid --workers parameter value. Correct file path to an existing "
          + "file with read permission is required.");
      return -1;
    }
    if (output.exists()) 
      output.delete();
    if (!output.createNewFile()) {
      printErr("Output file creation failed.");
      return -1;
    }   
    var lines = Files.readAllLines(workersFile.toPath());
    for (var ip : lines) {
      var reg = LocateRegistry.getRegistry(ip);
      var worker = (WorkerRemote) reg.lookup(WorkerRemote.NAME);
      if (!worker.getOK().equals("OK"))
        throw new RuntimeException("Host " + ip + " did not answered properly.");
      workers.add(ip);
    }   
 //   System.setProperty("java.rmi.server.codebase", "http://bin/params/");
//    try (var sock = new Socket("www.google.com", 80)) {
//      //System.setProperty("java.rmi.server.hostname", sock.getLocalAddress().getHostAddress());
//      
//      //var reg = LocateRegistry.createRegistry(1098);
//    }  
    //
    
    var fileServer = new ClassFileServer(8080, "./bin");
    
//    System.setProperty("java.security.policy", "sec.policy");
//    System.setSecurityManager(new SecurityManager());
    try (var sock = new Socket("www.google.com", 80)) {
      System.setProperty("java.rmi.server.hostname", sock.getLocalAddress().getHostAddress());
      System.setProperty("java.rmi.server.codebase", "http://192.168.15.4:8080/");
      //System.setProperty("java.rmi.server.codebase", );
    }
    var impl = new MasterRemoteImpl();
    var reg = LocateRegistry.createRegistry(1100);
    var ip = System.getProperty("java.rmi.server.hostname");
    reg.bind(MasterRemote.NAME, impl);
    System.out.println("RMI Registry is binded to address " + ip + ":1100 exporting MasterRemote interface.");
    
    var newFile = Paths.get("_" + LocalDateTime.now().toString() + ".class");
    var source = Paths.get("bin/test/TestImpl.class");
    Files.copy(source, newFile);
    var clazz = this.getClass().getClassLoader().loadClass(newFile.getFileName().toString());
    MapReduce test = (MapReduce) clazz.getDeclaredConstructor().newInstance();
    
    var text = test.getInputFormat();
    var splits = text.getSplits(input, workers.size());
    int i = 0;
    for (var s : splits) {
      var worker = getWorkerRemote(workers.get(i++));
      boolean exists = worker.exists(input);
      if (!exists || overwrite) {
        if (exists) worker.delete(input);
        worker.createNewFile(input);
        System.out.println("Sending split " + i + " to worker " + worker.getIp());
        var inStream = Files.newInputStream(s.toPath(), StandardOpenOption.READ);
        byte[] buf = new byte[2048];
        worker.initWrite(input);
        for (int len = inStream.read(buf); len != -1; len = inStream.read(buf)) {
          if (len == buf.length) worker.write(buf);
          else worker.write(buf, len);
        }
        worker.doneWrite();
      }
    }
    
    var pool = ForkJoinPool.commonPool();
    var tasks = new LinkedList<ForkJoinTask<String>>();
    for (var w : workers) {
      var task = pool.submit(() -> {
        var worker = getWorkerRemote(w);
        worker.sendImplClass();
        worker.doMap(input);
        return w;
      });
      tasks.add(task);
    }
    for (var t : tasks) {
      t.get();
    }        
    System.out.println("FINISHED");
//    var reg = LocateRegistry.getRegistry(w);
//    var worker = (WorkerRemote) reg.lookup(WorkerRemote.NAME);
//    var lines = Files.readString(input.toPath());
//    int ret = worker.sendMapChunk(lines);
//    System.out.println(ret);
//    if (isMaster)
//      return new Master(input, output, workers).call();
//    else
//      return new Worker().call();
    return 0;
  }

  public static void main(String[] args) {
    int exitCode = comm.execute(args);
    System.exit(exitCode);
  }

}
