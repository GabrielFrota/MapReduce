package interf;

import java.io.Closeable;
import java.io.IOException;

public interface RecordReader<K, V> extends Closeable {
      
  K getCurrentKey() throws IOException;
  
  V getCurrentValue() throws IOException;
  
  boolean readOneAndAdvance() throws IOException;

}
