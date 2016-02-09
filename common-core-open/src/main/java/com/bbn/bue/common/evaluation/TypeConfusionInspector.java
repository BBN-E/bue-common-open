package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.Inspector;
import com.bbn.bue.common.symbols.Symbol;
import com.bbn.bue.common.symbols.SymbolUtils;
import com.google.common.annotations.Beta;
import com.google.common.base.Charsets;
import com.google.common.base.Equivalence;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;

/**
 * Observes an {@link Alignment} between system items on the left and gold standard items on the
 * right and creates a confusion matrix between them based on a specified confusion equivalence
 * and labelling.
 *
 * To create the confusion matrix, the caller must specify:
 * <ol>
 *   <li>A confusion labeler, which is a {@link Function} mapping each observation to its row/column
 *   in the confusion matrix. For example, in part of speech tagging, it would return the part of
 *   speech tag itself.</li>
 *   <li>A confusion equivalence, which is an {@link Equivalence} that declares two observations to
 *   be equal if they are equal in every relevant way except for the value that would be returned by
 *   the confusion labeler. For example, in part of speech tagging, that equivalence should return
 *   true if two observations refer to the same token in the same document, regardless of whether
 *   the tags assigned to that token match.</li>
 *   </ol>
 *
 * @param <LeftRightT> the type of both left and right items in the alignment
 */
