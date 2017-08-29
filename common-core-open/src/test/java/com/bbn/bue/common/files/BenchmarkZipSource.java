package com.bbn.bue.common.files;

import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Stopwatch;
import com.google.common.io.ByteSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipFile;

/**
 * Test program for timing loading an Zip-backed key-value store.
 */
public final class BenchmarkZipSource {

  private static Logger log = LoggerFactory.getLogger(BenchmarkZipSource.class);

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
      System.out.println("No zip file specified");
      System.exit(1);
    }
    final File inputFile = new File(args[0]);

    // Open
    final Stopwatch stopwatch = Stopwatch.createStarted();
    final ImmutableKeyValueSource<Symbol, ByteSource> source =
        KeyValueSources.fromZip(new ZipFile(inputFile));
    log.info("Opened zip at {} in {} milliseconds", inputFile,
        stopwatch.elapsed(TimeUnit.MILLISECONDS));

    // Read everything
    int documents = 0;
    long bytesLoaded = 0;
    stopwatch.reset().start();
    for (final Symbol key : source.keys()) {
      documents++;
      bytesLoaded += source.getRequired(key).read().length;
    }
    source.close();
    final long readingTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);

    log.info("Read {} documents", documents);
    log.info("Read {} bytes", bytesLoaded);
    log.info("Reading time: {}", readingTime);
  }
}
