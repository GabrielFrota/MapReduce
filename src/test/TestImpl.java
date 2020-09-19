package test;

import java.io.File;
import java.io.IOException;

import params.InputFormat;
import params.MapReduce;
import params.RecordWriter;
import params.TextInputFormat;
import params.TextRecordWriter;

public class TestImpl implements MapReduce<String, String, String, String> {

  private static final long serialVersionUID = 1L;

  @Override
  public InputFormat<String, String> getInputFormat() {
    return new TextInputFormat();
  }

  @Override
  public RecordWriter<String, String> getMapWriter(File out) throws IOException {
    var rec = new TextRecordWriter();
    rec.init(out);
    return rec;
  }

  @Override
  public void map(String k, String v, RecordWriter<String, String> w) throws IOException {
    w.write(k, "111222233333333333333333333333333333333322");
  }
  
}


