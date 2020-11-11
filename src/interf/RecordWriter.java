package interf;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public interface RecordWriter<K extends Comparable<K> & Serializable, 
                              V extends Serializable> extends Closeable {
  
  void init(File out, int numPartitions) throws IOException;
  
  void write(K key, V value) throws IOException;
  
  int getPartition(K key, V value);
  
  void merge(Iterable<File> files, File out) throws Exception;
  
  public class Record<K extends Comparable<K> & Serializable, 
                      V extends Serializable> implements Comparable<Record<K, V>>, Serializable {    
    private static final long serialVersionUID = 1L;
    private final K key;
    private final V value;        
    public Record(K key, V value) {
      this.key = key;
      this.value = value;
    }
    public K getKey() {
      return key;
    }
    public V getValue() {
      return value;
    }
    @Override
    public int compareTo(Record<K, V> o) {
      return key.compareTo(o.key);
    }      
  }
  
}
