
package com.bbn.bue.common.parameters;

import com.google.common.io.CharSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by jdeyoung on 7/22/15.
 */
final class WindowizeParameters {

  public static void main(String... args) {
    try {
      trueMain(args);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static String toWindowsByBestGuess(final String path) throws IOException {
    final File f = new File(path);
    // if this is a thing on the linux file system
    if(f.exists()) {
      return f.getCanonicalPath().replace("/nfs/", "//").replaceAll("/", "\\\\");
    } else {
      // handle not yet existing paths
      final int parentIndex = path.lastIndexOf("/");
      if(parentIndex <= 0) {
        return path;
      }
      final File p = new File(path.substring(0, parentIndex));
      if(p.exists()) {
        return p.getCanonicalPath().replace("/nfs/", "//").replaceAll("/", "\\\\")  + "\\" + path.substring(parentIndex+1);
      } else {
        return path;
      }
    }

  }

  private static String toWindowsByBestGuess(final String[] params) throws IOException {
    final StringBuilder stringBuilder = new StringBuilder();
    for (final String p : params) {
      if (p.startsWith("#")) {
        stringBuilder.append(p);
        stringBuilder.append("\n");
        continue;
      }
      if (p.trim().isEmpty()) {
        continue;
      }
      final String[] parts = p.trim().split(":\\s*");
      checkArgument(parts.length == 2, "Params must be key:value");
      System.out.println(parts[1]);
      final String value = toWindowsByBestGuess(parts[1]);
      System.out.println(value);
      stringBuilder.append(parts[0]);
      stringBuilder.append(": ");
      stringBuilder.append(value);
      stringBuilder.append("\n");
    }
    return stringBuilder.toString();
  }

  private static void trueMain(final String[] args) throws IOException {
    final Parameters input = Parameters.loadSerifStyle(new File(args[0]));
    final String resolved = toWindowsByBestGuess(input.dump().split("\\n"));
    final CharSink output = Files.asCharSink(new File(args[1]), Charset.defaultCharset(),
        FileWriteMode.APPEND);
    output.write(resolved);
  }
}
