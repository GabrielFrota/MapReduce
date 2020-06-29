package core;

import java.io.File;
import java.nio.file.Files;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import props.TextInputFormat;

@Command(name = "core/Master", mixinStandardHelpOptions = true, 
    description = "Master proccess for distributed MapReduce jobs in a cluster.")
public class Master implements Callable<Integer> {
  
  @Option(names = {"-i", "--input"}, description = "Input file path.", 
      paramLabel = "FILE", required = true)
  private File input;

  @Option(names = {"-o", "--output"}, description = "Output file path.", 
      paramLabel = "FILE", required = true)
  private File output;

  @Option(names = {"-w", "--workers"}, description = "Workers IP addresses.", 
      paramLabel = "FILE", required = true)
  private File workers;
  
  @Option(names = {"-d", "--delete"})
  private boolean delete;

  private final static CommandLine comm = new CommandLine(new Master());

  private static void printErr(String msg) {
    var str = comm.getColorScheme().errorText(msg).toString();
    comm.getErr().println(str);
  }
  
  @Override
  public Integer call() throws Exception {
    if (!input.exists() || !input.canRead()) {
      printErr("Invalid --input parameter value. Correct file path to an existing "
          + "file with read permission is required.");
      return -1;
    }
    if (!workers.exists() || !workers.canRead()) {
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
    var lines = Files.readAllLines(workers.toPath());
    for (var ip : lines) {
      var reg = LocateRegistry.getRegistry(ip);
      var worker = (WorkerRemote) reg.lookup(WorkerRemote.NAME);
      if (!worker.getOK().equals("OK"))
        throw new RuntimeException("Host " + ip + " did not answered properly.");
    }
    
    var text = new TextInputFormat();
    var f = text.getSplits(input, 11);
    for (var file : f) {
      System.out.println(file.getName());
    }
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
