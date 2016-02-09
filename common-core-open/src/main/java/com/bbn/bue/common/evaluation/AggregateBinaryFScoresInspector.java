package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.Inspector;
import com.bbn.bue.common.StringUtils;
import com.bbn.bue.common.symbols.Symbol;
import com.google.common.annotations.Beta;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSink;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Observes an {@link Alignment} between system items on the left and gold standard items on the
 * right. A system/gold item is assumed to be found if aligned.  Writes summary F-measure scores
 * and an absent/present confusion matrix based on its observations.
 *
 * Although this only implements {@code Inspector<Alignment<Object,Object>>}, it is safe to cast it
 * to inspect any alignment.
 */
@Beta
public final class AggregateBinaryFScoresInspector<KeyT, TestT>
    implements Inspector<Alignment<? extends KeyT, ? extends TestT>> {

  private static final Symbol PRESENT = Symbol.from("Present");
  private static final Symbol ABSENT = Symbol.from("Absent");
  private static final String FILE_SUFFIX = "F.txt";

  private final SummaryConfusionMatrices.Builder summaryConfusionMatrixB =
      SummaryConfusionMatrices.builder();
  private final String outputName;
  private final File outputDir;
  private final ImmutableList<ScoringEventObserver<? super KeyT, ? super TestT>> scoringEventObservers;

  private AggregateBinaryFScoresInspector(final String outputName, final File outputDir,
      final Iterable<? extends ScoringEventObserver<? super KeyT, ? super TestT>> scoringEventObservers) {
    this.outputName = checkNotNull(outputName);
    this.outputDir = checkNotNull(outputDir);
    this.scoringEventObservers = ImmutableList.copyOf(scoringEventObservers);
  }

  public static <KeyT, TestT> AggregateBinaryFScoresInspector<KeyT, TestT> createOutputtingTo(final String outputName,
      final File outputDir) {
    return new AggregateBinaryFScoresInspector<>(outputName, outputDir,
        ImmutableList.<ScoringEventObserver<KeyT, TestT>>of());
  }

  public static <KeyT, TestT> AggregateBinaryFScoresInspector<KeyT, TestT> createWithScoringObservers(
      final String outputName, final File outputDir,
      final Iterable<? extends ScoringEventObserver<? super KeyT, ? super TestT>> scoringObservers) {
    return new AggregateBinaryFScoresInspector<>(outputName, outputDir, scoringObservers);
  }

  @Override
  public void finish() throws IOException {
    final CharSink outSink = Files.asCharSink(new File(outputDir, outputName + FILE_SUFFIX), Charsets.UTF_8);
    final SummaryConfusionMatrix summaryConfusionMatrix = summaryConfusionMatrixB.build();
    outSink.write(StringUtils.NewlineJoiner.join(
        SummaryConfusionMatrices.prettyPrint(summaryConfusionMatrix),
        SummaryConfusionMatrices.FMeasureVsAllOthers(summaryConfusionMatrix, PRESENT).compactPrettyString()));

    // Call finish on the observers
    for (final ScoringEventObserver observer : scoringEventObservers) {
      observer.finish(outputDir);
    }
  }

  @Override
  public void inspect(final Alignment<? extends KeyT, ? extends TestT> alignment) {
    // A new scope is created for each computation to guarantee that nothing is reused across scopes inappropriately,
    // for example the false positives condition accidentally iterating over the true positives.

    { // True positives
      final Set<? extends TestT> truePositives = alignment.rightAligned();
      summaryConfusionMatrixB.accumulatePredictedGold(PRESENT, PRESENT, truePositives.size());
      for (final TestT predicted : truePositives) {
        // We take the first aligned item as the alignment. Since this predicted item is aligned, we are guaranteed that
        // an aligned item exists.
        final KeyT gold = alignment.alignedToRightItem(predicted).iterator().next();
        for (final ScoringEventObserver<? super KeyT, ? super TestT> observer : scoringEventObservers) {
          observer.observeTruePositive(gold, predicted, 1.0);
        }
      }
    }

    { // False positives
      final Set<? extends TestT> falsePositives = alignment.rightUnaligned();
      summaryConfusionMatrixB.accumulatePredictedGold(PRESENT, ABSENT, falsePositives.size());
      for (final TestT predicted : falsePositives) {
        for (final ScoringEventObserver<? super KeyT, ? super TestT> observer : scoringEventObservers) {
          observer.observeFalsePositive(predicted, 1.0);
        }
      }
    }

    { // False negatives
      final Set<? extends KeyT> falseNegatives = alignment.leftUnaligned();
      summaryConfusionMatrixB.accumulatePredictedGold(ABSENT, PRESENT, falseNegatives.size());
      for (final KeyT gold : falseNegatives) {
        for (final ScoringEventObserver<? super KeyT, ? super TestT> observer : scoringEventObservers) {
          observer.observeFalseNegative(gold, 1.0);
        }
      }
    }
  }
}
