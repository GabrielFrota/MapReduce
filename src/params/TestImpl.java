package params;

import java.io.IOException;

public class TestImpl implements MapReduce<String, String, String, String> {

  @Override
  public InputFormat<String, String> getInputFormat() {
    return new TextInputFormat();
  }

  @Override
  public RecordWriter<String, String> getRecordWriter() {
    return new TextRecordWriter();
  }

  @Override
  public void map(String k, String v, RecordWriter<String, String> out) throws IOException {
    out.write(v, k);
  }
  
}
