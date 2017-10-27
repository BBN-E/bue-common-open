package com.bbn.bue.common.files;

import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Converts a a key-to-file map to a PalDB-backed database.
 *
 * @author Constantine Lignos
 */
public final class ConvertFileMapToEmbeddedDB {

  private static Logger log = LoggerFactory.getLogger(ConvertFileMapToEmbeddedDB.class);

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
    if (args.length != 1) {
      System.out.println("No parameter file specified");
      System.exit(1);
    }
    final Parameters params = Parameters.loadSerifStyle(new File(args[0]));
    final File inputMap = params.getExistingFile("inputMap");
    final File outputFile = params.getCreatableFile("outputFile");
    final boolean useCompression = params.getOptionalBoolean("useCompression").or(true);

    // Set up timing
    long readTime = 0;
    long writeTime = 0;

    log.info("Loading files from {}", inputMap);
    final Stopwatch stopwatch = Stopwatch.createStarted();
    final ImmutableMap<Symbol, File> fileMap = FileUtils.loadSymbolToFileMap(inputMap);
    final ImmutableKeyValueSource<Symbol, ByteSource> source =
        KeyValueSources.fromFileMap(fileMap);
    readTime += stopwatch.elapsed(TimeUnit.MILLISECONDS);

    // To allow us to time the open/close operations, we don't use try-with-resources
    stopwatch.reset().start();
    final KeyValueSink<Symbol, byte[]> sink =
        KeyValueSinks.forPalDB(outputFile, useCompression);
    writeTime += stopwatch.elapsed(TimeUnit.MILLISECONDS);

    // Process files
    for (final Symbol key : source.keys()) {
      // Read
      stopwatch.reset().start();
      byte[] value = source.getRequired(key).read();
      readTime += stopwatch.elapsed(TimeUnit.MILLISECONDS);

      // Write
      stopwatch.reset().start();
      sink.put(key, value);
      writeTime += stopwatch.elapsed(TimeUnit.MILLISECONDS);
    }

    // Time closing
    stopwatch.reset().start();
    sink.close();
    writeTime += stopwatch.elapsed(TimeUnit.MILLISECONDS);

    log.info("Wrote {} documents to {}", fileMap.size(), outputFile);
    log.info("Read time: {}", readTime);
    log.info("Write time: {}", writeTime);
  }
}
