package com.bbn.bue.common.files;

import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.symbols.Symbol;
import com.bbn.bue.common.symbols.SymbolUtils;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.math.IntMath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Functions.compose;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Given a file list, a file map, or both, will split it into either a fixed number of chunks or
 * chunks of a fixed size.  This is primarily used by Corpus::split() in Corpus.pm in
 * buetext/perl-modules for splitting corpora in Runjobs code.
 *
 * The behavior of this program is deterministic.
 *
 * @author Constantine Lignos, Ryan Gabbard
 */
public final class SplitCorpus {

  private static final Logger log = LoggerFactory.getLogger(SplitCorpus.class);

  private static final String USAGE = "SplitCorpus paramFile\n"
      + "Parameters are:\n"
      + "\tcom.bbn.bue.splitCorpus.inputList: list of files to split. Optional.\n"
      + "\tcom.bbn.bue.splitCorpus.inputMap: docId to file map of files to split. Optional.\n"
      + "\tcom.bbn.bue.splitCorpus.outputDir: path to write output\n"
      + "\tcom.bbn.bue.splitCorpus.numChunks: the number of chunks to split the corpus into. Optional.\n"
      + "\tcom.bbn.bue.splitCorpus.chunkSize: the number of of files to put in each chunk. Optional.\n"
      + "\n"
      + "If inputList is given, output file lists will be written to outputDir/split/fileList.txt\n"
      + "\tand a list of these lists will be written to outputDir/listOfLists.txt\n"
      + "If inputMap is given, output file maps will be written to outputDir/split/fileMap.txt\n"
      + "\tand a list of these maps will be written to outputDir/listOfMaps.txt\n"
      + "At least one of inputList and inputMap must be specified.\n"
      + "Exactly one of numChunks and chunkSize may be specified.";

  public static final String INPUT_LIST_PARAM = "com.bbn.bue.splitCorpus.inputList";
  public static final String INPUT_MAP_PARAM = "com.bbn.bue.splitCorpus.inputMap";
  public static final String OUTPUT_DIR_PARAM = "com.bbn.bue.splitCorpus.outputDir";
  public static final String NUM_CHUNKS_PARAM = "com.bbn.bue.splitCorpus.numChunks";
  public static final String CHUNK_SIZE_PARAM = "com.bbn.bue.splitCorpus.chunkSize";

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
    if (argv.length != 1) {
      log.info(USAGE);
      System.exit(1);
    }
    final Parameters params = Parameters.loadSerifStyle(new File(argv[0]));

    final File outputDir = params.getCreatableDirectory(OUTPUT_DIR_PARAM);

    params.assertAtLeastOneDefined(INPUT_LIST_PARAM, INPUT_MAP_PARAM);
    final Optional<File> inputFileListFile = params.getOptionalExistingFile(INPUT_LIST_PARAM);
    final Optional<File> inputFileMapFile = params.getOptionalExistingFile(INPUT_MAP_PARAM);

    final ImmutableMap<Symbol, File> docIdToFileMap =
        loadDocIdToFileMap(inputFileListFile, inputFileMapFile);

    params.assertExactlyOneDefined(NUM_CHUNKS_PARAM, CHUNK_SIZE_PARAM);
    final Iterable<List<Map.Entry<Symbol, File>>> chunks;
    if (params.isPresent(NUM_CHUNKS_PARAM)) {
      chunks = splitToNChunks(docIdToFileMap, params.getPositiveInteger(NUM_CHUNKS_PARAM));
    } else {
      chunks =
          splitToChunksOfFixedSize(docIdToFileMap, params.getPositiveInteger(CHUNK_SIZE_PARAM));
    }

    final List<File> listFiles = Lists.newArrayList();
    final List<File> mapFiles = Lists.newArrayList();

    int chunkIdx = 0;
    for (final List<Map.Entry<Symbol, File>> chunk : chunks) {
      final ImmutableMap<Symbol, File> chunkDocIdToFileMap = ImmutableMap.copyOf(chunk);
      final ImmutableList<File> chunkFileList = ImmutableList.copyOf(chunkDocIdToFileMap.values());

      final File chunkOutputDir = new File(outputDir, Integer.toString(chunkIdx));
      chunkOutputDir.mkdir();

      final File chunkMapFile = new File(chunkOutputDir, "fileMap.txt");
      mapFiles.add(chunkMapFile);
      if (inputFileMapFile.isPresent()) {
        // maps are only written if a map was given as input
        FileUtils.writeSymbolToFileMap(chunkDocIdToFileMap,
            Files.asCharSink(chunkMapFile, Charsets.UTF_8));
      }

      final File chunkListFile = new File(chunkOutputDir, "fileList.txt");
      listFiles.add(chunkListFile);
      if (inputFileListFile.isPresent()) {
        // lists are only written if a list was given as input
        FileUtils.writeFileList(chunkFileList,
            Files.asCharSink(chunkListFile, Charsets.UTF_8));
      }
      ++chunkIdx;
    }

