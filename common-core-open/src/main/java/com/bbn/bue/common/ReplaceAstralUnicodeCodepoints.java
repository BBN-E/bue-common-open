package com.bbn.bue.common;

import com.bbn.bue.common.files.FileUtils;
import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;

import static com.bbn.bue.common.CodepointMatcher.basicMultilingualPlane;
import static com.bbn.bue.common.CodepointMatcher.not;
import static com.bbn.bue.common.files.FileUtils.toAbsolutePathFunction;
import static com.bbn.bue.common.symbols.SymbolUtils.symbolizeFunction;
import static com.google.common.base.Functions.compose;
import static com.google.common.base.Preconditions.checkState;

/**
 * Replaces all characters in input documents which contain code points outside the basic
 * multilingual plane (hence 'astral') with the Unicode Replacement Character.  This is useful
 * if you are (a) working with a codepoint-based notion of offsets (which we typically need to for
 * evaluations) and (b) are working with software which cannot properly count non-BMP characters
 * as a single code point (many programs use an internal UTF-16 representations and will
 * represent these codepoints as two characters).
 *
 * Input documents are expected to be UTF-8 and output will be written in UTF-8.
 *
 * See documentation for parameter constants below for parameters to use
 *
 * @author Ryan Gabbard
 */
public final class ReplaceAstralUnicodeCodepoints implements TextGroupEntryPoint {

  private static final Logger log = LoggerFactory.getLogger(ReplaceAstralUnicodeCodepoints.class);

  private final Parameters parameters;

  @Inject
  ReplaceAstralUnicodeCodepoints(final Parameters parameters) {
    this.parameters = parameters;
  }

  // input must be specified *either* as a list of absolute paths (with no duplicates)
  // or a tab-separate map of keys to absolute paths.
  public static final String INPUT_LIST_PARAM = "inputFileList";
  public static final String INPUT_MAP_PARAM = "inputFileMap";

  // for output, you have two options.  (a) You can write the output in-place back to the input files
  // by setting the inPlace parameter to true or (b) you can write the output to a directory by
  // providing the outputDirectory parameter and the basePath parameter.  Output files will have
  // the same position relative to outputDirectory as the input files did to basePath
  public static final String IN_PLACE_PARAM = "inPlace";
  public static final String BASE_PATH_PARAM = "basePath";
  public static final String OUTPUT_DIR_PARAM = "outputDirectory";

  // you can request either a list or map (or both) specifying the absolute paths of the output
  // files be written. You can only request an output map if an input map was given, in which
  // case keys are preserved
  public static final String OUTPUT_MAP_PARAM = "outputFileMap";
  public static final String OUTPUT_LIST_PARAM = "outputFileList";

  private static final char UNICODE_REPLACEMENT_CHARACTER = '\uFFFD';

