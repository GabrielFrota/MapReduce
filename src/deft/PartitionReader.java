package deft;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.PriorityQueue;

import interf.Record;
import interf.RecordReader;

public class PartitionReader <K extends Comparable<K> & Serializable, V extends Serializable> 
    implements RecordReader<K, LinkedList<V>> {
  
  private class QElement implements Comparable<QElement> {
    public final ObjectInputStream in;
    public Record<K, V> cur;
    public QElement(ObjectInputStream in, Record<K, V> cur) {
      this.in = in;
      this.cur = cur;
    }
    @Override
    public int compareTo(QElement e) {
      return cur.compareTo(e.cur);
    }   
  }
  
  private PriorityQueue<QElement> queue;
  
  @SuppressWarnings("unchecked")
  public PartitionReader(Iterable<File> chunks) throws IOException, ClassNotFoundException {
    queue = new PriorityQueue<QElement>(); 
    for (var f : chunks) {
      var in = new ObjectInputStream(new FileInputStream(f));  
      var rec = (Record<K, V>) in.readObject(); 
      queue.add(new QElement(in, rec));
    }
  }
  
  private K key;
  private LinkedList<V> values;
  
  @SuppressWarnings("unchecked")
  @Override
  public boolean readOneAndAdvance() throws IOException {
    var elem = queue.poll();
    if (elem == null) 
      return false;    
    key = elem.cur.getKey();
    values = new LinkedList<V>();
    values.add(elem.cur.getValue());
    try {
      elem.cur = (Record<K, V>) elem.in.readObject();
      queue.add(elem);
    } catch (EOFException ex) {
      elem.in.close();
    } catch (ClassNotFoundException cex) {
      throw new IOException(cex);
    }  
    
    while (key.equals(queue.peek().cur.getKey())) {
      elem = queue.poll();
      values.add(elem.cur.getValue());
      try {
        elem.cur = (Record<K, V>) elem.in.readObject();
        queue.add(elem);
      } catch (EOFException ex) {
        elem.in.close();
        if (queue.peek() == null)
          break;
      } catch (ClassNotFoundException cex) {
        throw new IOException(cex);
      }
    }
    return true;
  }
  
  @Override
  public K getCurrentKey() throws IOException {
    return key;
  }
  
  @Override
  public LinkedList<V> getCurrentValue() throws IOException {
    return values;
  }
      
  @Override
  public void close() throws IOException {}

}
