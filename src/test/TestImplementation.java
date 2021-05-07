package test;

import java.io.IOException;
import java.util.LinkedList;

import impl.TextInputFormat;
import impl.TextOutputFormat;
import interf.InputFormat;
import interf.MapReduce;
import interf.OutputFormat;
import interf.RecordWriter;

public class TestImplementation extends MapReduce<Long, String, String, Long, String, LinkedList<Long>, String, LinkedList<Long>> {

  private static final long serialVersionUID = 1L;

  @Override
  public InputFormat<Long, String> getInputFormat() {
    return new TextInputFormat();
  }
  
  @Override
  public OutputFormat<String, LinkedList<Long>> getOutputFormat() {
    return new TextOutputFormat<String, LinkedList<Long>>();
  }

  @Override
  public void map(Long k, String v, RecordWriter<String, Long> w) throws IOException {
    for (var s : v.split(",")) {
      w.write(s, k);
    }
  }
  
  @Override
  public void reduce(String k, Iterable<Long> vs, RecordWriter<String, LinkedList<Long>> w) throws IOException {
    w.write(k, (LinkedList<Long>) vs);
  }

  @Override
  public void combine(String k, Iterable<LinkedList<Long>> vs, RecordWriter<String, LinkedList<Long>> w)
      throws IOException {
    for (var v : vs) {
      w.write(k, v);
    }
  }
  
}


