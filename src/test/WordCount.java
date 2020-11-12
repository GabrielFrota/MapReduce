package test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeMap;

public class WordCount {

  public static void main(String[] args) throws IOException {
    var map = new TreeMap<String, Long>();
    var lines = Files.readAllLines(Paths.get(args[0]));
    for (var l : lines) {
      var strs = l.split(",");
      for (var s : strs) {
        if (map.containsKey(s))
          map.put(s, map.get(s) + 1);
        else
          map.put(s, 1L);
      }
    }
    for (var en : map.entrySet()) {
      System.out.println(en.getKey() + "\t" + en.getValue() + "\n");
    }
  }
  
}
