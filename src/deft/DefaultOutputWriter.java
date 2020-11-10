package deft;

import java.io.BufferedWriter;
import java.io.EOFException;
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
  
  private class QElement implements Comparable<QElement> {
    public final ObjectInputStream in;
    public Record<K, V> rec;
    public QElement(ObjectInputStream in, Record<K, V> rec) {
      this.in = in;
      this.rec = rec;
    }
    @Override
    public int compareTo(QElement o) {
      return rec.compareTo(o.rec);
    }   
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void merge(Iterable<File> files, File out) throws Exception {
    var queue = new PriorityQueue<QElement>();
    for (var f : files) {
      var in = new ObjectInputStream(new FileInputStream(f));  
      var rec = (Record<K, V>) in.readObject(); 
      queue.add(new QElement(in, rec));
    }   
    
    var outStr = new BufferedWriter(new FileWriter(out));
    while (true) {
      var elem = queue.poll();
      if (elem == null) 
        break;
      outStr.write(elem.rec.getKey().toString() + "\t"
          + elem.rec.getValue().toString() + "\n");
      try {
        elem.rec = (Record<K, V>) elem.in.readObject();
        queue.add(elem);
      } catch (EOFException ex) {
        elem.in.close();
      }
    }
    outStr.close();
  }
  
}
