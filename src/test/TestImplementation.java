package test;

import java.io.IOException;

import deft.TextInputFormat;
import inter.InputFormat;
import inter.MapReduce;
import inter.RecordWriter;

public class TestImplementation extends MapReduce<Long, String, Long, String> {

  private static final long serialVersionUID = 1L;

  @Override
  public InputFormat<Long, String> getInputFormat() {
    return new TextInputFormat();
  }

  @Override
  public void map(Long k, String v, RecordWriter<Long, String> w) throws IOException {
    var arr = v.split(",");
    w.write(k, arr[arr.length - 1]);
  }
  
}


