package core;

import java.io.File;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "core/Hadp", mixinStandardHelpOptions = true, 
    description = "Framework for distributed MapReduce jobs in a cluster.")
public class MapReduce implements Callable<Integer> {

  @Option(names = { "-m", "--mode" }, required = true, 
      description = "Process execution mode. {master, worker}.", paramLabel = "mode")
  private String mode;

  @Option(names = { "-i", "--input" }, description = "Input file path. Required in master only.", 
      paramLabel = "FILE")
  private File input;

  @Option(names = { "-o", "--output" }, description = "Output file path. Required in master only.", 
      paramLabel = "FILE")
  private File output;

  @Option(names = { "-w", "--workers" }, description = "Workers IP addresses. Required in master only.", 
      paramLabel = "FILE")
  private File workers;

  private final static CommandLine comm = new CommandLine(new MapReduce());

  private static void printErr(String msg) {
    var str = comm.getColorScheme().errorText(msg).toString();
    comm.getErr().println(str);
  }

  @Override
  public Integer call() throws Exception {
    var isMaster = mode.equals("master");
    var isWorker = mode.equals("worker");
    if (!isMaster && !isWorker) {
      printErr("Invalid --mode parameter value. Valid values are \"master\" or \"worker\"");
      return -1;
    }
    if (isMaster && (!input.exists() || !input.canRead())) {
      printErr("Invalid --input parameter value. Correct file path to an existing "
          + "file with read permission is required.");
      return -1;
    }
    if (isMaster && (!workers.exists() || !workers.canRead())) {
      printErr("Invalid --workers parameter value. Correct file path to an existing "
          + "file with read permission is required.");
      return -1;
    }
    if (isMaster && output.exists()) {
      output.delete();
    }
    if (isMaster && !output.createNewFile()) {
      printErr("Output file creation failed.");
      return -1;
    }
    if (isMaster)
      return new Master(input, output, workers).call();
    else
      return new Worker().call();
  }

  public static void main(String[] args) {
    int exitCode = comm.execute(args);
    System.exit(exitCode);
  }

}
