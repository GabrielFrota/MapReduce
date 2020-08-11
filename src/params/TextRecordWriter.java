package params;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TextRecordWriter implements RecordWriter<String, String> {
  
  private BufferedWriter writer;
  
  @Override
  public void init(File out) throws IOException {
    writer = new BufferedWriter(new FileWriter(out));
  }

  @Override
  public void write(String key, String value) throws IOException {
    writer.write(key + "\t" + value + "\n");
  }
  
  @Override
  public void close() throws IOException {
    writer.flush();
    writer.close();
  }
  
}
