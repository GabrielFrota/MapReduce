package interf;

import java.io.Serializable;

public final class Record<K extends Comparable<K> & Serializable, V extends Serializable>
    implements Comparable<Record<K, V>>, Serializable {

  private static final long serialVersionUID = 1L;
  private final K key;
  private final V value;

  public Record(K key, V value) {
    this.key = key;
    this.value = value;
  }

  public K getKey() {
    return key;
  }

  public V getValue() {
    return value;
  }

  @Override
  public int compareTo(Record<K, V> o) {
    return key.compareTo(o.key);
  }

}
