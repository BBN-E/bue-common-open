package com.bbn.bue.common;

import com.bbn.bue.common.collections.CollectionUtils;
import com.bbn.bue.common.collections.ListUtils;
import com.bbn.bue.common.files.FileUtils;
import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.symbols.Symbol;
import com.bbn.bue.common.symbols.SymbolUtils;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;

import static com.google.common.base.Predicates.in;

/**
 * Given a list of files and a number of splits, creates training/test file lists for
 * cross-validation. When the files cannot be evenly divided across all splits, extra files are
 * distributed as evenly as possible, starting with the first folds. For example, dividing 11 items
 * into three folds will result in folds of size (4, 4, 3).
 *
 * <p>Normally, cross-validation batches are made such that as much data as possible is used in
 * training. However, if the {@code singleFoldTraining} parameter is set to true, a single fold
 * is used in training. Thus, across folds, each document appears exactly once in the training
 * data and exactly once in the testing data. In a standard cross-validation setup, each data point
 * appears in the training data {@code k-1} times for {@code k} folds and exactly once in the
 * testing data.</p>
 *
 * <p>
 * For example, in a normal cross-validation setup with three folds (A, B, C) you have the following
 * test: train pairs:
 * <pre>
 * A: (B, C)
 * B: (A, C)
 * C: (A, B)
 * </pre>
 *
 * In single fold training you have:
 * <pre>
 * A: B
 * B: C
 * C: A
 * </pre>
 * </p>
 *
 * <p>Note that in the single fold case, each fold is tested by training on the "next" fold. This
 * is arbitrary, but as documents are shuffled before being placed in folds this is not
 * problematic.</p>
 *
 */
public final class MakeCrossValidationBatches {

  private static final Logger log = LoggerFactory.getLogger(MakeCrossValidationBatches.class);

  private static final String PARAM_NAMESPACE = "com.bbn.bue.common.crossValidation.";
  private static final String PARAM_FILE_LIST = PARAM_NAMESPACE + "fileList";
  private static final String PARAM_FILE_MAP = PARAM_NAMESPACE + "fileMap";
  private static final String PARAM_NUM_BATCHES = PARAM_NAMESPACE + "numBatches";
  private static final String PARAM_RANDOM_SEED = PARAM_NAMESPACE + "randomSeed";
  private static final String PARAM_OUTPUT_DIR = PARAM_NAMESPACE + "outputDir";
  private static final String PARAM_OUTPUT_NAME = PARAM_NAMESPACE + "outputName";
  private static final String PARAM_SINGLE_FOLD_TRAINING = PARAM_NAMESPACE + "singleFoldTraining";

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
    // Can run on map or list, but only one of the two.
    parameters.assertExactlyOneDefined(PARAM_FILE_LIST, PARAM_FILE_MAP);

    // Configure for map/list
    boolean useFileMap = false;
    final File sourceFiles;
    if (parameters.isPresent(PARAM_FILE_LIST)) {
      sourceFiles = parameters.getExistingFile(PARAM_FILE_LIST);
    } else if (parameters.isPresent(PARAM_FILE_MAP)) {
      useFileMap = true;
      sourceFiles = parameters.getExistingFile(PARAM_FILE_MAP);
    } else {
      throw new IllegalArgumentException("Impossible state reached");
    }

    // Configure for single fold training.
    boolean singleFoldTraining =
        parameters.getOptionalBoolean(PARAM_SINGLE_FOLD_TRAINING).or(false);

    final File outputDirectory = parameters.getCreatableDirectory(PARAM_OUTPUT_DIR);
    final String outputName = parameters.getString(PARAM_OUTPUT_NAME);
    final int numBatches = parameters.getPositiveInteger(PARAM_NUM_BATCHES);
    final int randomSeed = parameters.getInteger(PARAM_RANDOM_SEED);

    if (numBatches < 1) {
      errorExit("Bad numBatches value: Need one or more batches to divide data into");
    }
    final int maxBatch = numBatches - 1;

    final ImmutableMap<Symbol, File> docIdMap;
    if (useFileMap) {
      docIdMap = FileUtils.loadSymbolToFileMap(Files.asCharSource(sourceFiles, Charsets.UTF_8));
    } else {
      // We load a file list but coerce it into a map
      final ImmutableList<File> inputFiles = FileUtils.loadFileList(sourceFiles);
      docIdMap = Maps.uniqueIndex(inputFiles, FileToSymbolFunction.INSTANCE);

      // Check that nothing was lost in the conversion
      if (docIdMap.size() != inputFiles.size()) {
        errorExit("Input file list contains duplicate entries");
      }
    }

    // Get the list of docids and shuffle them. In the case of using a file list, these are just
    // paths, not document ids, but they serve the same purpose.
    final ImmutableList<Symbol> docIds =
        ListUtils.shuffledCopy(docIdMap.keySet().asList(), new Random(randomSeed));
    if (numBatches > docIds.size()) {
      errorExit("Bad numBatches value: Cannot create more batches than there are input files");
    }

    // Divide into folds
    final ImmutableList<ImmutableList<Symbol>> testFolds =
        CollectionUtils.partitionAlmostEvenly(docIds, numBatches);

