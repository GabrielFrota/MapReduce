package deft;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import interf.Record;
import interf.RecordWriter;

public class ScatterRecordWriter <K extends Comparable<K> & Serializable, V extends Serializable> 
    implements RecordWriter<K, V> {
    
  private ArrayList<Record<K, V>>[] parts;
  private ObjectOutputStream[] ooss;
     
  @SuppressWarnings("unchecked")
  public ScatterRecordWriter(String prefix, int numPartitions) throws IOException {
    parts = new ArrayList[numPartitions];
    ooss = new ObjectOutputStream[numPartitions];
    for (int i = 0; i < numPartitions; i++) {
      parts[i] = new ArrayList<Record<K, V>>();
      ooss[i] = new ObjectOutputStream(new FileOutputStream(prefix + "." + i));
    }
  }
  
  private int getPartition(K key, V value) {
    return (key.hashCode() & Integer.MAX_VALUE) % parts.length;
  }

  @Override
  public void write(K key, V value) throws IOException {    
    parts[getPartition(key, value)].add(new Record<K, V>(key, value));   
  }
  
  @Override
  public void close() throws IOException {
    for (int i = 0; i < parts.length; i++) {
      var part = parts[i];
      Collections.sort(part);
      var out = ooss[i];
      for (var rec : part) {
        out.writeObject(rec);
      }
      out.close();
    }
  }
  
}
