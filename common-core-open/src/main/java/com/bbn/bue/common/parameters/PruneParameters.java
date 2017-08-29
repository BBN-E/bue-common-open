package com.bbn.bue.common.parameters;

import com.google.common.base.Charsets;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Prunes the contents of a parameter file down to only those in a specified list of parameters.
 *
 * @author Constantine Lignos
 */
public final class PruneParameters {

  private static final Logger log = LoggerFactory.getLogger(PruneParameters.class);

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
      System.out.println("Usage pruneParameters inputParams paramsList outputParams");
      System.exit(1);
    }

    final File inputParamsFile = new File(args[0]);
    final File paramsToKeepFile = new File(args[1]);
    final File outputParamsFile = new File(args[2]);

    final Parameters inputParams = Parameters.loadSerifStyle(inputParamsFile);
    // Sorted so that the params come out in order
    log.info("Loading parameters to keep from {}", paramsToKeepFile);
    final ImmutableSortedSet<String> paramsToKeep =
        FluentIterable.from(Files.asCharSource(paramsToKeepFile, Charsets.UTF_8).readLines())
            .toSortedSet(Ordering.<String>natural());
    log.info("Keeping {} parameters", paramsToKeep.size());

    final Parameters.Builder builder = Parameters.builder();
    for (final String param : paramsToKeep) {
      if (inputParams.isPresent(param)) {
        builder.set(param, inputParams.getString(param));
      } else {
        log.error("Parameter {} not present in input parameters", param);
        System.exit(1);
      }
    }

    log.info("Writing pruned parameters to {}", outputParamsFile);
    Files.asCharSink(outputParamsFile, Charsets.UTF_8).write(builder.build().dump(false));
  }
}
