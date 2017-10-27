package com.bbn.bue.common.parameters;

import com.bbn.bue.common.files.FileUtils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/**
 * Creates a copy of a parameter file such that all parameters with values that are absolute paths
 * to existing file are changed to point to a new copy of that file that is created inside the
 * specified base directory.
 *
 * For example, if a parameter has the value {@code /nfs/raid01/foo.bar} and the base directory
 * is {@code /nfs/raid02/archive}, the output parameter will have the value
 * {@code /nfs/raid02/archive/nfs/raid01/foo.bar}. Note that the entire original path is appended
 * to the base directory; this is necessary to ensure there are no collisions between source files.
 *
 * @author Constantine Lignos
 */
public final class RebaseFileParameters {

  private static final Logger log = LoggerFactory.getLogger(RebaseFileParameters.class);

  public static void main(String[] args) {
    // We wrap the main method in this way to ensure a non-zero return value on failure
    try {
      trueMain(args);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void trueMain(String[] args) throws IOException {
    if (args.length != 3) {
      System.out.println("Usage rebaseFileParameters inputParams baseDirectory outputParams");
      System.exit(1);
    }

    final File inputParamsFile = new File(args[0]);
    // It's essential that this is absolute
    final File baseDir = new File(args[1]).getAbsoluteFile();
    if (baseDir.mkdirs()) {
      log.info("Created output directory {}", baseDir);
    }
    final File outputParamsFile = new File(args[2]);

    final Parameters inputParams = Parameters.loadSerifStyle(inputParamsFile);

    final Parameters.Builder builder = Parameters.builder();
    for (final Map.Entry<String, String> entry : inputParams.asMap().entrySet()) {
      final String key = entry.getKey();
      final String value = entry.getValue();
      // If it exists as file and is an absolute path, copy it. We make it canonical to take
      // care of any relative paths (i.e., /nfs/foo/bar/../baz).
      final File sourceFile = new File(value).getCanonicalFile();
      if (sourceFile.exists() && sourceFile.isAbsolute()) {
        final File destFile = new File(baseDir, sourceFile.getAbsolutePath());
        if (sourceFile.isFile()) {
          log.info("Copying file value for parameter {} from {} to {}", key, sourceFile, destFile);
          destFile.getParentFile().mkdirs();
          Files.copy(sourceFile, destFile);
        } else if (sourceFile.isDirectory()) {
          log.info("Recursively copying directory value for parameter {} from {} to {}", key,
              sourceFile, destFile);
          FileUtils.recursivelyCopyDirectory(sourceFile, destFile,
              StandardCopyOption.REPLACE_EXISTING);
        } else {
          throw new RuntimeException();
        }
        builder.set(key, destFile.getAbsolutePath());
      } else {
        // Add a warning if this looks like a file, because if we got here must not exist
        if (sourceFile.isAbsolute()) {
          log.warn("Parameter value {} for parameter {} looks like a file but doesn't exist",
              value, key);
        }
        log.info("Passing parameter unchanged: {}: {}", key, value);
        builder.set(key, value);
      }
    }

    log.info("Writing rebased parameters to {}", outputParamsFile);
    Files.asCharSink(outputParamsFile, Charsets.UTF_8).write(builder.build().dump(false));
  }
}
