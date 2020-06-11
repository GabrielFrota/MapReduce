package core;

import java.io.File;
import java.nio.file.Files;
import java.rmi.registry.LocateRegistry;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

public class Master implements Callable<Integer> {

  private final File input;
  private final File output;

  private final List<String> workers = new LinkedList<>();

  public Master(File in, File out, File workersIp) throws Exception {
    input = in;
    output = out;
    var lines = Files.readAllLines(workersIp.toPath());
    for (var ip : lines) {
      var reg = LocateRegistry.getRegistry(ip);
      var worker = (WorkerRemote) reg.lookup(WorkerRemote.NAME);
      if (!worker.getOK().equals("OK"))
        throw new RuntimeException("Host " + ip + " did not answered properly.");
      workers.add(ip);
    }
  }

  @Override
  public Integer call() throws Exception {
    for (var w : workers) {
      var reg = LocateRegistry.getRegistry(w);
      var worker = (WorkerRemote) reg.lookup(WorkerRemote.NAME);
      var lines = Files.readAllLines(input.toPath());
      int ret = worker.sendMapChunk(lines.toString());
      System.out.println(ret);
    }
    return 0;
  }

}
