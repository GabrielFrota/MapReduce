package impl;

import java.io.Serializable;

class Record<K extends Comparable<K> & Serializable, V extends Serializable>
  implements Comparable<Record<K, V>>, Serializable {

  private static final long serialVersionUID = 1L;
  public final K key;
  public final V value;

  public Record(K key, V value) {
    this.key = key;
    this.value = value;
  }
  
  @Override
  public int compareTo(Record<K, V> o) {
    return key.compareTo(o.key);
  }
  
}
