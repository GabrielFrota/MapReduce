package props;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class TextInputFormat implements InputFormat<Long, String> {
  
  @Override
  public File[] getSplits(File in, int numSplits) throws IOException {
    long len = in.length();
    long splitLen = len / numSplits;
    var splits = new File[numSplits];
    var reader = new BufferedReader(new FileReader(in));
    for (int i = 0; i < numSplits; i++) {
      var inSplit = new File(in.getName() + "_" + i);
      var writer = new PrintWriter(new FileWriter(inSplit));
      long cnt = 0;
      while (cnt <= splitLen) {
        var line = reader.readLine();
        if (line == null) break;
        writer.println(line);
        cnt += line.getBytes().length;
      }
      splits[i] = inSplit;
      writer.close();
    }
    reader.close();
    return splits;
  }
  
  private class TextRecordReader implements RecordReader<Long, String> {
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

  @Override
  public RecordReader<Long, String> getRecordReader(File in) throws IOException {
    var rec = new TextRecordReader();
    rec.init(in);
    return rec;
  }
  
}
