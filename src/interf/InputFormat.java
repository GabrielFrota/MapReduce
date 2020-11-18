package interf;

import java.io.File;
import java.io.IOException;

public interface InputFormat<K, V> {
  
  File[] getSplits(File in, int numSplits) throws IOException;
  
  RecordReader<K, V> getRecordReader(File in) throws IOException;
  
}