    // Write out training/test data for each fold
    final ImmutableList.Builder<File> foldLists = ImmutableList.builder();
    final ImmutableList.Builder<File> foldMaps = ImmutableList.builder();
    int batchNum = 0;
    int totalDocs = 0;

    // Set up train folds
    final ImmutableList<ImmutableList<Symbol>> trainFolds =
        createTrainFolds(testFolds, docIds, singleFoldTraining);

    // Loop over train/test folds
    Preconditions.checkState(trainFolds.size() == testFolds.size());
    for (int i = 0; i < testFolds.size(); i++) {
      final ImmutableList<Symbol> testDocIds = testFolds.get(i);
      final ImmutableList<Symbol> trainDocIds = trainFolds.get(i);
      // Track the total test documents across folds to make sure nothing is lost.
      totalDocs += testDocIds.size();

      // Create maps for training and test. These are sorted to avoid arbitrary ordering.
      final SortedMap<Symbol, File> trainDocIdMap =
          ImmutableSortedMap.copyOf(Maps.filterKeys(docIdMap, in(trainDocIds)),
              SymbolUtils.byStringOrdering());
      final SortedMap<Symbol, File> testDocIdMap =
          ImmutableSortedMap.copyOf(Maps.filterKeys(docIdMap, in(testDocIds)),
              SymbolUtils.byStringOrdering());

      // Don't write out the maps for file lists as the keys are not actually document IDs
      if (useFileMap) {
        final File trainingMapOutputFile = new File(outputDirectory, outputName + "." +
            StringUtils.padWithMax(batchNum, maxBatch) + ".training.docIDToFileMap");
        FileUtils.writeSymbolToFileMap(trainDocIdMap, Files.asCharSink(trainingMapOutputFile,
            Charsets.UTF_8));

        final File testMapOutputFile = new File(outputDirectory, outputName + "." +
            StringUtils.padWithMax(batchNum, maxBatch) + ".test.docIDToFileMap");
        FileUtils.writeSymbolToFileMap(testDocIdMap, Files.asCharSink(testMapOutputFile,
            Charsets.UTF_8));
        foldMaps.add(testMapOutputFile);
      }

      // Write out file lists
      final ImmutableList<File> trainingFilesForBatch = ImmutableList.copyOf(trainDocIdMap.values());
      final ImmutableList<File> testFilesForBatch = ImmutableList.copyOf(testDocIdMap.values());
      final File trainingOutputFile = new File(outputDirectory, outputName + "." +
          StringUtils.padWithMax(batchNum, maxBatch) + ".training.list");
      FileUtils.writeFileList(trainingFilesForBatch,
          Files.asCharSink(trainingOutputFile,
              Charsets.UTF_8));
      final File testOutputFile = new File(outputDirectory, outputName + "." +
          StringUtils.padWithMax(batchNum, maxBatch) + ".test.list");
      FileUtils.writeFileList(testFilesForBatch,
          Files.asCharSink(testOutputFile,
              Charsets.UTF_8));
      foldLists.add(testOutputFile);

      ++batchNum;
    }
    if (totalDocs != docIdMap.size()) {
      errorExit("Test files created are not the same length as the input");
    }

    // Write out lists of files/maps created
    FileUtils.writeFileList(foldLists.build(),
        Files.asCharSink(new File(outputDirectory, "folds.list"), Charsets.UTF_8));
    if (useFileMap) {
      FileUtils.writeFileList(foldMaps.build(),
          Files.asCharSink(new File(outputDirectory, "folds.maplist"), Charsets.UTF_8));
    }
    log.info("Wrote {} cross validation batches from {} to directory {}", numBatches,
        sourceFiles.getAbsoluteFile(), outputDirectory.getAbsolutePath());
  }

  private static ImmutableList<Symbol> shuffledDocIds(final int randomSeed,
      final ImmutableMap<Symbol, File> docIdMap) {
    final ArrayList<Symbol> docIds = Lists.newArrayList(docIdMap.keySet());
    Collections.shuffle(docIds, new Random(randomSeed));
    return ImmutableList.copyOf(docIds);
  }

  private static ImmutableList<ImmutableList<Symbol>> createTrainFolds(
      final ImmutableList<ImmutableList<Symbol>> testFolds,
      final ImmutableList<Symbol> docIds, final boolean singleFoldTraining) {
    final ImmutableList.Builder<ImmutableList<Symbol>> ret = ImmutableList.builder();

    for (int i = 0; i < testFolds.size(); i++) {
      final Set<Symbol> testDocIds = ImmutableSet.copyOf(testFolds.get(i));
      final ImmutableList<Symbol> trainDocIds =
          singleFoldTraining
          // In the single fold training case, use the "next" fold as the training data. We use
          // the modulus to wrap around the list.
          ? testFolds.get((i + 1) % testFolds.size())
          // In the normal case, just use all the remaining data for training.
          : Sets.difference(ImmutableSet.copyOf(docIds), testDocIds).immutableCopy().asList();
      ret.add(trainDocIds);
    }
    return ret.build();
  }

  enum FileToSymbolFunction implements Function<File, Symbol> {
    INSTANCE;

    @Override
    public Symbol apply(final File input) {
      return Symbol.from(input.getPath());
    }
  }
}
