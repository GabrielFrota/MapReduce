package params;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DefaultRecordWriter implements RecordWriter<String, String> {
  
  private BufferedWriter writer;
  private char RECORD_SEPARATOR = 0x1e;
  private char UNIT_SEPARATOR = 0x1f;
    
  @Override
  public void init(File out) throws IOException {
    writer = new BufferedWriter(new FileWriter(out));
  }

  @Override
  public void write(String key, String value) throws IOException {
    writer.write(key + UNIT_SEPARATOR + value + RECORD_SEPARATOR);
  }
  
  @Override
  public void close() throws IOException {
    writer.close();
  }
  
}
