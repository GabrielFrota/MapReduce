package props;

import java.io.File;
import java.io.IOException;

public interface InputFormat<K, V> {
  
  public File[] getSplits(File in, int numSplits) throws IOException;
  
  public RecordReader<K, V> getRecordReader(File in) throws IOException;
  
}
