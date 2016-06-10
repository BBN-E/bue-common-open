package com.bbn.bue.common;

import com.bbn.bue.common.collections.CollectionUtils;
import com.bbn.bue.common.collections.ListUtils;
import com.bbn.bue.common.files.FileUtils;
import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.math.DoubleMath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Divides a data set into almost equally-sized partitions after optionally holding out a portion
 * of the data. For example, this could be used to divide into three equal partitions (33.3% of the
 * data each), to hold out 10% of the data and then split into three partitions (10% held out,
 * 30% in each partition), or do a simple train/test split (by specifying one partition).
 *
 * This uses the following parameters in the com.bbn.bue.common.partitionData namespace:
 *
 * fileList or fileMap: Specify exactly of these to give the input as a file list or document id to
 * file map. If a list is specified, all output is in the form of file lists, etc.
 *
 * holdOutProportion: The proportion of the data to hold out, for example .25 for 25%. May be zero.
 * holdOutFile: The file to write the held out file list/map to. Must be specified if
 * holdOutProportion is greater than zero. It is an error to specify this if holdOutProportion is
 * zero.
 *
 * numPartitions: The number of partitions to create. Partitions are created after any held out
 * data is removed. This can be one to allow for a simple train/test split that is defined using
 * holdOutProportion.
 * randomSeed: The random seed to use when shuffling the data before data is held out and
 * partitioned.
 *
 * partitionOutputDir: The directory to write the file lists/maps that give each partition.
 * partitionListFile: The file to write the list of partition lists/maps to.
 * partitionPrefix: The prefix to give the filename of each partition. The output files will be
 * of the format <code>%partitionOutputDir%/%partitionPrefix%.%partitionNumber%.{map,list}</code>
 * where the partition number is zero-indexed.
 */
public final class PartitionData {

  private static final Logger log = LoggerFactory.getLogger(PartitionData.class);

  private static final String PARAM_NAMESPACE = "com.bbn.bue.common.partitionData.";
  // The input file list or map
  private static final String PARAM_FILE_LIST = PARAM_NAMESPACE + "fileList";
  private static final String PARAM_FILE_MAP = PARAM_NAMESPACE + "fileMap";

  // File to write out held out data to
  private static final String PARAM_HOLD_OUT_PATH = PARAM_NAMESPACE + "holdOutFile";
  // Output directory for writing partitions
  private static final String PARAM_OUTPUT_DIR = PARAM_NAMESPACE + "partitionOutputDir";
  // Prefix for file names of partition map/lists
  private static final String PARAM_PARTITION_LIST = PARAM_NAMESPACE + "partitionListFile";
  // Prefix for file names of partition map/lists
  private static final String PARAM_PARTITION_PREFIX = PARAM_NAMESPACE + "partitionPrefix";

  // The proportion of the data to hold out
  private static final String PARAM_HOLD_OUT = PARAM_NAMESPACE + "holdOutProportion";

  // The number of partitions
  private static final String PARAM_PARTITIONS = PARAM_NAMESPACE + "numPartitions";

  // Random seed for shuffling
  private static final String PARAM_RANDOM_SEED = PARAM_NAMESPACE + "randomSeed";

  private PartitionData() {
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
      errorExit("Usage: PartitionData params");
    }
    final Parameters parameters = Parameters.loadSerifStyle(new File(argv[0]));
    // Can run on map or list, but only one of the two.
    parameters.assertExactlyOneDefined(PARAM_FILE_LIST, PARAM_FILE_MAP);

    // Configure for map/list
    // This will contain the file paths in the case of a file list, or the docids in the case
    // of a docid to file map.
    ImmutableList<Symbol> documents;
    // This is null if we are not using a map
    final ImmutableMap<Symbol, File> documentMap;
    if (parameters.isPresent(PARAM_FILE_LIST)) {
      final File fileList = parameters.getExistingFile(PARAM_FILE_LIST);
      log.info("Loading file list from {}", fileList);
      documentMap = null;
      documents =
          FluentIterable.from(FileUtils.loadFileList(fileList))
              .transform(MakeCrossValidationBatches.FileToSymbolFunction.INSTANCE)
              .toList();
    } else if (parameters.isPresent(PARAM_FILE_MAP)) {
      final File fileMap = parameters.getExistingFile(PARAM_FILE_MAP);
      log.info("Loading file map from {}", fileMap);
      documentMap = FileUtils.loadSymbolToFileMap(fileMap);
      documents = documentMap.keySet().asList();
    } else {
      // Should be unreachable. Used instead of checkState to satisfy compiler.
      throw new IllegalStateException("Input is neither file list nor map");
    }
    log.info("Loaded {} documents", documents.size());

    final File outputDirectory = parameters.getCreatableDirectory(PARAM_OUTPUT_DIR);
    final File partitionListFile = parameters.getCreatableFile(PARAM_PARTITION_LIST);
    final Optional<File> holdOutFile = parameters.getOptionalCreatableFile(PARAM_HOLD_OUT_PATH);
    final String partitionPrefix = parameters.getString(PARAM_PARTITION_PREFIX);

    final int nPartitions = parameters.getPositiveInteger(PARAM_PARTITIONS);
    final int randomSeed = parameters.getInteger(PARAM_RANDOM_SEED);
    final double holdOut = parameters.getProbability(PARAM_HOLD_OUT);

