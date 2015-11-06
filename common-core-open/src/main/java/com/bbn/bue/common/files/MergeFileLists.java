package com.bbn.bue.common.files;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Merges file lists together.
 */
public final class MergeFileLists {

  private static final Logger log = LoggerFactory.getLogger(MergeFileLists.class);

  private MergeFileLists() {
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

  private static void trueMain(String[] argv) throws IOException {
    if (argv.length != 2) {
      System.err.println("usage: MergeFileLists inputListOfLists outputList:\n"
          + "\tinputListOfLists: a file with one filename per line of file lists to merge\n"
          + "\toutputList: a file to write the merged list to\n");
      System.exit(1);
    }
    final File listOfLists = new File(argv[0]);
    final File outputFile = new File(argv[1]);
    final ImmutableSet.Builder<File> ret = ImmutableSet.builder();
    for (final File fileList : FileUtils
        .loadFileList(Files.asCharSource(listOfLists, Charsets.UTF_8))) {
      final ImmutableList<File> fileFromFileList =
          FileUtils.loadFileList(Files.asCharSource(fileList, Charsets.UTF_8));
      log.info("Loaded {} files from {}", fileFromFileList, fileList);
      ret.addAll(fileFromFileList);
    }
    outputFile.mkdirs();

    final ImmutableSet<File> mergedFiles = ret.build();
    log.info("Wrote list of {} files to {}", mergedFiles.size(), outputFile);
    FileUtils.writeFileList(mergedFiles, Files.asCharSink(outputFile, Charsets.UTF_8));
  }
}
