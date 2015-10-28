package com.bbn.bue.common;

import com.bbn.bue.common.collections.ShufflingIterable;
import com.bbn.bue.common.files.FileUtils;
import com.bbn.bue.common.parameters.Parameters;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.math.IntMath;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Given a list of files and a number of splits, creates training/test file lists for
 * cross-validation.
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

  private static void trueMain(String[] argv) throws IOException {
    final Parameters parameters = Parameters.loadSerifStyle(new File(argv[0]));
    final File fileList = parameters.getExistingFile("com.bbn.bue.common.crossValidation.fileList");
    final File outputDirectory = parameters.getExistingDirectory(
        "com.bbn.bue.common.crossValidation.outputDir");
    final String outputName = parameters.getString("com.bbn.bue.common.crossValidation.outputName");
    final int numBatches = parameters.getPositiveInteger(
        "com.bbn.bue.common.crossValidation.numBatches");
    final int randomSeed = parameters.getInteger("com.bbn.bue.common.crossValidation.randomSeed");

    final ImmutableList<File> inputFiles = FileUtils.loadFileList(fileList);

    int batchNum = 0;
    for (final List<File> testFilesForBatch : Iterables
        .partition(ShufflingIterable.from(inputFiles, new Random(randomSeed)),
            IntMath.divide(inputFiles.size(), numBatches, RoundingMode.UP))) {
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