  // method contains many safe .get()s with complex checks IntelliJ can't find
  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @Override
  public void run() throws Exception {
    final Optional<File> inputMapFile = parameters.getOptionalExistingFile(INPUT_MAP_PARAM);
    final Optional<File> inputListFile = parameters.getOptionalExistingFile(INPUT_LIST_PARAM);
    final Optional<File> outputMapFile = parameters.getOptionalCreatableFile(OUTPUT_MAP_PARAM);
    final Optional<File> outputListFile = parameters.getOptionalCreatableFile(OUTPUT_LIST_PARAM);
    final Optional<File> outputDirectory =
        parameters.getOptionalCreatableDirectory(OUTPUT_DIR_PARAM);
    final boolean inPlace = parameters.getOptionalBoolean(IN_PLACE_PARAM).or(false);
    final Optional<File> basePath = parameters.getOptionalExistingDirectory(BASE_PATH_PARAM);

    if (inputListFile.isPresent() == inputMapFile.isPresent()) {
      log.error("Exactly one input parameter given. Expected exactly one of {} and {}",
          INPUT_LIST_PARAM, INPUT_MAP_PARAM);
      System.exit(1);
    }

    if (!outputListFile.isPresent() && !outputMapFile.isPresent() && !inPlace) {
      log.error("No output parameter given. Expected {} or {} or for {} to be true",
          OUTPUT_LIST_PARAM, OUTPUT_MAP_PARAM, IN_PLACE_PARAM);
      System.exit(1);
    }

    if (outputMapFile.isPresent() && !inputMapFile.isPresent()) {
      log.error("Cannot use {} without {}", OUTPUT_MAP_PARAM, INPUT_MAP_PARAM);
      System.exit(1);
    }

    if (!inPlace && !basePath.isPresent()) {
      log.error("Cannot determine whow to output. Either {} must be true or {} must be "
          + "specified", IN_PLACE_PARAM, BASE_PATH_PARAM);
    }

    final ImmutableMap<Symbol, File> inputFileMap;
    if (inputMapFile.isPresent()) {
      inputFileMap = FileUtils.loadSymbolToFileMap(inputMapFile.get());
    } else {
      // if a map wasn't supplied, we make a fake map using the file path as the key.
      // we know the key won't matter, because we know map output wasn't requested or
      // the checks above would have caught the missing input map
      // .get() is safe by checks above
      inputFileMap = Maps.uniqueIndex(FileUtils.loadFileList(inputListFile.get()),
          compose(symbolizeFunction(), toAbsolutePathFunction()));
    }
    log.info("Cleaning {} input files {}", inputFileMap.size(),
        inPlace ? "in-place" : ("to " + outputDirectory.get().getAbsolutePath()));

    int totalCharactersReplaced = 0;
    int numFilesWithReplacements = 0;

    final ImmutableList.Builder<File> outputFiles = ImmutableList.builder();
    final ImmutableMap.Builder<Symbol, File> outputMap = ImmutableMap.builder();
    for (final Map.Entry<Symbol, File> e : inputFileMap.entrySet()) {
      final File inputFileName = e.getValue();
      final File outFile;
      if (inPlace) {
        outFile = inputFileName;
      } else {
        // write the output to a file with the same position relative to the output directory
        // as the input had relative to the input directory
        // get safe by checks above
        //noinspection OptionalGetWithoutIsPresent
        outFile = outputDirectory.get().toPath().resolve(
            basePath.get().toPath().relativize(inputFileName.toPath())).toFile();
      }
      //noinspection ResultOfMethodCallIgnored
      outFile.getParentFile().mkdirs();

      final String originalText = Files.asCharSource(inputFileName, Charsets.UTF_8).read();
      final int numReplacementCharsInOriginal =
          CodepointMatcher.forCharacter(UNICODE_REPLACEMENT_CHARACTER).countIn(originalText);
      final String cleanedText = not(basicMultilingualPlane()).replaceAll(
          originalText, UNICODE_REPLACEMENT_CHARACTER);
      final int numReplacementCharsInCleaned =
          CodepointMatcher.forCharacter(UNICODE_REPLACEMENT_CHARACTER).countIn(cleanedText);
      Files.asCharSink(outFile, Charsets.UTF_8).write(cleanedText);

      outputFiles.add(outFile);
      outputMap.put(e.getKey(), outFile);

      final int numCharsReplaced = numReplacementCharsInCleaned - numReplacementCharsInOriginal;

      checkState(numCharsReplaced >= 0,
          "Number of replacement characters went down.  Either this program is buggy or you have"
              + " a very broken and bizarre document");
      if (numCharsReplaced > 0) {
        log.info("Replaced {} non-BMP code points with the Unicode replacement character for"
            + "input file {}", numCharsReplaced, inputFileName);
        totalCharactersReplaced += numCharsReplaced;
        ++numFilesWithReplacements;
      }
    }

    log.info("Replaced {} non-BMP characters in {} files",
        totalCharactersReplaced, numFilesWithReplacements);
    if (outputListFile.isPresent()) {
      log.info("Writing list of transformed files to {}", outputListFile.get());
      FileUtils.writeFileList(outputFiles.build(), Files.asCharSink(outputListFile.get(),
          Charsets.UTF_8));
    }
    if (outputMapFile.isPresent()) {
      log.info("Writing map of transformed files to {}", outputMapFile.get());
      FileUtils.writeSymbolToFileMap(outputMap.build(), Files.asCharSink(outputMapFile.get(),
          Charsets.UTF_8));
    }
  }

  public static void main(String[] args) throws Exception {
    TextGroupEntryPoints.runEntryPoint(ReplaceAstralUnicodeCodepoints.class, args);
  }

  // does nothing, just here to make TextGroupEntryPoints.runEntryPoint happy
  static class Module extends AbstractModule {

    @Override
    protected void configure() {

    }
  }
}
