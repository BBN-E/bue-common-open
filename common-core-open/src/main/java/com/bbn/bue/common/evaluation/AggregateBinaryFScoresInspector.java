package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.Inspector;
import com.bbn.bue.common.StringUtils;
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
public final class AggregateBinaryFScoresInspector<KeyT, TestT>
    implements Inspector<Alignment<? extends KeyT, ? extends TestT>> {

  private final SummaryConfusionMatrices.Builder summaryConfusionMatrixB =
      SummaryConfusionMatrices.builder();
  private final CharSink outSink;
  private static final Symbol PRESENT = Symbol.from("Present");
  private static final Symbol ABSENT = Symbol.from("Absent");

  private AggregateBinaryFScoresInspector(final CharSink outSink) {
    this.outSink = checkNotNull(outSink);
  }

  public static <KeyT, TestT> AggregateBinaryFScoresInspector<KeyT, TestT> createOutputtingTo(
      CharSink outputSink) {
    return new AggregateBinaryFScoresInspector<KeyT, TestT>(outputSink);
  }

  @Override
  public void finish() throws IOException {
    final SummaryConfusionMatrix summaryConfusionMatrix = summaryConfusionMatrixB.build();
    outSink.write(StringUtils.NewlineJoiner.join(
        SummaryConfusionMatrices.prettyPrint(summaryConfusionMatrix),
        SummaryConfusionMatrices.FMeasureVsAllOthers(summaryConfusionMatrix, PRESENT).compactPrettyString()));
  }

  @Override
  public void inspect(final Alignment<? extends KeyT, ? extends TestT> alignment) {
    final int truePositives = alignment.rightAligned().size();
    final int falseNegatives = alignment.leftUnaligned().size();
    final int falsePositives = alignment.rightUnaligned().size();
    summaryConfusionMatrixB
        .accumulatePredictedGold(PRESENT, PRESENT, truePositives);
    summaryConfusionMatrixB
        .accumulatePredictedGold(ABSENT, PRESENT, falseNegatives);
    summaryConfusionMatrixB
        .accumulatePredictedGold(PRESENT, ABSENT, falsePositives);
  }
}
