package test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeMap;

public class WordCount {

  public static void main(String[] args) throws IOException {
    //System.setOut(new PrintStream(new File("count.txt")));
    var map = new TreeMap<String, Integer>();
    var lines = Files.readAllLines(Paths.get(args[0]));
    for (var l : lines) {
      var strs = l.split(",");
      for (var s : strs) {
        if (map.containsKey(s))
          map.put(s, map.get(s) + 1);
        else
          map.put(s, 1);
      }
    }
    for (var en : map.entrySet()) {
      System.out.println(en.getKey() + "\t" + en.getValue());
    }
    
//    String key = null;
//    int value = 0;
//    for (var en : map.entrySet()) {
//      if (en.getValue() > value) {
//        value = en.getValue();
//        key = en.getKey();
//      }
//    }
//    System.out.println(key + "\t" + value);
  }
  
}
