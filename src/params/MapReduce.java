package params;

import java.io.IOException;
import java.io.Serializable;

public interface MapReduce <K1 extends Comparable<K1> & Serializable, V1 extends Serializable, 
                            K2 extends Comparable<K2> & Serializable, V2 extends Serializable> 
  extends Serializable {
  
  public InputFormat<K1, V1> getInputFormat();
  
  public default RecordWriter<? extends Object, ? extends Object> getMapRecordWriter() {
    return new DefaultRecordWriter();
  }
      
  public void map(K1 k, V1 v, RecordWriter<K2, V2> w) throws IOException;
  
}
