package interf;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import deft.DefaultOutputWriter;

public abstract class MapReduce <K1 extends Comparable<K1> & Serializable, V1 extends Serializable, 
                                 K2 extends Comparable<K2> & Serializable, V2 extends Serializable> 
  implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  protected final HashMap<String, String> conf = new HashMap<>();
  
  public int setWorkersNum(int num) {
    var ret = conf.put("workersNum", Integer.toString(num));
    return ret != null ? Integer.parseInt(ret) : -1;
  }
  
  public int getWorkersNum() {
    var ret = conf.get("workersNum");
    return ret != null ? Integer.parseInt(ret) : -1;
  }
  
  public String setIdWorkerIp(int id, String ip) {
    return conf.put(Integer.toString(id), ip);
  }
  
  public String getWorkerIpFromId(int id) {
    return conf.get(Integer.toString(id));
  }
    
  public RecordWriter<K2, V2> getMapWriter() {
    return new DefaultOutputWriter<K2, V2>();
  }
  
  public abstract InputFormat<K1, V1> getInputFormat();
  
  public abstract void map(K1 k, V1 v, RecordWriter<K2, V2> w) throws IOException;
  
}
