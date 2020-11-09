package interf;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import deft.DefaultOutputWriter;

public abstract class MapReduce <K1 extends Comparable<K1> & Serializable, V1 extends Serializable, 
                                 K2 extends Comparable<K2> & Serializable, V2 extends Serializable> 
  implements Serializable {
  
  private static final long serialVersionUID = 1L;
    
  private String inputName;
  
  public void setInputName(String n) {
    if (inputName != null) {
      throw new IllegalStateException("setInputName was already called");
    }
    inputName = Objects.requireNonNull(n);
  }
  
  public String getInputName() {
    return inputName;
  }
  
  public final List<String> workers = new ArrayList<String>();
    
  public RecordWriter<K2, V2> getMapWriter() {
    return new DefaultOutputWriter<K2, V2>();
  }
  
  public abstract InputFormat<K1, V1> getInputFormat();
  
  public abstract void map(K1 k, V1 v, RecordWriter<K2, V2> w) throws IOException;
  
}
