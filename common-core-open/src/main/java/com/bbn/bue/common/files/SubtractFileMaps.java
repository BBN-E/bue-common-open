package com.bbn.bue.common.files;

import com.bbn.bue.common.TextGroupImmutable;
import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Given two key-to-file maps, produces a new map which includes all key-value mappsing from the
 * first where the key is not present in the second.
 *
 * @author Ryan Gabbard
 */
public final class SubtractFileMaps {

  private static final Logger log = LoggerFactory.getLogger(SubtractFileMaps.class);

  private SubtractFileMaps() {
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
    final Parameters params = Parameters.loadSerifStyle(new File(argv[0]));
    log.info(params.dump());

    final File inputFileMapFile = params.getExistingFile("com.bbn.subtractFileMaps.inputMap");
    final File outputFile = params.getCreatableFile("com.bbn.subtractFileMaps.outputMap");
    final StuffToSubtract stuffToSubtract = loadStuffToSubtract(params);

    final ImmutableMap<Symbol, File> inputFiles = FileUtils.loadSymbolToFileMap(inputFileMapFile);

    final ImmutableMap.Builder<Symbol, File> outputFilesB = ImmutableMap.builder();
    for (final Map.Entry<Symbol, File> inputEntry : inputFiles.entrySet()) {
      if (stuffToSubtract.docIDs().contains(inputEntry.getKey())) {
        // put nothing, this entry is deleted
        if (stuffToSubtract.docIDsToFileMap().isPresent()) {
          // if paths are available and matching was requested, they must match
          final File pathInSubtractMap =
              stuffToSubtract.docIDsToFileMap().get().get(inputEntry.getKey());
          final boolean pathsMatch = pathInSubtractMap.equals(inputEntry.getValue());
          if (!pathsMatch) {
            throw new RuntimeException("Mismatch in file maps: for " + inputEntry.getKey()
                + " input has " + inputEntry.getValue() + " to subtract has "
                + pathInSubtractMap);
          }
        }
      } else {
        outputFilesB.put(inputEntry);
      }
    }

    final ImmutableMap<Symbol, File> outputFileMap = outputFilesB.build();

    log.info("Subtracting {}'s {} files from {}'s {} files and writing {} files to to {}",
        stuffToSubtract.path(), stuffToSubtract.docIDs().size(),
        inputFileMapFile, inputFiles.size(),
        outputFileMap.size(), outputFile);

    FileUtils.writeSymbolToFileMap(outputFileMap, Files.asCharSink(outputFile, Charsets.UTF_8));
  }

  public static StuffToSubtract loadStuffToSubtract(final Parameters params) throws IOException {
    final File toSubtractFile = params.getExistingFile("com.bbn.subtractFileMaps.toSubtract");
    final boolean toSubtractIsMap =
        params.getOptionalBoolean("com.bbn.subtractFileMaps.subtrahendIsMap").or(true);
    final boolean requirePathMatch =
        params.getOptionalBoolean("com.bbn.subtractFileMaps.requirePathMatch").or(false);

    if (toSubtractIsMap) {
      final ImmutableMap<Symbol, File> filesToSubtract =
          FileUtils.loadSymbolToFileMap(toSubtractFile);
      final StuffToSubtract.Builder ret = new StuffToSubtract.Builder()
          .path(toSubtractFile)
          .docIDs(filesToSubtract.keySet());
      if (requirePathMatch) {
        ret.docIDsToFileMap(filesToSubtract).build();
      }
      return ret.build();
    } else {
      return new StuffToSubtract.Builder()
          .path(toSubtractFile)
          .docIDs(FileUtils.loadSymbolSet(Files.asCharSource(toSubtractFile, Charsets.UTF_8)))
          .build();
    }
  }
}

@TextGroupImmutable
@Value.Immutable
abstract class StuffToSubtract {

  public abstract File path();

  public abstract ImmutableSet<Symbol> docIDs();

  public abstract Optional<ImmutableMap<Symbol, File>> docIDsToFileMap();

  public static class Builder extends ImmutableStuffToSubtract.Builder {

  }
}
