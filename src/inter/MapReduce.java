package inter;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import deft.DefaultOutputWriter;

public interface MapReduce <K1 extends Comparable<K1> & Serializable, V1 extends Serializable, 
                            K2 extends Comparable<K2> & Serializable, V2 extends Serializable> 
  extends Serializable {
  
  public final HashMap<String, String> props = new HashMap<>();
  
  public default int setWorkersNum(int num) {
    var ret = props.put("workersNum", Integer.toString(num));
    if (ret == null)
      return 0;
    return Integer.parseInt(ret);
  }
  
  public default int getWorkersNum() {
    return Integer.parseInt(props.get("workersNum"));
  }
  
  public InputFormat<K1, V1> getInputFormat();
        
  public void map(K1 k, V1 v, RecordWriter<K2, V2> w) throws IOException;
  
  public default RecordWriter<K2, V2> getMapWriter() {
    return new DefaultOutputWriter<K2, V2>();
  }
  
}
