package test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeMap;

public class WordCount {

  public static void main(String[] args) throws IOException {
    var map = new TreeMap<Integer, Integer>();
    var lines = Files.readAllLines(Paths.get(args[0]));
    for (var l : lines) {
      var strs = l.split(",");
      for (var s : strs) {
        var i = Integer.parseInt(s);
        if (map.containsKey(i))
          map.put(i, map.get(i) + 1);
        else
          map.put(i, 1);
      }
    }
    for (var en : map.entrySet()) {
      System.out.println(en.getKey() + "\t" + en.getValue());
    }
  }
  
}
