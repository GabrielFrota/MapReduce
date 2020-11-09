package deft;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import interf.RecordWriter;

public class DefaultOutputWriter <K extends Comparable<K> & Serializable, 
                                  V extends Serializable> implements RecordWriter<K, V> {
    
  private ArrayList<Record<K, V>>[] parts;
  private ObjectOutputStream[] ooss;
     
  @SuppressWarnings("unchecked")
  @Override
  public void init(File out, int numPartitions) throws IOException {
    parts = new ArrayList[numPartitions];
    ooss = new ObjectOutputStream[numPartitions];
    for (int i = 0; i < numPartitions; i++) {
      parts[i] = new ArrayList<Record<K, V>>();
      ooss[i] = new ObjectOutputStream(new FileOutputStream(out.getName() + "." + i));
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
      Collections.sort(part);
      var out = ooss[i];
      for (var rec : part) {
        out.writeObject(rec);
      }
      out.close();
    }
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void merge(Iterable<File> files, File out) throws Exception {
    var map = new HashMap<ObjectInputStream, Record<K, V>>();
    Comparator<ObjectInputStream> comp = (o1, o2) -> {
      return map.get(o1).compareTo(map.get(o2));
    };
    var queue = new PriorityQueue<ObjectInputStream>(comp);
    for (var f : files) {
      var in = new ObjectInputStream(new FileInputStream(f));  
      var rec = in.readObject(); 
      map.put(in, (Record<K, V>) rec);
      queue.add(in);
    }   
    
    var outStr = new BufferedWriter(new FileWriter(out));
    while (true) {
      var in = queue.poll();
      if (in == null) break;
      var rec = map.get(in);
      outStr.write(rec.getKey().toString() + "\t"
          + rec.getValue().toString() + "\n");
      try {
        rec = (Record<K, V>) in.readObject();
        map.put(in, rec);
        queue.add(in);
      } catch (IOException ex) {
        map.remove(in);
        in.close();
      }
    }
    outStr.close();
  }
  
}
