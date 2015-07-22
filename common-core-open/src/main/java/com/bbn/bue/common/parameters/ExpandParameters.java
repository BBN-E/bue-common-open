package com.bbn.bue.common.parameters;

import com.google.common.base.Charsets;
import com.google.common.io.CharSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

/**
 * Created by jdeyoung on 7/22/15.
 */
final class ExpandParameters {

  public static void main(String ... args) {
    try {
      trueMain(args);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void trueMain(final String[] args) throws IOException {
    final Parameters input = Parameters.loadSerifStyle(new File(args[0]));
    final String resolved = input.dump();
    final CharSink output = Files.asCharSink(new File(args[1]), Charsets.UTF_8,
        FileWriteMode.APPEND);
    output.write(resolved);
  }

}
