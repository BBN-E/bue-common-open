package com.bbn.bue.common;

import com.bbn.bue.common.files.FileUtils;
import com.bbn.bue.common.parameters.Parameters;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.math.IntMath;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Given a list of files and a number of splits, creates training/test file lists for
 * cross-validation. When the files cannot be evenly divided across all splits, extra files are
 * distributed as evenly as possible, starting with the first folds. For example, dividing 11 items
 * into three folds will result in folds of size (4, 4, 3).
 */
public final class MakeCrossValidationBatches {

  private MakeCrossValidationBatches() {
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

  private static void errorExit(final String msg) {
    System.err.println(msg);
    System.exit(1);
  }

  private static void trueMain(String[] argv) throws IOException {
    if (argv.length != 1) {
      errorExit("Usage: MakeCrossValidationBatches params");
    }
    final Parameters parameters = Parameters.loadSerifStyle(new File(argv[0]));
    final File fileList = parameters.getExistingFile("com.bbn.bue.common.crossValidation.fileList");
    final File outputDirectory = parameters.getExistingDirectory(
        "com.bbn.bue.common.crossValidation.outputDir");
    final String outputName = parameters.getString("com.bbn.bue.common.crossValidation.outputName");
    final int numBatches = parameters.getPositiveInteger(
        "com.bbn.bue.common.crossValidation.numBatches");
    final int randomSeed = parameters.getInteger("com.bbn.bue.common.crossValidation.randomSeed");

    // Load the list of files
    final List<File> inputFiles = Lists.newArrayList(FileUtils.loadFileList(fileList));

    // Check numbatches
    if (numBatches < 2) {
      errorExit("Bad numBatches value: Need two or more batches to divide data into");
    } else if (numBatches > inputFiles.size()) {
      errorExit("Bad numBatches value: Cannot create more batches than there are input files");
    }

    // Shuffle files
    Collections.shuffle(inputFiles, new Random(randomSeed));
    // Divide into folds, with the remainder falling into the last fold
    List<List<File>> folds =
        Lists.partition(inputFiles, IntMath.divide(inputFiles.size(), numBatches, RoundingMode.DOWN));
    // If there are more folds than desired, distribute elements of the last fold to the other folds
    if (folds.size() > numBatches) {
      // First, make the outer list modifiable
      folds = Lists.newLinkedList(folds);
      // Remove the extra fold
      final List<File> extras = folds.remove(folds.size() - 1);

      // Make the folds modifiable
      for (int i = 0; i < folds.size(); i++) {
        folds.set(i, Lists.newLinkedList(folds.get(i)));
      }

      // Distribute the extra elements
      for (int extraIdx = 0, foldIdx = 0; extraIdx < extras.size(); extraIdx++, foldIdx = (foldIdx + 1) % folds.size()) {
        folds.get(foldIdx).add(extras.get(extraIdx));
      }
    }

    // Sanity checks
    // Correct number of folds
    if (folds.size() != numBatches) {
      throw new RuntimeException("Incorrect number of batches created");
    }
    // No elements were lost
    int totalFiles = 0;
    for(List<File> fold : folds) {
      totalFiles += fold.size();
    }
    if (totalFiles != inputFiles.size()) {
      throw new RuntimeException("Input files size and sum of folds do not match");
    }

    int batchNum = 0;
    for (final List<File> testFilesForBatch : folds) {
      final Set<File> trainingFilesForBatch =
          Sets.difference(ImmutableSet.copyOf(inputFiles), ImmutableSet.copyOf(testFilesForBatch))
              .immutableCopy();

      final File trainingOutputFile = new File(outputDirectory, outputName + "." +
          StringUtils.padWithMax(batchNum, numBatches - 1) + ".training.list");
      FileUtils.writeFileList(Ordering.natural().sortedCopy(trainingFilesForBatch),
          Files.asCharSink(trainingOutputFile,
              Charsets.UTF_8));
      final File testOutputFile = new File(outputDirectory, outputName + "."+
          StringUtils.padWithMax(batchNum, numBatches - 1) + ".test.list");
      FileUtils.writeFileList(Ordering.natural().sortedCopy(testFilesForBatch),
          Files.asCharSink(testOutputFile,
              Charsets.UTF_8));
      ++batchNum;
    }
  }
}
