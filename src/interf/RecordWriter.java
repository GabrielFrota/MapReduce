package interf;

import java.io.Closeable;
import java.io.IOException;

public interface RecordWriter<K, V> extends Closeable {
  
  void write(K key, V value) throws IOException;
  
}
