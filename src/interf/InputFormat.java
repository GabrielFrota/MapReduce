package interf;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public interface InputFormat<K extends Comparable<K> & Serializable, 
                             V extends Serializable> {
  
  File[] getSplits(File in, int numSplits) throws IOException;
  
  RecordReader<K, V> getRecordReader(File in) throws IOException;
  
}
