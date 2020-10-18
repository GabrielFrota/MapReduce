package params;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DefaultRecordWriter implements RecordWriter<Object, Object> {
  
  private BufferedWriter writer;
  private char GROUP_SEPARATOR = 0x1d;
  private char RECORD_SEPARATOR = 0x1e;
  private char UNIT_SEPARATOR = 0x1f;
    
  @Override
  public void init(File out) throws IOException {
    writer = new BufferedWriter(new FileWriter(out));
  }

  @Override
  public void write(Object key, Object value) throws IOException {
    writer.write(key.toString() + UNIT_SEPARATOR + value.toString() + RECORD_SEPARATOR);
  }
  
  @Override
  public void close() throws IOException {
    writer.close();
  }
  
}
