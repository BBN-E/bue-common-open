package com.bbn.bue.gnuplot;

import com.bbn.bue.common.evaluation.FMeasureCounts;
import com.bbn.bue.common.scoring.Scored;
import com.bbn.bue.common.scoring.Scoreds;

import com.google.common.annotations.Beta;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Range.atLeast;

@Beta
public class PRCurvePlotBuilder {

  private PRCurvePlotBuilder(final String title, final double thresholdStartInclusive,
      final double thresholdEndExclusive, final double increment) {
    builder.setTitle(checkNotNull(title));
    this.start = thresholdStartInclusive;
    this.end = thresholdEndExclusive;
    this.increment = increment;
    checkArgument(this.end > this.start);
    checkArgument(increment > 0.0);
  }

  public static PRCurvePlotBuilder create(final String title,
      final double thresholdStartInclusive,
      final double thresholdEndExclusive, final double increment) {
    return new PRCurvePlotBuilder(title, thresholdStartInclusive,
        thresholdEndExclusive, increment);
  }

  public <T> void observeLine(final String lineName, final List<Scored<T>> scoredItems,
      final Set<T> allTruePositives) {
    checkNotNull(lineName);
    final ImmutableList.Builder<Point2D> points = ImmutableList.builder();

    for (double threshold = start; threshold < end; threshold += increment) {
      final List<T> passingItems = FluentIterable.from(scoredItems)
          .filter(Scoreds.<T>scoreIs(atLeast(threshold)))
          .transform(Scoreds.<T>itemsOnly()).toList();

      final FMeasureCounts eval = FMeasureCounts.fromHashableItems(
          ImmutableSet.copyOf(passingItems), allTruePositives);

      points.add(Point2D.fromXY(eval.recall(), eval.precision()));
    }

    builder.addLine(LineData.from(lineName, points.build()));
  }

  public LinePlot build() {
    if (!built) {
      built = true;
      return builder.build();
    } else {
      throw new RuntimeException("Cannot build twice");
    }
  }

  private final double start;
  private final double end;
  private final double increment;
  private final LinePlot.Builder builder = LinePlot.builder()
      .setXLabel("Recall")
      .setYLabel("Precision")
      .setXRange(Range.closed(0.0, 1.0))
      .setYRange(Range.closed(0.0, 1.0))
      .setPointSize(0.5);
  boolean built = false;

}
