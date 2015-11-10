package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.Inspector;
import com.bbn.bue.common.symbols.Symbol;
import com.google.common.annotations.Beta;
import com.google.common.base.Equivalence;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.CharSink;

import java.io.IOException;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

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

  private final SummaryConfusionMatrices.Builder summaryConfusionMatrixB =
      SummaryConfusionMatrices.builder();
  private final Function<? super LeftRightT, String> confusionLabeler;
  private final Equivalence<LeftRightT> confusionEquivalence;

  private final CharSink outSink;

  private static final Symbol NONE = Symbol.from("NONE");

  private TypeConfusionInspector(
      final Function<? super LeftRightT, String> confusionLabeler,
      final Equivalence<LeftRightT> confusionEquivalence,
      final CharSink outSink) {
    this.confusionLabeler = checkNotNull(confusionLabeler);
    this.confusionEquivalence = checkNotNull(confusionEquivalence);
    this.outSink = checkNotNull(outSink);
  }

  /**
   * Creates a new inspector. See class documentation for an explanation of the confusion labeler
   * and confusion equivalence.
   * 
   * @param confusionLabeler a function for mapping an observation to its label in the confusion matrix
   * @param confusionEquivalence an equivalence between observations that are considered confusable
   * @param outputSink sink for writing output
   * @param <LeftRightT> the type of both left and right items in the alignment
   * @return a new inspector
   */
  public static <LeftRightT> TypeConfusionInspector<LeftRightT> createOutputtingTo(
      final Function<? super LeftRightT, String> confusionLabeler,
      final Equivalence<LeftRightT> confusionEquivalence,
      final CharSink outputSink) {
    return new TypeConfusionInspector<LeftRightT>(confusionLabeler, confusionEquivalence, outputSink);
  }

  @Override
  public void finish() throws IOException {
    final SummaryConfusionMatrix summaryConfusionMatrix = summaryConfusionMatrixB.build();
    outSink.write(SummaryConfusionMatrices.prettyPrint(summaryConfusionMatrix));
  }

  @Override
  public void inspect(final Alignment<? extends LeftRightT, ? extends LeftRightT> alignment) {
    final int falseNegatives = alignment.leftUnaligned().size();
    final int falsePositives = alignment.rightUnaligned().size();

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
        Maps.uniqueIndex(predUnaligned, wrapEquivalenceFunction);
    final ImmutableMap<Equivalence.Wrapper<LeftRightT>, ? extends LeftRightT> goldEquiv =
        Maps.uniqueIndex(goldUnaligned, wrapEquivalenceFunction);

    // Raise an error if the size of the unaligned observations has changed
    checkState(goldEquiv.size() == goldUnaligned.size() && predEquiv.size() == predUnaligned.size(),
        "Confusion equivalence maps multiple observations to the same value");

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
}

