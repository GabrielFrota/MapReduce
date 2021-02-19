package impl;

import java.io.File;
import java.io.IOException;

import interf.OutputFormat;
import interf.RecordWriter;

public class TextOutputFormat<K, V> implements OutputFormat<K, V> {
  
  @Override
  public RecordWriter<K, V> getRecordWriter(File out) throws IOException {
    return new LineRecordWriter<K, V>(out);
  }

}
