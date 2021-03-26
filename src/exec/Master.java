package exec;

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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

import impl.PartitionReader;
import interf.MapReduce;
import lib.ClassFileServer;
import lib.CommandLine;
import lib.CommandLine.Command;
import lib.CommandLine.Option;

@Command(name = "exec/Master", mixinStandardHelpOptions = false, 
    description = "Master process for MapReduce.")
class Master implements Callable<Integer> {
  
  private String timestamp = "_" + LocalDateTime.now().toString().replace(".", "_");
  @SuppressWarnings("rawtypes")
  private MapReduce mapRed;
  
  private class MasterRemoteImpl extends UnicastRemoteObject implements MasterRemote {  
    private static final long serialVersionUID = 1L;

    public MasterRemoteImpl() throws RemoteException {
      super(1099);
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
  
  private final String fileLabel = "FILE_PATH";
  
  @Option(names = {"-i", "--input"}, description = "Input file path.", 
      paramLabel = fileLabel, required = true)
  private File input;

  @Option(names = {"-o", "--output"}, description = "Output file path.", 
      paramLabel = fileLabel, required = true)
  private File output;

  @Option(names = {"-w", "--workers"}, description = "Workers IP addresses file path.", 
      paramLabel = fileLabel, required = true)
  private File workersFile;
  
  @Option(names = {"-mr", "--mapreduce"}, description = "MapReduce implementation class file path.", 
      paramLabel = fileLabel, required = true)
  private File mapReduceFile;
  
  @Option(names = {"-ov", "--overwrite"}, description = "Overwrite splits in Workers.")
  private boolean overwrite;
  
  @Option(names = {"-bs", "--buffersize"}, description = "In-memory Record buffer size.",
      paramLabel = "INT_VAL")
  private Integer buffSize;
    
  private final static CommandLine comm = new CommandLine(new Master());
  
  private void printErr(String msg) {
    var str = comm.getColorScheme().errorText(msg).toString();
    comm.getErr().println(str);
  }
  
  private WorkerRemote getWorkerRemote(String ip) 
    throws RemoteException, AccessException, NotBoundException 
  {
    var reg = LocateRegistry.getRegistry(ip);
    return (WorkerRemote) reg.lookup(WorkerRemote.NAME);
  }
  
  private boolean checkExistsCanRead(File f, String param) {
    if (!f.exists() || !f.canRead()) {
      printErr("Invalid " + param + " parameter value. Correct file "
          + "path to an existing file with read permission is required.");
      return false;
    }
    return true;
  }
  
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public Integer call() throws Exception {
    var params = Map.of(
      input, "--input", 
      workersFile, "--workers",
      mapReduceFile, "--mp"
    );
    for (var p : params.entrySet()) {
      if (!checkExistsCanRead(p.getKey(), p.getValue()))
        return -1;
    }
    
    var remapperIn = new HashMap<String, String>();
    byte[] b = Files.readAllBytes(mapReduceFile.toPath());
    var cr = new ClassReader(b);
    cr.accept(new ClassVisitor(Opcodes.ASM7) {
      @Override
      public void visit(int version, int access, String name, String signature, 
                        String superName, String[] interfaces) {
        if (!remapperIn.isEmpty()) 
          return;
        remapperIn.put(name, timestamp);
      }
    }, 0);
    var cw = new ClassWriter(0);
    var re = new ClassRemapper(cw, new SimpleRemapper(remapperIn));
    cr.accept(re, 0);
    var tempFile = remapperIn.values().iterator().next() + ".class";
    var tempFilePath = Paths.get(tempFile);
    byte[] bClazz = cw.toByteArray(); 
    Files.write(tempFilePath, bClazz);
    var clazzName = tempFile.split("\\.")[0].replace("/", ".");
    var clazz = new MasterClassLoader().loadClass(clazzName, bClazz);
    mapRed = (MapReduce) clazz.getDeclaredConstructor().newInstance();
    
    var out = comm.getOut();
    @SuppressWarnings("unused")
    var fileServer = new ClassFileServer(8080, ".");  
    try (var sock = new Socket("www.google.com", 80)) {
      var addr = sock.getLocalAddress().getHostAddress();
      System.setProperty("java.rmi.server.hostname", addr);
      System.setProperty("java.rmi.server.codebase", "http://" + addr + ":8080/");
    }
    var impl = new MasterRemoteImpl();
    var reg = LocateRegistry.createRegistry(1099);
    reg.bind(MasterRemote.NAME, impl);
    out.println("RMI Registry is bound to address " 
        + System.getProperty("java.rmi.server.hostname") 
        + ":1099 exporting MasterRemote interface.");
    
    var workers = Files.readAllLines(workersFile.toPath());
    for (var ip : workers) {
      var worker = getWorkerRemote(ip);
      if (!worker.getOK().equals("OK"))
        throw new RuntimeException("Host " + ip + " did not answer properly.");
      mapRed.workers.add(ip);
    }
    mapRed.setInputName(input.getName());
    mapRed.setOutputName(output.getName());
    if (buffSize != null)
      mapRed.setBufferSize(buffSize); 
    for (var ip : workers) {
      var worker = getWorkerRemote(ip);
      worker.downloadImpl(System.getProperty("java.rmi.server.hostname"));
    }
    
    var splits = mapRed.getInputFormat()
        .getSplits(input, mapRed.workers.size());
    for (var ip : workers) {
      var worker = getWorkerRemote(ip);
      var file = splits[workers.indexOf(ip)];
      var name = file.getName();
      if (overwrite) worker.delete(name);
      if (!worker.exists(name)) {
        worker.createNewFile(name);
        out.println("Sending " + name + " to worker " + worker.getIp());      
        var inStream = Files.newInputStream(file.toPath(), StandardOpenOption.READ);
        byte[] buf = new byte[WorkerRemote.CHUNK_LENGTH];
        worker.initOutputStream(name);
        for (int len = inStream.read(buf); len != -1; len = inStream.read(buf)) {
          worker.write(buf, len);
        }
        worker.closeOutputStream();
      }
    }
    
    var start = Instant.now();
    var pool = ForkJoinPool.commonPool();
    var tasks = new LinkedList<ForkJoinTask<Integer>>();
    for (var ip : workers) {
      Callable c = () -> {
        var worker = getWorkerRemote(ip);
        worker.doMap(mapRed.workers.indexOf(ip));
        return 0;
      };
      var t = pool.submit(c);
      tasks.add(t);
    }
    for (var t : tasks) {
      t.get();
    }
    tasks.clear();
    
    for (var ip : mapRed.workers) {
      Callable c = () -> {
        var worker = getWorkerRemote((String) ip);
        worker.gatherChunks(mapRed.workers.indexOf(ip));
        worker.doReduce();
        return 0;
      };
      var t = pool.submit(c);
      tasks.add(t);
    }
    for (var t : tasks) {
      t.get();
    }
    tasks.clear();
    
    var chunkName = mapRed.getInputName() + ".redout.0";
    var chunksToGather = new LinkedList<File>();
    for (var ip : mapRed.workers) {
      Callable c = () -> {
        var worker = getWorkerRemote((String) ip);
        chunksToGather.add(new File(chunkName + "." + ip));
        var output = Files.newOutputStream(chunksToGather.getLast().toPath());
        worker.initInputStream(chunkName);
        for (byte[] bytes = worker.read(WorkerRemote.CHUNK_LENGTH); 
             bytes.length != 0;
             bytes = worker.read(WorkerRemote.CHUNK_LENGTH)) {
          output.write(bytes);
        }
        worker.closeInputStream();
        output.close();
        return 0;
      };
      var t = pool.submit(c);
      tasks.add(t);
    }
    for (var t : tasks) {
      t.get();
    }    
    var recordReader = new PartitionReader(chunksToGather);  
    var recordWriter = mapRed.getOutputFormat().getRecordWriter(new File(mapRed.getOutputName()));
    mapRed.preCombine(recordWriter);
    while (recordReader.readOneAndAdvance()) {
      mapRed.combine(recordReader.getCurrentKey(), recordReader.getCurrentValue(), recordWriter);
    }
    mapRed.postCombine(recordWriter);
    recordReader.close();
    recordWriter.close();
    
    var finish = Instant.now();
    Files.delete(tempFilePath);
    out.println("MapReduce done. Execution time was " 
        + Duration.between(start, finish).toSeconds() + " seconds");
    return 0;
  }

  public static void main(String[] args) {
    int exitCode = comm.execute(args);
    System.exit(exitCode);
  }

}
