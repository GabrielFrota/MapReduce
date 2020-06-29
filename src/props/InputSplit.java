package props;

import java.io.File;

public interface InputSplit {

  public boolean splitAndSendChunks(File input, File workers);
  
}
