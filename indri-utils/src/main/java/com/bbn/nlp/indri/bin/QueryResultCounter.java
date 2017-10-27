package com.bbn.nlp.indri.bin;

import com.bbn.nlp.indri.Indri;
import com.bbn.nlp.indri.IndriQueryer;

import com.google.common.base.Charsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public final class QueryResultCounter {

  private static final Logger log = LoggerFactory.getLogger(QueryResultCounter.class);

  private QueryResultCounter() {
    throw new UnsupportedOperationException();
  }

  public static void main(String[] argv) {
    // we wrap the main method in this way to
    // ensure a non-zero return value on failure
    try {
      trueMain(argv);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void trueMain(String[] argv) throws Exception {
    final IndriQueryer processor = Indri.queryerForIndex(new File(argv[0])).build();
    final BufferedReader reader =
        new BufferedReader(new InputStreamReader(System.in, Charsets.UTF_8));
    String line;
    while ((line = reader.readLine()) != null) {
      int count = processor.countResults(line);
      System.out.println(count);
    }
  }
}
