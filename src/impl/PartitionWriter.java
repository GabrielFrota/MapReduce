package impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import interf.RecordWriter;

public class PartitionWriter <K extends Comparable<K> & Serializable, V extends Serializable> 
  implements RecordWriter<K, V> {
    
  private String prefix;
  private ArrayList<Record<K, V>>[] parts;
  private LinkedList<File>[] spills;
  private int MAX_SIZE = 10;
     
  @SuppressWarnings("unchecked")
  public PartitionWriter(String prefix, int numPartitions) throws IOException {
    this.prefix = prefix;
    parts = new ArrayList[numPartitions];
    spills = new LinkedList[numPartitions];
    for (int i = 0; i < numPartitions; i++) {
      parts[i] = new ArrayList<Record<K, V>>(MAX_SIZE);
      spills[i] = new LinkedList<File>();
    }
  }
  
  private int getPartition(K key) {
    return (key.hashCode() & Integer.MAX_VALUE) % parts.length;
  }
  
  private ForkJoinTask<Integer> spillTask;
  
  private void spill(int i) throws IOException {
    if (parts[i].size() == 0)
      return;
    if (spillTask != null) {
      try {
        spillTask.get();
        spillTask = null;
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    } 
    var part = parts[i];
    parts[i] = new ArrayList<Record<K, V>>(MAX_SIZE);
    var file = new File(prefix + "." + i + "." + spills[i].size());
    spills[i].add(file);
    
    spillTask = ForkJoinPool.commonPool().submit(() -> {
      var out = new ObjectOutputStream(new FileOutputStream(file));
      Collections.sort(part); 
      for (var rec : part) {
        out.writeObject(rec);
      }
      out.close();
      return 0;
    });
  }

  @Override
  public void write(K key, V value) throws IOException {    
    var rec = new Record<K, V>(key, value);
    var i = getPartition(key);
    var part = parts[i];
    if (part.size() == MAX_SIZE) {
      spill(i);
      part = parts[i];
    }     
    part.add(rec); 
  }
  
  @Override
  public void close() throws IOException {
    for (int i = 0; i < parts.length; i++) {
      spill(i);
      var files = spills[i];
      try {
        var reader = new PartitionReader<K, V>(files);
        var writer = new ObjectOutputStream(new FileOutputStream(prefix + "." + i));
        while (reader.readOneAndAdvance()) {
          for (var v : reader.getCurrentValue()) {     
            writer.writeObject(new Record<K, V>(reader.getCurrentKey(), v));
          }
        }
        reader.close();
        writer.close();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  
}
