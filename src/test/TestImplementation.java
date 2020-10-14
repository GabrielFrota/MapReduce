package test;

import java.io.IOException;

import params.InputFormat;
import params.MapReduce;
import params.RecordWriter;
import params.TextInputFormat;
import params.TextRecordWriter;

public class TestImplementation implements MapReduce<String, String, String, String> {

  private static final long serialVersionUID = 1L;

  @Override
  public InputFormat<String, String> getInputFormat() {
    return new TextInputFormat();
  }

  @Override
  public RecordWriter<String, String> getMapWriter() throws IOException {
    var rec = new TextRecordWriter();
    return rec;
  }

  @Override
  public void map(String k, String v, RecordWriter<String, String> w) throws IOException {
    w.write(k, "111222989898898");
  }
  
}


