package deft;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import inter.RecordWriter;

public class DefaultOutputWriter <K extends Comparable<K> & Serializable, 
                                  V extends Serializable> implements RecordWriter<K, V> {
  
  private BufferedWriter writer;
  
  private char GROUP_SEPARATOR = 0x1d;
  private char RECORD_SEPARATOR = 0x1e;
  private char UNIT_SEPARATOR = 0x1f;
  
  private ArrayList<Record<K, V>>[] parts;
     
  @SuppressWarnings("unchecked")
  @Override
  public void init(File out, int numPartitions) throws IOException {
    writer = new BufferedWriter(new FileWriter(out));
    parts = new ArrayList[numPartitions];
    for (int i = 0; i < parts.length; i++) {
      parts[i] = new ArrayList<Record<K, V>>();
    }
  }
  
  @Override
  public int getPartition(K key, V value) {
    return (key.hashCode() & Integer.MAX_VALUE) % parts.length;
  }

  @Override
  public void write(K key, V value) throws IOException {    
    parts[getPartition(key, value)].add(new Record<K, V>(key, value));   
  }
  
  @Override
  public void close() throws IOException {
    for (var p : parts) {
      Collections.sort(p);
      for (var rec : p) {
        writer.write(rec.getKey().toString() + UNIT_SEPARATOR
            + rec.getValue().toString() + RECORD_SEPARATOR);
      }
      writer.write(GROUP_SEPARATOR);
    }
    writer.close();
  }
  
}