    // 1.0 is a valid probability but not a valid hold out value
    checkArgument(holdOut != 1.0, "Hold out proportion must be less than all of the data");
    checkArgument(holdOutFile.isPresent() == (holdOut > 0.0),
        PARAM_HOLD_OUT + " must be specified if and only if hold out amount is greater than zero");
    checkArgument(holdOut > 0.0 || nPartitions > 1,
        "Neither hold out nor more than one partition specified. Nothing to do.");

    // Figure out how much is held out
    final int nDocuments = documents.size();
    final int nHeldOut = DoubleMath.roundToInt(nDocuments * holdOut, RoundingMode.HALF_UP);
    // Prevent requesting .99999 of 10 documents, which is all of them.
    checkArgument(nHeldOut < nDocuments, "Cannot hold out all documents");
    // Prevent requesting .00001 of 10 documents, which is none of them.
    checkArgument(holdOut == 0.0 || nHeldOut > 0,
        "Hold out amount is non-zero but less than one document");
    log.info("Holding out {} documents", nHeldOut);

    // Compute how much is left over after hold out
    final int nRemaining = nDocuments - nHeldOut;
    checkArgument(nRemaining >= nPartitions,
        "More partitions requested than number of non-held out documents");
    log.info("Dividing {} documents into {} partitions", nRemaining, nPartitions);

    // Shuffle and replace the original to avoid incorrect references.
    documents = ListUtils.shuffledCopy(documents, new Random(randomSeed));

    // Hold out beginning of list
    final ImmutableSet<Symbol> heldOut = ImmutableSet.copyOf(documents.subList(0, nHeldOut));
    final ImmutableSet<Symbol> remaining =
        ImmutableSet.copyOf(documents.subList(nHeldOut, nDocuments));
    checkState(heldOut.size() + remaining.size() == nDocuments,
        "Number of documents in held out and partitioned data differs from original number of documents");

    int outputDocuments = 0;
    if (holdOut > 0.0) {
      // Already checked about that holdOutFile is present in this case
      outputDocuments += writeHoldOut(heldOut, holdOutFile.get(), documentMap);
    }

    outputDocuments += writePartitions(remaining, nPartitions, outputDirectory, partitionListFile,
        partitionPrefix, documentMap);

    checkState(nDocuments == outputDocuments, "Incorrect number of documents written");
  }

  private static int writeHoldOut(final ImmutableSet<Symbol> heldOut,
      final File holdOutFile, final ImmutableMap<Symbol, File> documentMap) throws IOException {
    if (documentMap != null) {
      FileUtils.writeSymbolToFileMap(filterMapToKeysPreservingOrder(documentMap, heldOut),
          Files.asCharSink(holdOutFile, Charsets.UTF_8));
    } else {
      FileUtils.writeFileList(Lists.transform(heldOut.asList(), SymbolToFileFunction.INSTANCE),
          Files.asCharSink(holdOutFile, Charsets.UTF_8));
    }
    log.info("Wrote held out data to {}", holdOutFile);
    return heldOut.size();
  }

  private static int writePartitions(final ImmutableSet<Symbol> remaining, final int nPartitions,
      final File outputDirectory, final File partitionListFile, final String partitionPrefix,
      final ImmutableMap<Symbol, File> documentMap) throws IOException {
    int outputDocuments = 0;

    // Partition remaining data
    final ImmutableList<ImmutableList<Symbol>> partitions =
        CollectionUtils.partitionAlmostEvenly(remaining.asList(), nPartitions);

    // Write out each partition
    log.info("Writing partitions to directory {}", outputDirectory);
    int maxPartition = partitions.size();
    final ImmutableList.Builder<File> partitionFiles = ImmutableList.builder();
    for (int partitionNum = 0; partitionNum < partitions.size(); partitionNum++) {
      final ImmutableList<Symbol> partition = partitions.get(partitionNum);
      final String partitionFileName = partitionPrefix + '.'
          + StringUtils.padWithMax(partitionNum, maxPartition);
      final File partitionFile;
      if (documentMap != null) {
        partitionFile = new File(outputDirectory, partitionFileName + ".map");
        FileUtils.writeSymbolToFileMap(filterMapToKeysPreservingOrder(documentMap, partition),
            Files.asCharSink(partitionFile, Charsets.UTF_8));
      } else {
        partitionFile = new File(outputDirectory, partitionFileName + ".list");
        FileUtils.writeFileList(Lists.transform(partition, SymbolToFileFunction.INSTANCE),
            Files.asCharSink(partitionFile, Charsets.UTF_8));
      }
      partitionFiles.add(partitionFile);
      outputDocuments += partition.size();
      log.info("Wrote partition {} to {}", partitionNum, partitionFile);
    }

    // Write out lists of files/maps created
    FileUtils.writeFileList(partitionFiles.build(),
        Files.asCharSink(partitionListFile, Charsets.UTF_8));
    log.info("Wrote partition list to {}", partitionListFile);

    return outputDocuments;
  }

  /**
   * Filters a map down to the specified keys such that the new map has the same iteration order as
   * the specified keys.
   */
  private static <K, V> ImmutableMap<K, V> filterMapToKeysPreservingOrder(
      final ImmutableMap<? extends K, ? extends V> map, Iterable<? extends K> keys) {
    final ImmutableMap.Builder<K, V> ret = ImmutableMap.builder();
    for (final K key : keys) {
      final V value = map.get(key);
      checkArgument(value != null, "Key " + key + " not in map");
      ret.put(key, value);
    }
    return ret.build();
  }

  private enum SymbolToFileFunction implements Function<Symbol, File> {
    INSTANCE;

    @Override
    public File apply(final Symbol input) {
      return new File(input.asString());
    }
  }
}
