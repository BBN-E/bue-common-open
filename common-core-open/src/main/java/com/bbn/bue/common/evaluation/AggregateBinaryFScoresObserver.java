package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.Inspector;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.annotations.Beta;
import com.google.common.io.CharSink;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Observes an {@link Alignment} between system items on the left and gold standard items on the
 * right. A system/gold item is assumed to be found if aligned.  Writes summary F-measure scores
 * based on its observations.
 *
 * At the moment this only writes confusion matrices but it will be extended to write more soon.
 *
 * Although this only implements {@code Inspector<Alignment<Object,Object>>}, it is safe to cast it
 * to inspect any alignment.
 */
@Beta
public final class AggregateBinaryFScoresObserver implements Inspector<Alignment<Object, Object>> {

  private final SummaryConfusionMatrices.Builder summaryConfusionMatrixB =
      SummaryConfusionMatrices.builder();
  private final CharSink outSink;
  private static final Symbol PRESENT = Symbol.from("Present");
  private static final Symbol ABSENT = Symbol.from("Absent");

  private AggregateBinaryFScoresObserver(final CharSink outSink) {
    this.outSink = checkNotNull(outSink);
  }

  public static AggregateBinaryFScoresObserver createOutputtingTo(CharSink outputSink) {
    return new AggregateBinaryFScoresObserver(outputSink);
  }

  @Override
  public void finish() throws IOException {
    final SummaryConfusionMatrix summaryConfusionMatrix = summaryConfusionMatrixB.build();
    outSink.write(SummaryConfusionMatrices.prettyPrint(summaryConfusionMatrix));
  }

  @Override
  public void inspect(final Alignment<Object, Object> alignment) {
    summaryConfusionMatrixB
        .accumulatePredictedGold(PRESENT, PRESENT, alignment.rightAligned().size());
    summaryConfusionMatrixB
        .accumulatePredictedGold(PRESENT, ABSENT, alignment.leftUnaligned().size());
    summaryConfusionMatrixB
        .accumulatePredictedGold(ABSENT, PRESENT, alignment.rightAligned().size());
  }
}
