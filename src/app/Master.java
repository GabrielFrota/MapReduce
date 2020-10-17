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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

import lib.ClassFileServer;
import lib.CommandLine;
import lib.CommandLine.Command;
import lib.CommandLine.Option;
import params.MapReduce;

@Command(name = "core/Master", mixinStandardHelpOptions = false, 
    description = "Master proccess for distributed MapReduce jobs in a cluster.")
public class Master implements Callable<Integer> {
  
  private String timestamp = "_" + LocalDateTime.now().toString().replace(".", "_");
  @SuppressWarnings("rawtypes")
  private MapReduce mapRed;
  
  private class MasterRemoteImpl extends UnicastRemoteObject implements MasterRemote {  
    private static final long serialVersionUID = 1L;

    protected MasterRemoteImpl() throws RemoteException {
      super(1100);
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public MapReduce getMapReduceImpl() throws RemoteException {
      return mapRed;
    }
  }
  
  private class MasterClassLoader extends ClassLoader {
    public Class<?> loadClass(String name, byte[] b) {
      return defineClass(name, b, 0, b.length);
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
  
  @Option(names = {"-ov", "--overwrite"}, description = "Overwrite splits in Workers.")
  private boolean overwrite;
  
  @Option(names = {"-mp", "--mapreduce"}, description = "MapReduce implementation class file path.", 
      paramLabel = "FILE", required = true)
  private File mapReduceFile;

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
    if (!mapReduceFile.exists() || !mapReduceFile.canRead()) {
      printErr("Invalid --mapreduce parameter value. Correct file path to an existing "
          + "file with read permission is required.");
      return -1;
    }
    
    var remapperIn = new HashMap<String, String>();
    byte[] b = Files.readAllBytes(mapReduceFile.toPath());
    var cr = new ClassReader(b);
    cr.accept(new ClassVisitor(Opcodes.ASM7) {
      @Override
      public void visit(int version, int access, String name, String signature, 
                        String superName, String[] interfaces) {
        var strs = name.split("/");
        strs[strs.length - 1] = timestamp;
        var sj = new StringJoiner("/");
        for (var s : strs) 
          sj.add(s);
        remapperIn.put(name, sj.toString());
      }
    }, 0);
    var cw = new ClassWriter(0);
    var re = new ClassRemapper(cw, new SimpleRemapper(remapperIn));
    cr.accept(re, 0);
    var tempFilePath = remapperIn.values().iterator().next() + ".class";
    var tempFileFullPath = Paths.get("bin/" + tempFilePath);
    byte[] bClazz = cw.toByteArray(); 
    Files.write(tempFileFullPath, bClazz, StandardOpenOption.CREATE);
    var clazz = new MasterClassLoader().loadClass(tempFilePath.split("\\.")[0]
        .replace("/", "."), bClazz);
    mapRed = (MapReduce) clazz.getDeclaredConstructor().newInstance();
    
    var fileServer = new ClassFileServer(8080, "./bin");  
    try (var sock = new Socket("www.google.com", 80)) {
      var addr = sock.getLocalAddress().getHostAddress();
      System.setProperty("java.rmi.server.hostname", addr);
      System.setProperty("java.rmi.server.codebase", "http://" + addr + ":8080/");
    }
    var impl = new MasterRemoteImpl();
    var reg = LocateRegistry.createRegistry(1100);
    reg.bind(MasterRemote.NAME, impl);
    System.out.println("RMI Registry is binded to address " 
        + System.getProperty("java.rmi.server.hostname") 
        + ":1100 exporting MasterRemote interface.");
    
    var lines = Files.readAllLines(workersFile.toPath());
    for (var ip : lines) {
      var worker = getWorkerRemote(ip);
      if (!worker.getOK().equals("OK"))
        throw new RuntimeException("Host " + ip + " did not answered properly.");
      workers.add(ip);
      worker.setMasterIp(System.getProperty("java.rmi.server.hostname"));
      worker.sendImplClass();
    }   
    
    var text = mapRed.getInputFormat();
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
        worker.doMap(input);
        return w;
      });
      tasks.add(task);
    }
    for (var t : tasks) {
      t.get();
    }        
    System.out.println("FINISHED");
    
    return 0;
  }

  public static void main(String[] args) {
    int exitCode = comm.execute(args);
    System.exit(exitCode);
  }

}
