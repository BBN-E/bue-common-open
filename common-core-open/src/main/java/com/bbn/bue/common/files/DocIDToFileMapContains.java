package com.bbn.bue.common.files;

import com.bbn.bue.common.StringUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Utility to check that a doc ID-to-file-map contains all documents
 * on a given list.
 */
public final class DocIDToFileMapContains {
  private DocIDToFileMapContains() {
    throw new UnsupportedOperationException();
  }

  public static void main(String[] argv) {
    // we wrap the main method in this way to
    // ensure a non-zero return value on failure
    try {
      System.err.println("Copyright 2015 Raytheon BBN Technologies Corp.");
      System.err.println("All Rights Reserved.");
      trueMain(argv);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void trueMain(String[] argv) throws IOException {
    if (argv.length != 2) {
      System.err.println("usage: DocIDToFileMapContains docIDToFileMap fileList");
      System.exit(1);
    }

    final File docIdMapFile = new File(argv[0]);
    final File docListFile = new File(argv[1]);
    final ImmutableSet<String> idsInDocList =
        ImmutableSet.copyOf(Files.asCharSource(docListFile, Charsets.UTF_8).readLines());
    final Set<String> idsMapped = FileUtils.loadStringToFileMap(docIdMapFile).keySet();
    final ImmutableSet<String> difference = Sets.difference(idsInDocList, idsMapped).immutableCopy();

    if (!difference.isEmpty()) {
      System.out.println(StringUtils.NewlineJoiner.join(difference));
      System.exit(1);
    } else {
      System.exit(0);
    }
  }
}
