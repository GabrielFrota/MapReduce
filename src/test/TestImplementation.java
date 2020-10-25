package test;

import java.io.IOException;

import deft.TextInputFormat;
import inter.InputFormat;
import inter.MapReduce;
import inter.RecordWriter;

public class TestImplementation extends MapReduce<Long, String, String, Integer> {

  private static final long serialVersionUID = 1L;

  @Override
  public InputFormat<Long, String> getInputFormat() {
    return new TextInputFormat();
  }

  @Override
  public void map(Long k, String v, RecordWriter<String, Integer> w) throws IOException {
    var strs = v.split(",");
    for (var s : strs) {
      w.write(s, 1);
    }
  }
  
}


