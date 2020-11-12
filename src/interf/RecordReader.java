package interf;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;

public interface RecordReader<K extends Comparable<K> & Serializable, 
                              V extends Serializable> extends Closeable {
      
  K getCurrentKey() throws IOException;
  
  V getCurrentValue() throws IOException;
  
  boolean readOneAndAdvance() throws IOException;

}
