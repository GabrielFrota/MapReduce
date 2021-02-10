package interf;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class MapReduce <K1, V1, 
                                 K2 extends Comparable<K2> & Serializable, V2 extends Serializable,
                                 K3 extends Comparable<K3> & Serializable, V3 extends Serializable> 
  implements Serializable {
  
  private static final long serialVersionUID = 1L;
    
  private String inputName;
  private String outputName;
  private Integer bufferSize;
  
  public void setInputName(String n) {
    if (inputName != null) {
      throw new IllegalStateException("setInputName was already called");
    }
    inputName = Objects.requireNonNull(n);
  }
  
  public String getInputName() {
    return inputName;
  }
  
  public void setOutputName(String n) {
    if (outputName != null) {
      throw new IllegalStateException("setOutputName was already called");
    }
    outputName = Objects.requireNonNull(n);
  }
  
  public String getOutputName() {
    return outputName;
  }
  
  public void setBufferSize(Integer s) {
    if (bufferSize != null) {
      throw new IllegalStateException("setBuffSize was already called");
    }
    bufferSize = Objects.requireNonNull(s);
  }
  
  public Integer getBufferSize() {
    return bufferSize;
  }
  
  public void preMap(RecordWriter<K2, V2> w) throws IOException {};
  public void postMap(RecordWriter<K2, V2> w) throws IOException {};
  public void preReduce(RecordWriter<K3, V3> w) throws IOException {};
  public void postReduce(RecordWriter<K3, V3> w) throws IOException {};
  public void preCombine(RecordWriter<K3, V3> w) throws IOException {};
  public void postCombine(RecordWriter<K3, V3> w) throws IOException {};
  
  public final List<String> workers = new ArrayList<String>();
    
  public abstract InputFormat<K1, V1> getInputFormat();
  
  public abstract OutputFormat<K3, V3> getOutputFormat();
  
  public abstract void map(K1 k, V1 v, RecordWriter<K2, V2> w) throws IOException;
  
  public abstract void reduce(K2 k, Iterable<V2> values, RecordWriter<K3, V3> w) throws IOException;
  
  public void combine(K3 k, Iterable<V3> values, RecordWriter<K3, V3> w) throws IOException {
    for (var v : values) {
      w.write(k, v);
    }
  };
    
}
