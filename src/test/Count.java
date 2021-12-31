package test;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeMap;

import impl.TextInputFormat;
import impl.TextOutputFormat;
import interf.InputFormat;
import interf.MapReduce;
import interf.OutputFormat;
import interf.RecordWriter;

public class Count extends MapReduce<Long, String, String, Integer, String, Integer, String, Integer> {
  
  private static final long serialVersionUID = 1L;

  @Override
  public InputFormat<Long, String> getInputFormat() {
    return new TextInputFormat();
  }

  @Override
  public OutputFormat<String, Integer> getOutputFormat() {
    return new TextOutputFormat<String, Integer>();
  }

  @Override
  public void map(Long k, String v, RecordWriter<String, Integer> w) throws IOException {
    for (var s : v.split(",")) {
      w.write(s, 1);
    }
  }
  
  @Override
  public void reduce(String k, Iterable<Integer> vs, RecordWriter<String, Integer> w) throws IOException {
    int sum = 0;
    for (var v : vs) {
      sum += v;
    }
    w.write(k, sum);
  }
  
  TreeMap<Integer, LinkedList<String>> map = new TreeMap<>(Comparator.reverseOrder());
  
  @Override
  public void combine(String k, Iterable<Integer> vs, RecordWriter<String, Integer> w) throws IOException {
    int cnt = ((LinkedList<Integer>) vs).getFirst();
    if (map.size() < 5) {
      var l = new LinkedList<String>();
      l.add(k);
      map.put(cnt, l);
    }
    if (map.containsKey(cnt)) {
      map.get(cnt).add(k);
    }
    if (cnt > map.lastKey()) {
      var l = new LinkedList<String>();
      l.add(k);
      map.put(cnt, l);
      map.remove(map.lastKey());
    }
  }
  
  @Override
  public void postCombine(RecordWriter<String, Integer> w) throws IOException {
    for (var en : map.entrySet()) {
      for (var s : en.getValue()) {
        w.write(s, en.getKey());
      }
    }
  }

}