    // write lists pointing to output files

    log.info("Split into {} chunks", chunkIdx);
    if (inputFileListFile.isPresent()) {
      // lists are only written if a list was given as input
      final File listOfListsFile = new File(outputDir, "listOfLists.txt");
      log.info("List of file lists written to {}", listOfListsFile);
      FileUtils.writeFileList(listFiles,
          Files.asCharSink(listOfListsFile, Charsets.UTF_8));
    }

    if (inputFileMapFile.isPresent()) {
      // maps are only written if a map was given as input
      final File listOfMapsFile = new File(outputDir, "listOfMaps.txt");
      log.info("List of file maps written to {}", listOfMapsFile);
      FileUtils.writeFileList(mapFiles,
          Files.asCharSink(listOfMapsFile, Charsets.UTF_8));
    }
  }

  private static Iterable<List<Map.Entry<Symbol, File>>> splitToChunksOfFixedSize(
      final ImmutableMap<Symbol, File> inputMap, int chunkSize) {
    checkArgument(chunkSize > 0);
    return Iterables.partition(inputMap.entrySet(), chunkSize);
  }

  /**
   * If there are fewer files than chunks, fewer than numChunks will be returned.
   */
  private static Iterable<List<Map.Entry<Symbol, File>>> splitToNChunks(
      final ImmutableMap<Symbol, File> inputMap, int numChunks) {
    checkArgument(numChunks > 0);
    final List<Map.Entry<Symbol, File>> emptyChunk = ImmutableList.of();

    if (inputMap.isEmpty()) {
      return Collections.nCopies(numChunks, emptyChunk);
    }

    final int chunkSize = IntMath.divide(inputMap.size(), numChunks, RoundingMode.UP);
    final ImmutableList<List<Map.Entry<Symbol, File>>> chunks =
        ImmutableList.copyOf(splitToChunksOfFixedSize(inputMap, chunkSize));
    if (chunks.size() == numChunks) {
      return chunks;
    } else {
      // there weren't enough elements to make the desired number of chunks, so we need to
      // pad with empty chunks
      final int shortage = numChunks - chunks.size();
      final List<List<Map.Entry<Symbol, File>>> padding = Collections.nCopies(shortage, emptyChunk);
      return Iterables.concat(chunks, padding);
    }
  }

  /**
   * Gets a doc-id-to-file map for the input, either directly or making a fake one based on an input
   * file list.
   */
  private static ImmutableMap<Symbol, File> loadDocIdToFileMap(
      final Optional<File> inputFileListFile,
      final Optional<File> inputFileMapFile) throws IOException {
    checkArgument(inputFileListFile.isPresent() || inputFileMapFile.isPresent());

    final Optional<ImmutableList<File>> fileList;
    if (inputFileListFile.isPresent()) {
      fileList = Optional.of(FileUtils.loadFileList(inputFileListFile.get()));
    } else {
      fileList = Optional.absent();
    }

    final Optional<ImmutableMap<Symbol, File>> fileMap;
    if (inputFileMapFile.isPresent()) {
      fileMap = Optional.of(FileUtils.loadSymbolToFileMap(inputFileMapFile.get()));
    } else {
      fileMap = Optional.absent();
    }

    // sanity checks

    if (fileList.isPresent()) {
      // file list may not contain duplicates
      final boolean containsDuplicates =
          ImmutableSet.copyOf(fileList.get()).size() != fileList.get().size();
      if (containsDuplicates) {
        throw new RuntimeException("Input file list contains duplicates");
      }
    }

    // if both a file map and a file list are given, they must be compatible.
    if (fileList.isPresent() && fileMap.isPresent()) {
      if (fileList.get().size() != fileMap.get().size()) {
        throw new RuntimeException("Input file list and file map do not match in size ("
            + fileList.get().size() + " vs " + fileMap.get().size());
      }
      final boolean haveExactlyTheSameFiles =
          ImmutableSet.copyOf(fileList.get()).equals(ImmutableSet.copyOf(fileMap.get().values()));
      if (!haveExactlyTheSameFiles) {
        throw new RuntimeException(
            "Input file list and file map do not containe exactly the same files");
      }
    }

    // output

    if (fileMap.isPresent()) {
      return fileMap.get();
    } else {
      // if we only had a file list as input, we make a fake doc-id-to-file-map using
      // the absolute path as the document ID.  This won't get output, so it doesn't matter
      // that this is a little hacky
      final Function<File, Symbol> fileNameAsSymbolFunction =
          compose(SymbolUtils.symbolizeFunction(), FileUtils.toAbsolutePathFunction());
      return Maps.uniqueIndex(fileList.get(), fileNameAsSymbolFunction);
    }
  }
}
