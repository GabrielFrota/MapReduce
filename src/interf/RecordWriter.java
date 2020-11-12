package interf;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;

public interface RecordWriter<K extends Comparable<K> & Serializable, 
                              V extends Serializable> extends Closeable {
  
  void write(K key, V value) throws IOException;
  
}
