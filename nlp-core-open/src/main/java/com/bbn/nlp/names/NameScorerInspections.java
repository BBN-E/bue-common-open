package com.bbn.nlp.names;

import com.bbn.bue.common.evaluation.AggregateBinaryFScoresInspector;
import com.bbn.bue.common.evaluation.BinaryFScoreBootstrapStrategy;
import com.bbn.bue.common.evaluation.BootstrapInspector;
import com.bbn.bue.common.evaluation.EquivalenceBasedProvenancedAligner;
import com.bbn.bue.common.evaluation.EvalPair;
import com.bbn.bue.common.evaluation.HasScoringType;
import com.bbn.bue.common.evaluation.InspectorTreeDSL;
import com.bbn.bue.common.evaluation.InspectorTreeNode;
import com.bbn.bue.common.evaluation.ProvenancedAlignment;
import com.bbn.bue.common.evaluation.ScoringTypedOffsetRange;
import com.bbn.bue.common.evaluation.TypeConfusionInspector;
import com.bbn.bue.common.strings.offsets.CharOffset;
import com.bbn.bue.common.strings.offsets.Offset;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.sun.istack.internal.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Random;
import java.util.Set;

import static com.bbn.bue.common.evaluation.InspectorTreeDSL.inspect;
import static com.bbn.bue.common.evaluation.InspectorTreeDSL.transformBothSets;
import static com.bbn.bue.common.evaluation.InspectorTreeDSL.transformed;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.compose;
import static com.google.common.base.Predicates.in;

/**
 * The guts of our name scorer. Applied to an {@link InspectorTreeNode} of paired key and system
 * {@link Set}s of {@link ScoringTypedOffsetRange}s and an {@code outputDir}, it will produce exact
 * match and partial credit scores with types both neutralized, un-neutralized, and merging {@code
 * ORG} and {@code LOC}.
 *
 * This is class is used to build name scorers with different front-ends. We have an internal one
 * which loads from SerifXML and an open-source one which loads from EDL.
 *
 * @author Constantine Lignos, Ryan Gabbard
 */
public final class NameScorerInspections {

  private static final Logger log = LoggerFactory.getLogger(NameScorerInspections.class);
  private final Predicate<ScoringTypedOffsetRange<CharOffset>> scoringFilter;


  private NameScorerInspections(
      final Predicate<ScoringTypedOffsetRange<CharOffset>> scoringFilter) {

    this.scoringFilter = checkNotNull(scoringFilter);
  }

  public static NameScorerInspections createWithScoringTypes(Iterable<Symbol> scoringTypes) {
    return new NameScorerInspections(compose(in(ImmutableSet.copyOf(scoringTypes)),
        ScoringTypedOffsetRange.<CharOffset>typeFunction()));
  }


  public void applyNameScoringTree(
      final InspectorTreeNode<EvalPair<Set<ScoringTypedOffsetRange<CharOffset>>, Set<ScoringTypedOffsetRange<CharOffset>>>> inputNode,
      final File outputDir) {
    final File exactMatchOutputDir = new File(outputDir, "exactMatch");
    exactMatchOutputDir.mkdir();
    final File naivePartialCreditOutputDir = new File(outputDir, "naivePartialCredit");
    naivePartialCreditOutputDir.mkdir();

    // filter out things we aren't scoring
    final InspectorTreeNode<EvalPair<Set<ScoringTypedOffsetRange<CharOffset>>, Set<ScoringTypedOffsetRange<CharOffset>>>>
        filteredInputNode = InspectorTreeDSL.filterBothSets(inputNode, scoringFilter);

    // Strict typing
    scoreExactMatch(exactMatchOutputDir, "Strict", alignNamesByIdentity(filteredInputNode));
    scorePartialCredit(naivePartialCreditOutputDir, "Strict", filteredInputNode);

    // No typing
    final Function<ScoringTypedOffsetRange<CharOffset>, ScoringTypedOffsetRange<CharOffset>>
        untypedFunc =
        NameScorerInspections.applyScoringModeFunction(NameTypeMode.UNTYPED);
    final InspectorTreeNode<EvalPair<Set<ScoringTypedOffsetRange<CharOffset>>, Set<ScoringTypedOffsetRange<CharOffset>>>>
        extractedTypedNamesUntyped =
        InspectorTreeDSL.transformBothSets(filteredInputNode, untypedFunc);
    scoreExactMatch(exactMatchOutputDir, "Untyped",
        alignNamesByIdentity(extractedTypedNamesUntyped));
    scorePartialCredit(naivePartialCreditOutputDir, "Untyped", extractedTypedNamesUntyped);

    // LOC-ORG merged
    final Function<ScoringTypedOffsetRange<CharOffset>, ScoringTypedOffsetRange<CharOffset>>
        mergeLOCORGFunc =
        NameScorerInspections.applyScoringModeFunction(NameTypeMode.MERGE_LOC_ORG);
    final InspectorTreeNode<EvalPair<Set<ScoringTypedOffsetRange<CharOffset>>, Set<ScoringTypedOffsetRange<CharOffset>>>>
        extractedTypedNamesLocOrgMerged = transformBothSets(filteredInputNode, mergeLOCORGFunc);
    scoreExactMatch(exactMatchOutputDir, "LocOrgMerged",
        alignNamesByIdentity(extractedTypedNamesLocOrgMerged));
    scorePartialCredit(naivePartialCreditOutputDir, "LocOrgMerged",
        extractedTypedNamesLocOrgMerged);
  }

