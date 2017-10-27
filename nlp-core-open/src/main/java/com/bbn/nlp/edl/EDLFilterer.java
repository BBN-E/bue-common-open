package com.bbn.nlp.edl;

import com.bbn.bue.common.files.FileUtils;
import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static com.bbn.nlp.edl.EDLMentionFunctions.documentID;
import static com.google.common.base.Predicates.compose;
import static com.google.common.base.Predicates.in;

/**
 * Filters EDL files to contain only responses from the given documents
 *
 * @author Ryan Gabbard
 */
public final class EDLFilterer {

  private static final Logger log = LoggerFactory.getLogger(EDLFilterer.class);

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
    final File inputEDLFile = params.getExistingFile("inputEDLFile");
    log.info("Loading EDL mentions from {}", inputEDLFile);
    final ImmutableList<EDLMention> inputEDL = EDLLoader.create().loadEDLMentionsFrom(
        Files.asCharSource(inputEDLFile, Charsets.UTF_8));
    final File outputEDLFile = params.getCreatableFile("outputEDLFile");
    final File docsToKeepFile = params.getExistingFile("docIDsToKeep");
    final ImmutableSet<Symbol> docIDsToKeep = FileUtils.loadSymbolSet(
        Files.asCharSource(docsToKeepFile, Charsets.UTF_8));
    log.info("Loaded {} doc IDs to keep from {}", docIDsToKeep.size(), docIDsToKeep.size());

    final Predicate<EDLMention> shouldKeep = compose(in(docIDsToKeep), documentID());
    final ImmutableSet<EDLMention> edlMentionsToKeep = FluentIterable.from(inputEDL)
        .filter(shouldKeep)
        .toSet();
    log.info("Filtered {} edl mentions down to {}; writing to {}",
        inputEDL.size(), edlMentionsToKeep.size(), outputEDLFile);
    EDLWriter.create().writeEDLMentions(edlMentionsToKeep,
        Files.asCharSink(outputEDLFile, Charsets.UTF_8));
  }
}
