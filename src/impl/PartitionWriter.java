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
  private int sizeMax = 10000000;
     
  @SuppressWarnings("unchecked")
  public PartitionWriter(String prefix, int numPartitions) throws IOException {
    this.prefix = prefix;
    parts = new ArrayList[numPartitions];
    spills = new LinkedList[numPartitions];
    for (int i = 0; i < numPartitions; i++) {
      parts[i] = new ArrayList<Record<K, V>>(sizeMax);
      spills[i] = new LinkedList<File>();
    }
  }
  
  @SuppressWarnings("unchecked")
  public PartitionWriter(String prefix, int numPartitions, int size) throws IOException {
    this.prefix = prefix;
    sizeMax = size;
    parts = new ArrayList[numPartitions];
    spills = new LinkedList[numPartitions];
    for (int i = 0; i < numPartitions; i++) {
      parts[i] = new ArrayList<Record<K, V>>(sizeMax);
      spills[i] = new LinkedList<File>();
    }
  }
  
  @Override
  public void write(K key, V value) throws IOException {    
    var i = getPartition(key);
    var part = parts[i];
    if (part.size() == sizeMax) {
      spill(i);
      part = parts[i];
    }     
    part.add(new Record<K, V>(key, value)); 
  }
  
  @Override
  public void close() throws IOException {
    for (int i = 0; i < parts.length; i++) {
      var fileName = prefix + "." + i;
      var out = new ObjectOutputStream(new FileOutputStream(fileName));
      
      if (spills[i].size() == 0) {
        Collections.sort(parts[i]);
        for (var rec : parts[i]) {
          out.writeObject(rec);
        }
      } else {
        spill(i);
        var in = new PartitionReader<K, V>(spills[i]);
        while (in.readOneAndAdvance()) {
          for (var rec : in.getRecords()) {
            out.writeObject(rec);
          }
        }
        in.close();
      }
      out.close();
    }
  }
  
  private int getPartition(K key) {
    return (key.hashCode() & Integer.MAX_VALUE) % parts.length;
  }
  
  private ForkJoinTask<Integer> task;
  private ObjectOutputStream out;
  
  private void spill(int i) throws IOException {
    if (parts[i].size() == 0)
      return;
    if (task != null) {
      try {
        task.get();
        out.close();
        task = null;    
        out = null;
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    } 
    var part = parts[i];
    parts[i] = new ArrayList<Record<K, V>>(sizeMax);
    var fileName = prefix + "." + i + ".spill." + spills[i].size();
    var file = new File(fileName);
    spills[i].add(file);
    
    out = new ObjectOutputStream(new FileOutputStream(file));      
    task = ForkJoinPool.commonPool().submit(() -> {          
      Collections.sort(part); 
      for (var rec : part) {
        out.writeObject(rec);
      }
      return 0;
    });
  }
  
}