  private static InspectorTreeNode<ProvenancedAlignment<ScoringTypedOffsetRange<CharOffset>, ScoringTypedOffsetRange<CharOffset>, ScoringTypedOffsetRange<CharOffset>, ScoringTypedOffsetRange<CharOffset>>> alignNamesByIdentity(
      final InspectorTreeNode<EvalPair<Set<ScoringTypedOffsetRange<CharOffset>>, Set<ScoringTypedOffsetRange<CharOffset>>>> extractedTypedNames) {
    return transformed(extractedTypedNames,
        EquivalenceBasedProvenancedAligner
            .forEquivalenceFunction(Functions.<ScoringTypedOffsetRange<CharOffset>>identity())
            .asFunction());
  }

  @SuppressWarnings("unchecked") // for BootstrapInspector, which is safe
  private static void scoreExactMatch(final File outputDir, final String prefix,
      final InspectorTreeNode<ProvenancedAlignment<ScoringTypedOffsetRange<CharOffset>, ScoringTypedOffsetRange<CharOffset>,
          ScoringTypedOffsetRange<CharOffset>, ScoringTypedOffsetRange<CharOffset>>>
          alignedNames) {
    // Score strictly typed
    // Aggregate F-score
    inspect(alignedNames).with(
        AggregateBinaryFScoresInspector.createOutputtingTo(
            Files.asCharSink(new File(outputDir, prefix + "AggregateF.txt"), Charsets.UTF_8)));
    // Confusion matrix
    inspect(alignedNames).with(
        TypeConfusionInspector.createOutputtingTo(
            prefix,
            TypeFunction.INSTANCE,
            ScoringTypedOffsetRange.docIdOffsetEquivalence(),
            outputDir));
    // Bootstrapped aggregate
    final BootstrapInspector.BootstrapStrategy bootStrappedAggregateScore =
        BinaryFScoreBootstrapStrategy.create(prefix + "Aggregate", outputDir);
    inspect(alignedNames).with(
        BootstrapInspector.forStrategy(
            bootStrappedAggregateScore, 1000, new Random(0)));
    // Bootstrapped by type
    final BootstrapInspector.BootstrapStrategy bootStrappedScoreByType =
        BinaryFScoreBootstrapStrategy.createBrokenDownBy(prefix + "Type",
            TypeFunction.INSTANCE,
            outputDir);
    inspect(alignedNames).with(
        BootstrapInspector.forStrategy(bootStrappedScoreByType, 1000, new Random(0)));
  }

  private static void scorePartialCredit(final File outputDir, final String prefix,
      final InspectorTreeNode<EvalPair<Set<ScoringTypedOffsetRange<CharOffset>>, Set<ScoringTypedOffsetRange<CharOffset>>>> pairedNames) {
    // Bootstrapped aggregate
    final BootstrapInspector.BootstrapStrategy bootStrappedAggregateScore =
        NaivePartialCreditBootstrapStrategy.create(prefix + "Aggregate", outputDir);
    inspect(pairedNames).with(
        BootstrapInspector.forStrategy(
            bootStrappedAggregateScore, 1000, new Random(0)));
    // Bootstrapped by type
    final BootstrapInspector.BootstrapStrategy bootStrappedScoreByType =
        NaivePartialCreditBootstrapStrategy.createBrokenDownBy(prefix + "Type",
            TypeFunction.INSTANCE,
            outputDir);
    inspect(pairedNames).with(
        BootstrapInspector.forStrategy(bootStrappedScoreByType, 1000, new Random(0)));
  }


  private enum TypeFunction implements Function<HasScoringType, String> {
    INSTANCE;

    @Override
    public String apply(@Nullable final HasScoringType input) {
      checkNotNull(input);
      return input.scoringType().toString();
    }
  }

  private static <T extends Offset<T>> Function<ScoringTypedOffsetRange<T>, ScoringTypedOffsetRange<T>> applyScoringModeFunction(
      final NameTypeMode mode) {
    return new Function<ScoringTypedOffsetRange<T>, ScoringTypedOffsetRange<T>>() {
      @Nullable
      @Override
      public ScoringTypedOffsetRange<T> apply(@Nullable final ScoringTypedOffsetRange<T> input) {
        checkNotNull(input);
        return ScoringTypedOffsetRange
            .create(input.docID(), mode.transformNameType(input.scoringType()),
                input.offsetRange());
      }
    };
  }

  private enum NameTypeMode {
    STRICT,
    UNTYPED,
    MERGE_LOC_ORG;

    private static final Symbol NAME_TYPE_LOC = Symbol.from("LOC");
    private static final Symbol NAME_TYPE_ORG = Symbol.from("ORG");
    private static final Symbol NAME_TYPE_ANY = Symbol.from("ANY");
    private static final Symbol NAME_TYPE_LOC_ORG = Symbol.from("LOC-ORG");

    private Symbol transformNameType(final Symbol nameType) {
      switch (this) {
        case STRICT: {
          // Return as-is
          return nameType;
        }
        case UNTYPED: {
          // Change all types to the same type
          return NAME_TYPE_ANY;
        }
        case MERGE_LOC_ORG: {
          // Conflate LOC and ORG
          if (nameType.equals(NAME_TYPE_LOC) || nameType.equals(NAME_TYPE_ORG)) {
            return NAME_TYPE_LOC_ORG;
          } else {
            return nameType;
          }
        }
        default: {
          throw new IllegalArgumentException("Unhandled enum value: " + this);
        }
      }
    }
  }
}
