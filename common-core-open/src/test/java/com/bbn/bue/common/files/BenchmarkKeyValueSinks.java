package com.bbn.bue.common.files;

import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class BenchmarkKeyValueSinks {

  private static Logger log = LoggerFactory.getLogger(BenchmarkKeyValueSinks.class);

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
    final File outputDir = params.getCreatableDirectory("outputDir");
    final File dbFile = new File(outputDir, "output.db");
    final File zipFile = new File(outputDir, "output.zip");
    final File fileDir = new File(outputDir, "files");
    fileDir.mkdirs();
    final File outputMap = new File(fileDir, "files.map");

    // Timers and counters
    long readingTime = 0;
    long dbWritingTime = 0;
    long zipWritingTime = 0;
    long fileWritingTime = 0;
    int documents = 0;

    // Load the file map and set up output map
    final Stopwatch stopwatch = Stopwatch.createStarted();
    final ImmutableKeyValueSource<Symbol, ByteSource> source =
        KeyValueSources.fromFileMap(FileUtils.loadSymbolToFileMap(inputMap));
    final ImmutableMap.Builder<Symbol, File> mapBuilder = ImmutableMap.builder();
    readingTime += stopwatch.elapsed(TimeUnit.MILLISECONDS);

    // We don't use try with resources so we can time opening/closing each individually
    stopwatch.reset().start();
    final KeyValueSink<Symbol, byte[]> dbSink = KeyValueSinks.forPalDB(dbFile, true);
    dbWritingTime += stopwatch.elapsed(TimeUnit.MILLISECONDS);

    stopwatch.reset().start();
    final KeyValueSink<Symbol, byte[]> zipSink = KeyValueSinks.forZip(zipFile);
    zipWritingTime += stopwatch.elapsed(TimeUnit.MILLISECONDS);

    // Write the output
    for (final Symbol key : source.keys()) {
      documents++;

      // Read
      stopwatch.reset().start();
      final byte[] value = source.getRequired(key).read();
      readingTime += stopwatch.elapsed(TimeUnit.MILLISECONDS);

      // DB Write
      stopwatch.reset().start();
      dbSink.put(key, value);
      dbWritingTime += stopwatch.elapsed(TimeUnit.MILLISECONDS);

      // Zip Write
      stopwatch.reset().start();
      zipSink.put(key, value);
      zipWritingTime += stopwatch.elapsed(TimeUnit.MILLISECONDS);

      // File write
      stopwatch.reset().start();
      final File outputFile = new File(fileDir, key.asString());
      Files.asByteSink(outputFile).write(value);
      mapBuilder.put(key, outputFile);
      fileWritingTime += stopwatch.elapsed(TimeUnit.MILLISECONDS);
    }

    // Count time spent closing
    stopwatch.reset().start();
    dbSink.close();
    dbWritingTime += stopwatch.elapsed(TimeUnit.MILLISECONDS);

    stopwatch.reset().start();
    zipSink.close();
    zipWritingTime += stopwatch.elapsed(TimeUnit.MILLISECONDS);

    // Write out the map
    stopwatch.reset().start();
    FileUtils.writeSymbolToFileMap(mapBuilder.build(), Files.asCharSink(outputMap, Charsets.UTF_8));
    fileWritingTime += stopwatch.elapsed(TimeUnit.MILLISECONDS);

    log.info("Wrote {} documents", documents);
    log.info("Reading time: {}", readingTime);
    log.info("File writing time: {}", fileWritingTime);
    log.info("DB writing time: {}", dbWritingTime);
    log.info("Zip writing time: {}", zipWritingTime);
  }
}
