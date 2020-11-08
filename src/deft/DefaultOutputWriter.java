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
    
  private char UNIT_SEPARATOR = 0x1f;
  private char RECORD_SEPARATOR = 0x1e;

  private ArrayList<Record<K, V>>[] parts;
  private BufferedWriter[] wrts;
     
  @SuppressWarnings("unchecked")
  @Override
  public void init(File out, int numPartitions) throws IOException {
    parts = new ArrayList[numPartitions];
    wrts = new BufferedWriter[numPartitions];
    for (int i = 0; i < numPartitions; i++) {
      parts[i] = new ArrayList<Record<K, V>>();
      wrts[i] = new BufferedWriter(new FileWriter(out.getName() + "." + i));
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
    for (int i = 0; i < parts.length; i++) {
      var part = parts[i];
      var wrt = wrts[i];
      Collections.sort(part);
      for (var rec : part) {
        wrt.write(rec.getKey().toString() + UNIT_SEPARATOR
            + rec.getValue().toString() + RECORD_SEPARATOR);
      }
      wrt.close();
    }
  }
  
}
