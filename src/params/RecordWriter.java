package params;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public interface RecordWriter<K extends Comparable<K> & Serializable, V extends Serializable> extends Closeable {
  
  public void init(File out) throws IOException;
  
  public void write(K key, V value) throws IOException;
  
}
