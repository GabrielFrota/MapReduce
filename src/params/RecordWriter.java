package params;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public interface RecordWriter<K extends Object, V extends Object> extends Closeable {
  
  public void init(File out) throws IOException;
  
  public void write(K key, V value) throws IOException;
  
}
