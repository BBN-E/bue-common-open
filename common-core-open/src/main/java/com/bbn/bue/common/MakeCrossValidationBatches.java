package com.bbn.bue.common;

import com.bbn.bue.common.collections.CollectionUtils;
import com.bbn.bue.common.files.FileUtils;
import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.symbols.Symbol;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static com.google.common.base.Predicates.in;

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
    System.err.println("Error: " + msg);
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
    final boolean useDocIdMap = parameters.getBoolean("com.bbn.bue.common.crossValidation.useDocIdMap");

    if (numBatches < 1) {
      errorExit("Bad numBatches value: Need one or more batches to divide data into");
    }

    if (useDocIdMap) {
      // For the document ID map
      final ImmutableMap<Symbol, File> docIdMap =
          FileUtils.loadSymbolToFileMap(Files.asCharSource(fileList, Charsets.UTF_8));

      // Get the list of docids and shuffle them
      final ArrayList<Symbol> docIds = Lists.newArrayList(docIdMap.keySet());
      if (numBatches > docIds.size()) {
        errorExit("Bad numBatches value: Cannot create more batches than there are input files");
      }
      Collections.shuffle(docIds, new Random(randomSeed));

      // Divide into folds
      final ImmutableList<ImmutableList<Symbol>> folds =
          CollectionUtils.partitionAlmostEvenly(docIds, numBatches);

      int batchNum = 0;
      int totalTest = 0;
      for (final List<Symbol> docIdsForBatch : folds) {
        final Set<Symbol> testDocIds = ImmutableSet.copyOf(docIdsForBatch);
        final Set<Symbol> trainDocIds =
            Sets.difference(ImmutableSet.copyOf(docIds), testDocIds).immutableCopy();

        final Map<Symbol, File> trainDocIdMap =
            Maps.filterKeys(docIdMap, in(trainDocIds));

        final Map<Symbol, File> testDocIdMap =
            Maps.filterKeys(docIdMap, in(testDocIds));

        // Write out file maps
        final File trainingMapOutputFile = new File(outputDirectory, outputName + "." +
            StringUtils.padWithMax(batchNum, numBatches - 1) + ".training.docIDToFileMap");
        FileUtils.writeSymbolToFileMap(trainDocIdMap, Files.asCharSink(trainingMapOutputFile,
            Charsets.UTF_8));
        final File testMapOutputFile = new File(outputDirectory, outputName + "." +
            StringUtils.padWithMax(batchNum, numBatches - 1) + ".test.docIDToFileMap");
        FileUtils.writeSymbolToFileMap(testDocIdMap, Files.asCharSink(testMapOutputFile,
            Charsets.UTF_8));

        // Write out file lists
        final ImmutableList<File> trainingFilesForBatch = ImmutableList.copyOf(trainDocIdMap.values());
        final ImmutableList<File> testFilesForBatch = ImmutableList.copyOf(testDocIdMap.values());
        final File trainingOutputFile = new File(outputDirectory, outputName + "." +
            StringUtils.padWithMax(batchNum, numBatches - 1) + ".training.list");
        FileUtils.writeFileList(trainingFilesForBatch,
            Files.asCharSink(trainingOutputFile,
                Charsets.UTF_8));
        final File testOutputFile = new File(outputDirectory, outputName + "." +
            StringUtils.padWithMax(batchNum, numBatches - 1) + ".test.list");
        FileUtils.writeFileList(testFilesForBatch,
            Files.asCharSink(testOutputFile,
                Charsets.UTF_8));

        ++batchNum;
        totalTest += testDocIdMap.size();
      }
      if(totalTest != docIdMap.size()) {
        errorExit("Test files created are not the same length as the input");
      }
    } else {

      // Load the list of files and shuffle.
      final List<File> inputFiles = Lists.newArrayList(FileUtils.loadFileList(fileList));
      if (Sets.newHashSet(inputFiles).size() != inputFiles.size()) {
        errorExit("Input file list contains duplicate entries");
      }
      else if (numBatches > inputFiles.size()) {
        errorExit("Bad numBatches value: Cannot create more batches than there are input files");
      }
      Collections.shuffle(inputFiles, new Random(randomSeed));

      // Divide into folds
      final ImmutableList<ImmutableList<File>> folds =
          CollectionUtils.partitionAlmostEvenly(inputFiles, numBatches);

      int batchNum = 0;
      int totalTest = 0;
      for (final List<File> testFilesForBatch : folds) {
        final Set<File> trainingFilesForBatch =
            Sets.difference(ImmutableSet.copyOf(inputFiles), ImmutableSet.copyOf(testFilesForBatch))
                .immutableCopy();

        final File trainingOutputFile = new File(outputDirectory, outputName + "." +
            StringUtils.padWithMax(batchNum, numBatches - 1) + ".training.list");
        FileUtils.writeFileList(Ordering.natural().sortedCopy(trainingFilesForBatch),
            Files.asCharSink(trainingOutputFile,
                Charsets.UTF_8));
        final File testOutputFile = new File(outputDirectory, outputName + "." +
            StringUtils.padWithMax(batchNum, numBatches - 1) + ".test.list");
        FileUtils.writeFileList(Ordering.natural().sortedCopy(testFilesForBatch),
            Files.asCharSink(testOutputFile,
                Charsets.UTF_8));
        ++batchNum;
        totalTest += testFilesForBatch.size();
      }
      if(totalTest != inputFiles.size()) {
        errorExit("Test files created are not the same length as the input");
      }
    }
  }
}
