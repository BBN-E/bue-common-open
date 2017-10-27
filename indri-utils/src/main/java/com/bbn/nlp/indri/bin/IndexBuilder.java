package com.bbn.nlp.indri.bin;

import com.bbn.bue.common.parameters.Parameters;
import com.bbn.nlp.indri.IndriFileProcessor;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import lemurproject.indri.IndexEnvironment;
import lemurproject.indri.IndexStatus;
import lemurproject.indri.Specification;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Predicates.compose;
import static com.google.common.base.Predicates.in;

public final class IndexBuilder {

  private static final Logger log = LoggerFactory.getLogger(IndexBuilder.class);

  private IndexBuilder() {
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

  // static to ensure it is never garbage collected, which would
  // lead to a crash in native code
  private static final StatusMonitor statusMonitor = new StatusMonitor();

  // A sample parameter file can be found at buetext/indri/Gigaword/gigaword_v5.params
  // memoryInMB:      How much memory the indexer should use. The more it has the faster it tends to
  //                  run.  A few gigabytes at least is recommended.
  // outputDirectory: Output directory that gets overwritten.  Anything in it will be deleted.
  // storeDocs:       If true the documents themselves will be stored in the Indri index.
  private static void trueMain(String[] argv) throws Exception {
    if (argv.length != 1) {
      System.err.println("Please provide a params file as the sole command line argument");
      System.exit(-1);
    }
    final Parameters params = Parameters.loadSerifStyle(new File(argv[0]));

    final File outputDirectory = params.getCreatableDirectory("outputDirectory");
    final File corpusRoot = params.getExistingDirectory("corpusRoot");
    final int memory = params.getPositiveInteger("memoryInMB");
    final boolean storeDocs = params.getBoolean("storeDocs");
    final Predicate<File> fileFilter = getFileFilter(params);
    final IndriFileProcessor indriFileProcessor = params.getParameterInitializedObject(
        "indriFileProcessorClass", IndriFileProcessor.class);

    log.info("Building index for corpus {} format to {} using processor {}",
        corpusRoot, outputDirectory, indriFileProcessor.getClass().getName());

    final IndexEnvironment env = setupIndexer(outputDirectory, memory, storeDocs);

    recursiveIndex(corpusRoot, env, indriFileProcessor,
        fileFilter);
    log.info("Indexed {} documents", env.documentsIndexed());
    env.close();
    log.info("Index complete");
  }

  private static void recursiveIndex(File dir, IndexEnvironment env,
      IndriFileProcessor indriFileProcessor, Predicate<File> fileFilter) throws Exception {
    checkArgument(dir.isDirectory());
    for (final File f : dir.listFiles()) {
      if (f.isFile() && fileFilter.apply(f)) {
        index(f, env, indriFileProcessor);
        if (env.documentsIndexed() % 100 == 0) {
          log.info("Indexed {} documents", env.documentsIndexed());
        }
      } else if (f.isDirectory()) {
        recursiveIndex(f, env, indriFileProcessor, fileFilter);
      }
    }
  }

  private static final ImmutableMap<String, String> NO_METADATA = ImmutableMap.of();

  private static void index(File f, IndexEnvironment env, IndriFileProcessor indriFileProcessor)
      throws Exception {
    final Iterator<String> docsForFile = indriFileProcessor.documentsForFile(f);
    while (docsForFile.hasNext()) {
      final String trecTextDoc = docsForFile.next();
      env.addString(trecTextDoc + "\n", "trectext", NO_METADATA);
    }
  }

  // callback function passed into Indri native code to monitor
  // progress
  private static final class StatusMonitor extends IndexStatus {

    @Override
    public void status(int code, String documentFile, String error,
        int documentsIndexed, int documentsSeen) {
      if (code == action_code.FileOpen.swigValue()) {
        log.info("Documents: " + documentsIndexed + "\n" +
            "Opened " + documentFile);
      } else if (code == action_code.FileSkip.swigValue()) {
        log.info("Skipped " + documentFile);
      } else if (code == action_code.FileError.swigValue()) {
        log.error("Error in " + documentFile + " : " + error);
      } else if (code == action_code.DocumentCount.swigValue()) {
        if ((documentsIndexed % 500) == 0) {
          log.info("Documents: " + documentsIndexed);
        }
      }
    }
  }

  /**
   * ************** Configuration methods **************************
   */
  private static final long ONE_MEGABYTE = 1048576;

  // note we must pass in the status monitor because we need to ensure the
  // monitor outlasts the IndexEnvironment. If the JVM garbage collects it
  // because its only reference is in native code, then we will get a crash.
  private static IndexEnvironment setupIndexer(File outputDirectory, int memory,
      boolean storeDocs) throws Exception {
    final IndexEnvironment env = new IndexEnvironment();
    env.setMemory(memory * ONE_MEGABYTE);
    final Specification spec = env.getFileClassSpec("trectext");
    env.addFileClass(spec);
    env.setStoreDocs(storeDocs);
    // if we don't build indices on DOCNO then getting the docIds at query time is
    // extremely slow
    env.setMetadataIndexedFields(new String[]{"docno"}, new String[]{"docno"});
    env.create(outputDirectory.getAbsolutePath(), statusMonitor);
    return env;
  }

  private static final String RESTRICT_TO_EXTENSIONS = "restrictToExtensions";
  public static final Function<File, String>
      FILE_TO_EXTENSION_FUNCTION =
      new Function<File, String>() {
        @Override
        public String apply(File input) {
          return Files.getFileExtension(input.getAbsolutePath());
        }
      };

  private static Predicate<File> getFileFilter(Parameters params) {
    if (params.isPresent(RESTRICT_TO_EXTENSIONS)) {
      final Set<String> validExtensions = params.getStringSet(RESTRICT_TO_EXTENSIONS);
      log.info("Restricting to these extensions: {}", validExtensions);
      return compose(in(validExtensions), FILE_TO_EXTENSION_FUNCTION);
    } else {
      return Predicates.alwaysTrue();
    }
  }
}
