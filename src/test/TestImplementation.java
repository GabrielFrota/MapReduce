package test;

import java.io.IOException;

import impl.TextInputFormat;
import impl.TextOutputFormat;
import interf.InputFormat;
import interf.MapReduce;
import interf.OutputFormat;
import interf.RecordWriter;

public class TestImplementation extends MapReduce<Long, String, Integer, Integer, Integer, Integer> {

  private static final long serialVersionUID = 1L;

  @Override
  public InputFormat<Long, String> getInputFormat() {
    return new TextInputFormat();
  }

  @Override
  public void preMap(RecordWriter<Integer, Integer> w) throws IOException {
    w.write(-1, Integer.parseInt("Startando"));
  }
  
  @Override
  public void map(Long k, String v, RecordWriter<Integer, Integer> w) throws IOException {
    var strs = v.split(",");
    for (var s : strs) {
      w.write(Integer.parseInt(s), 1);
    }
  }

  @Override
  public void reduce(Integer k, Iterable<Integer> values, RecordWriter<Integer, Integer> w) throws IOException {
    int cnt = 0;
    for (var v : values) {
      cnt += v;
    }
    w.write(k, cnt);
  }

  @Override
  public OutputFormat<Integer, Integer> getOutputFormat() {
    return new TextOutputFormat<Integer, Integer>();
  }
  
}


