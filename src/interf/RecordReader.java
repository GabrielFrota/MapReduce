package interf;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

public interface RecordReader<K extends Comparable<K> & Serializable, V extends Serializable> extends Closeable {
  
  public void init(File in) throws FileNotFoundException, IOException;
      
  public K getCurrentKey() throws IOException;
  
  public V getCurrentValue() throws IOException;
  
  public boolean readOneAndAdvance() throws IOException;

}
