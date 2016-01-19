package com.bbn.nlp.names;

import com.bbn.bue.common.evaluation.EvalPair;
import com.bbn.bue.common.evaluation.InspectionNode;
import com.bbn.bue.common.evaluation.ScoringTypedOffsetRange;
import com.bbn.bue.common.files.FileUtils;
import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.strings.offsets.CharOffset;
import com.bbn.bue.common.symbols.Symbol;
import com.bbn.bue.common.symbols.SymbolUtils;
import com.bbn.nlp.edl.EDLLoader;
import com.bbn.nlp.edl.EDLMention;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static com.bbn.bue.common.evaluation.InspectorTreeDSL.pairedInput;

/**
 * Scores named entity recognition over files in the format used by the TAC KBP Entity Detection and
 * Linking evaluation.
 *
 * See the USAGE_MESSAGE constant for parameters.
 */
public final class NameScorer {

  private static final Logger log = LoggerFactory.getLogger(NameScorer.class);

  private static final String USAGE_MESSAGE =
      "Scores named entity recognition over files in the format used by the TAC KBP Entity\n"
          + "\t\t\tDetection and Linking evaluation.\n"
          + "\n"
          + "Parameters:\n"
          + "\tcom.bbn.nlp.names.scorer.keyEDLFile: An EDL-format file containing the gold standard\n"
          + "\t\t\tnames.\n"
          + "\tcom.bbn.nlp.names.scorer.systemEDLFile: An EDL-format file containing the system names.\n"
          + "\tcom.bbn.nlp.names.scorer.types: A comma-separated list of named entity types to score.\n"
          + "\t\t\tAll others will be ignored.\n"
          + "\tcom.bbn.nlp.names.scorer.outputDir: directory to write output to. Will be created if\n"
          + "\t\t\tit doesn't exist.\n"
          + "\tcom.bbn.nlp.names.scorer.docIDsToScore: a file listing the document IDs to be scored,\n"
          + "\t\t\tone per line. All other documents will be ignored. This parameter is optional; if\n"
          + "\t\t\tit is missing, all documents found in the key file will be scored.\n";

  public static void usage() {
    System.err.println(USAGE_MESSAGE);
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
    if (argv.length == 0) {
      usage();
      System.exit(1);
    }
    final Parameters params = Parameters.loadSerifStyle(new File(argv[0]));
    final ImmutableListMultimap<Symbol, EDLMention> keyMentionsByDoc =
        loadEDLMentionsByDocs(params.getExistingFile("com.bbn.nlp.names.scorer.keyEDLFile"));
    final ImmutableListMultimap<Symbol, EDLMention> systemMentionsByDoc =
        loadEDLMentionsByDocs(params.getExistingFile("com.bbn.nlp.names.scorer.systemEDLFile"));
    final Set<Symbol> scoringTypes = params.getSymbolSet("com.bbn.nlp.names.scorer.types");

    final File scorerOutputDir = params.getCreatableDirectory("com.bbn.nlp.names.scorer.outputDir");
    final ImmutableSet<Symbol> docIDsToScore = determineDocIDsToScore(keyMentionsByDoc,
        params.getOptionalExistingFile("com.bbn.nlp.names.scorer.docIDsToScore"));

    log.info("Scoring over {} documents", docIDsToScore.size());

    // set up the scoring process. Anything fed through input will go through this processing
    final TypeToken<Set<ScoringTypedOffsetRange<CharOffset>>> inputInputIsOffsetRangeSet =
        new TypeToken<Set<ScoringTypedOffsetRange<CharOffset>>>() {
        };
    final InspectionNode<EvalPair<Set<ScoringTypedOffsetRange<CharOffset>>, Set<ScoringTypedOffsetRange<CharOffset>>>>
        input = pairedInput(inputInputIsOffsetRangeSet);
    NameScorerInspections.createWithScoringTypes(scoringTypes)
        .applyNameScoringTree(input, scorerOutputDir);

    for (final Symbol docID : docIDsToScore) {
      final Set<ScoringTypedOffsetRange<CharOffset>> keyOffsetRanges =
          toTypedOffsetRanges(keyMentionsByDoc.get(docID));
      final Set<ScoringTypedOffsetRange<CharOffset>> systemOffsetRanges =
          toTypedOffsetRanges(systemMentionsByDoc.get(docID));
      input.inspect(EvalPair.of(keyOffsetRanges, systemOffsetRanges));
    }

    input.finish();

    log.info("Scoring output written to {}", scorerOutputDir);
  }

  private static final Symbol NAME_MENTION_TYPE = Symbol.from("NAM");

  private static ImmutableSet<ScoringTypedOffsetRange<CharOffset>> toTypedOffsetRanges(
      final Iterable<EDLMention> edlMentions) {
    final ImmutableSet.Builder<ScoringTypedOffsetRange<CharOffset>> ret = ImmutableSet.builder();
    for (final EDLMention edlMention : edlMentions) {
      // non-name mentions may be present and should be skipped
      if (NAME_MENTION_TYPE.equalTo(edlMention.mentionType())) {
        ret.add(ScoringTypedOffsetRange.create(edlMention.documentID(), edlMention.entityType(),
            edlMention.headOffsets()));
      }
    }
    return ret.build();
  }

  private static ImmutableSet<Symbol> determineDocIDsToScore(
      final ImmutableListMultimap<Symbol, EDLMention> keyMentionsByDoc,
      final Optional<File> docIDsToScoreFile) throws IOException {
    final ImmutableSet<Symbol> docIDsToScore;
    if (docIDsToScoreFile.isPresent()) {
      docIDsToScore =
          FileUtils.loadSymbolSet(Files.asCharSource(docIDsToScoreFile.get(), Charsets.UTF_8));
      if (keyMentionsByDoc.keySet().containsAll(docIDsToScore)) {
        throw new RuntimeException("The key lacks the following documents you requested to score: "
            + Sets.difference(keyMentionsByDoc.keySet(), docIDsToScore));
      }
    } else {
      // default to scoring over all documents in the key
      docIDsToScore = keyMentionsByDoc.keySet();
    }
    return docIDsToScore;
  }

  private static ImmutableListMultimap<Symbol, EDLMention> loadEDLMentionsByDocs(final File f)
      throws IOException {
    final EDLLoader edlLoader = EDLLoader.create();
    final ImmutableList<EDLMention> edlMentions =
        edlLoader.loadEDLMentionsFrom(Files.asCharSource(f, Charsets.UTF_8));
    final ImmutableListMultimap.Builder<Symbol, EDLMention> byDocs =
        ImmutableListMultimap.<Symbol, EDLMention>builder()
            .orderKeysBy(SymbolUtils.byStringOrdering());
    for (final EDLMention edlMention : edlMentions) {
      byDocs.put(edlMention.documentID(), edlMention);
    }
    return byDocs.build();
  }

}
