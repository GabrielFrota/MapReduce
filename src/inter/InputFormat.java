package inter;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public interface InputFormat<K extends Comparable<K> & Serializable, V extends Serializable> {
  
  public File[] getSplits(File in, int numSplits) throws IOException;
  
  public RecordReader<K, V> getRecordReader(File in) throws IOException;
  
}
