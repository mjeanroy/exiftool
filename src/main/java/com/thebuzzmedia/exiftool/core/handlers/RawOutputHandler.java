package com.thebuzzmedia.exiftool.core.handlers;

import static com.thebuzzmedia.exiftool.core.handlers.StopHandler.stopHandler;

import com.thebuzzmedia.exiftool.Constants;
import com.thebuzzmedia.exiftool.process.OutputHandler;

public class RawOutputHandler implements OutputHandler {

    private final StringBuilder output;

    public RawOutputHandler() {
      this.output = new StringBuilder();
    }

    @Override
    public boolean readLine(String line) {
      // If line is null, then this is the end.
      // If line is strictly equals to "{ready}", then it means that stay_open feature
      // is enabled and this is the end of the output.
      if (!stopHandler().readLine(line)) {
        return false;
      }

      if (output.length() > 0) {
        output.append(Constants.BR);
      }
      output.append(line);

      return true;
    }

    public String getOutput() {
      return output.toString();
    }
}
