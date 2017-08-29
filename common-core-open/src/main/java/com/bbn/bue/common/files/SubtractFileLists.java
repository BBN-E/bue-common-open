package com.bbn.bue.common.files;

import com.bbn.bue.common.parameters.Parameters;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Given two lists of files, produces a new list of all files present in the first list but not
 * present in the second.
 *
 * @author Ryan Gabbard
 */
public final class SubtractFileLists {

  private static final Logger log = LoggerFactory.getLogger(SubtractFileLists.class);

  private SubtractFileLists() {
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

  private static final Ordering<File> ORDER_BY_ABSOLUTE_PATH =
      Ordering.natural().onResultOf(FileUtils.toAbsolutePathFunction());

  private static void trueMain(String[] argv) throws IOException {
    final Parameters params = Parameters.loadSerifStyle(new File(argv[0]));
    log.info(params.dump());

    final File inputFileListFile = params.getExistingFile("com.bbn.subtractFileLists.inputList");
    final File toSubtractFileListFile =
        params.getExistingFile("com.bbn.subtractFileLists.toSubtract");
    final File outputFile = params.getCreatableFile("com.bbn.subtractFileLists.outputList");

    final ImmutableSet<File> inputFiles =
        ImmutableSet.copyOf(FileUtils.loadFileList(inputFileListFile));
    final ImmutableSet<File> filesToSubtract =
        ImmutableSet.copyOf(FileUtils.loadFileList(toSubtractFileListFile));

    final ImmutableSet<File> outputFileList =
        Sets.difference(inputFiles, filesToSubtract).immutableCopy();

    log.info("Subtracting {}'s {} files from {}'s {} files and writing {} files to to {}",
        toSubtractFileListFile, filesToSubtract.size(),
        inputFileListFile, inputFiles.size(),
        outputFileList.size(), outputFile);

    FileUtils.writeFileList(ORDER_BY_ABSOLUTE_PATH.sortedCopy(outputFileList),
        Files.asCharSink(outputFile, Charsets.UTF_8));
  }
}
