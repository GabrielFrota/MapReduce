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
  private int MAX_SIZE = 10000000;
     
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
  
  @SuppressWarnings("unchecked")
  public PartitionWriter(String prefix, int numPartitions, int buffSize) throws IOException {
    this.prefix = prefix;
    MAX_SIZE = buffSize;
    parts = new ArrayList[numPartitions];
    spills = new LinkedList[numPartitions];
    for (int i = 0; i < numPartitions; i++) {
      parts[i] = new ArrayList<Record<K, V>>(MAX_SIZE);
      spills[i] = new LinkedList<File>();
    }
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
  
  private ForkJoinTask<Integer> spillTask;
  private ObjectOutputStream spillOut;
  
  private void spill(int i) throws IOException {
    if (parts[i].size() == 0)
      return;
    if (spillTask != null) {
      try {
        spillTask.get();
        spillTask = null;
        spillOut.close();
        spillOut = null;
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    } 
    var part = parts[i];
    parts[i] = new ArrayList<Record<K, V>>(MAX_SIZE);
    var fileName = prefix + "." + i + ".spill." + spills[i].size();
    var file = new File(fileName);
    spills[i].add(file);
    
    spillOut = new ObjectOutputStream(new FileOutputStream(file));      
    spillTask = ForkJoinPool.commonPool().submit(() -> {          
      Collections.sort(part); 
      for (var rec : part) {
        spillOut.writeObject(rec);
      }
      return 0;
    });
  }
  
}
