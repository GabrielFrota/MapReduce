package props;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TextRecordReader implements RecordReader<Long, String> {
  
  private BufferedReader reader;
  private Long currentKey;
  private String currentValue;
  private long currentLine;
  
  @Override
  public void init(File in) throws FileNotFoundException, IOException {
    reader = new BufferedReader(new FileReader(in));
  }

  @Override
  public Long getCurrentKey() throws IOException {
    return currentKey;
  }

  @Override
  public String getCurrentValue() throws IOException {
    return currentValue;
  }

  @Override
  public boolean readOneAndAdvance() throws IOException {
    currentKey = currentLine;
    currentValue = reader.readLine();
    if (currentValue == null) return false;
    currentLine++;
    return true;
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }
  
}
