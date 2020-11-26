package impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

import interf.RecordWriter;

public class LineRecordWriter<K extends Comparable<K> & Serializable, V extends Serializable> 
  implements RecordWriter<K, V> {
  
  private BufferedWriter writer;

  public LineRecordWriter(File out) throws IOException {
    writer = new BufferedWriter(new FileWriter(out));
  }
  
  @Override
  public void write(K key, V value) throws IOException {
    writer.write(key + "\t" + value + "\n");
  }
  
  @Override
  public void close() throws IOException {
    writer.close();
  }

}
