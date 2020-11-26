package interf;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public interface OutputFormat<K extends Comparable<K> & Serializable, 
                              V extends Serializable> {

  RecordWriter<K, V> getRecordWriter(File out) throws IOException;
  
}