@Beta
public final class TypeConfusionInspector<LeftRightT>
    implements Inspector<Alignment<? extends LeftRightT, ? extends LeftRightT>> {

  private static final Logger log = LoggerFactory.getLogger(TypeConfusionInspector.class);
  private static final Symbol NONE = Symbol.from("NONE");

  private final SummaryConfusionMatrices.Builder summaryConfusionMatrixB =
      SummaryConfusionMatrices.builder();
  private final Function<? super LeftRightT, String> confusionLabeler;
  private final Equivalence<LeftRightT> confusionEquivalence;
  private final File outputDir;
  private final String name;

  private TypeConfusionInspector(
      final String name,
      final Function<? super LeftRightT, String> confusionLabeler,
      final Equivalence<LeftRightT> confusionEquivalence,
      final File outputDir) {
    this.confusionLabeler = checkNotNull(confusionLabeler);
    this.confusionEquivalence = checkNotNull(confusionEquivalence);
    this.outputDir = checkNotNull(outputDir);
    this.name = checkNotNull(name);
  }

  /**
   * Creates a new inspector. See class documentation for an explanation of the confusion labeler
   * and confusion equivalence.
   *
   * @param name a name to be used as a prefix for file output
   * @param confusionLabeler a function for mapping an observation to its label in the confusion matrix
   * @param confusionEquivalence an equivalence between observations that are considered confusable
   * @param outputDir directory for writing output
   * @param <LeftRightT> the type of both left and right items in the alignment
   * @return a new inspector
   */
  public static <LeftRightT> TypeConfusionInspector<LeftRightT> createOutputtingTo(
      final String name,
      final Function<? super LeftRightT, String> confusionLabeler,
      final Equivalence<LeftRightT> confusionEquivalence,
      final File outputDir) {
    return new TypeConfusionInspector<LeftRightT>(name, confusionLabeler, confusionEquivalence, outputDir);
  }

  @Override
  public void finish() throws IOException {
    final SummaryConfusionMatrix summaryConfusionMatrix = summaryConfusionMatrixB.build();
    // Create an ordering that puts none last
    final Set<Symbol> notNoneSymbols = Sets.filter(
        Sets.union(summaryConfusionMatrix.leftLabels(), summaryConfusionMatrix.rightLabels()),
        not(equalTo(NONE)));
    final ImmutableList.Builder<Symbol> symbolOrder = ImmutableList.builder();
    symbolOrder.addAll(SymbolUtils.byStringOrdering().sortedCopy(notNoneSymbols));
    symbolOrder.add(NONE);
    final Ordering<Symbol> ordering = Ordering.explicit(symbolOrder.build());

    Files.asCharSink(new File(outputDir, name + "TypeConfusion.txt"),
        Charsets.UTF_8).write(SummaryConfusionMatrices.prettyPrint(summaryConfusionMatrix,
        ordering));
    Files.asCharSink(new File(outputDir, name + "TypeConfusion.csv"),
        Charsets.UTF_8).write(SummaryConfusionMatrices.prettyDelimPrint(summaryConfusionMatrix,
        ",", ordering));
  }

  @Override
  public void inspect(final Alignment<? extends LeftRightT, ? extends LeftRightT> alignment) {
    // Count each aligned item as the diagonal
    for (final LeftRightT correctItem : alignment.rightAligned()) {
      final Symbol label = Symbol.from(confusionLabeler.apply(correctItem));
      summaryConfusionMatrixB.accumulatePredictedGold(label, label, 1);
    }

    // Extract unaligned items and their backoff
    final Set<? extends LeftRightT> goldUnaligned = alignment.leftUnaligned();
    final Set<? extends LeftRightT> predUnaligned = alignment.rightUnaligned();

    // Set up equivalence wrapper
    final Function<LeftRightT, Equivalence.Wrapper<LeftRightT>> wrapEquivalenceFunction =
        new Function<LeftRightT, Equivalence.Wrapper<LeftRightT>>() {
          @Override
          public Equivalence.Wrapper<LeftRightT> apply(LeftRightT input) {
            return confusionEquivalence.wrap(input);
          }
        };

    // Create map between the equivalence-wrapped observations and the originals
    final ImmutableMap<Equivalence.Wrapper<LeftRightT>, ? extends LeftRightT> predEquiv =
        makeEquivalenceWrapperMap(predUnaligned, wrapEquivalenceFunction);
    final ImmutableMap<Equivalence.Wrapper<LeftRightT>, ? extends LeftRightT> goldEquiv =
        makeEquivalenceWrapperMap(goldUnaligned, wrapEquivalenceFunction);

    // Log an warning if the size of the unaligned observations has changed. This shouldn't be fatal
    // because annotation of the same offsets with multiple types often causes issues of this type.
    if (goldEquiv.size() != goldUnaligned.size() || predEquiv.size() != predUnaligned.size()) {
        log.warn("Confusion equivalence maps multiple observations to the same value");
    }

    // Create confusion entries for unaligned gold
    for (final LeftRightT goldItem : goldUnaligned) {
      final Symbol goldLabel = Symbol.from(confusionLabeler.apply(goldItem));
      final Equivalence.Wrapper<LeftRightT> wrappedGoldItem = confusionEquivalence.wrap(goldItem);
      final Symbol predLabel = getConfusedLabel(wrappedGoldItem, predEquiv);
      summaryConfusionMatrixB.accumulatePredictedGold(predLabel, goldLabel, 1);
    }

    // Create confusion entries for unaligned predicted
    for (final LeftRightT predItem : predUnaligned) {
      final Symbol predLabel = Symbol.from(confusionLabeler.apply(predItem));
      final Equivalence.Wrapper<LeftRightT> wrappedPredItem = confusionEquivalence.wrap(predItem);
      final Symbol goldLabel = getConfusedLabel(wrappedPredItem, goldEquiv);
      summaryConfusionMatrixB.accumulatePredictedGold(predLabel, goldLabel, 1);
    }
  }

  private Symbol getConfusedLabel(final Equivalence.Wrapper<LeftRightT> wrappedObservation,
      final ImmutableMap<Equivalence.Wrapper<LeftRightT>, ? extends LeftRightT> confusableObservations) {
    if (confusableObservations.containsKey(wrappedObservation)) {
      // Returned the label of the confusable instance if there's a match
      final LeftRightT match = confusableObservations.get(wrappedObservation);
      return Symbol.from(confusionLabeler.apply(match));
    } else {
      // Otherwise just map to NONE
      return NONE;
    }
  }

  private ImmutableMap<Equivalence.Wrapper<LeftRightT>, LeftRightT> makeEquivalenceWrapperMap(
      final Iterable<? extends LeftRightT> items,
      final Function<LeftRightT, Equivalence.Wrapper<LeftRightT>> wrapperFunction) {
    // This would normally just be Maps.uniqueIndex,but we need defensive behavior for key collisions
    final Map<Equivalence.Wrapper<LeftRightT>, LeftRightT> equivalenceMap = Maps.newHashMap();
    for (final LeftRightT item : items) {
      final Equivalence.Wrapper<LeftRightT> wrapped = wrapperFunction.apply(item);
      if (equivalenceMap.containsKey(wrapped)) {
        log.warn("Multiple values with same key: '{}'(new) and '{}'(existing). Skipping new value.", item,
            equivalenceMap.get(wrapped));
      } else {
        equivalenceMap.put(wrapped, item);
      }
    }
    return ImmutableMap.copyOf(equivalenceMap);
  }
}
