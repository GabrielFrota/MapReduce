package interf;

import java.io.File;
import java.io.IOException;

public interface OutputFormat<K, V> {

  RecordWriter<K, V> getRecordWriter(File out) throws IOException;
  
}
