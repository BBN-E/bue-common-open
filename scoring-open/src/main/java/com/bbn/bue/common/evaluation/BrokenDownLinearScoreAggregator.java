package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.TextGroupImmutable;
import com.bbn.bue.common.math.PercentileComputer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimaps;

import org.immutables.value.Value;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

@TextGroupImmutable
@Value.Immutable
public abstract class BrokenDownLinearScoreAggregator
    implements BootstrapInspector.SummaryAggregator<Map<String, FMeasureCounts>> {

  public abstract double alpha();
  public abstract String name();
  public abstract File outputDir();

  private final ImmutableListMultimap.Builder<String, Double> linearScoresB = ImmutableListMultimap.builder();

  private static final String LINEAR_SCORE = "LinearScore";

  private final BootstrapWriter writer = new BootstrapWriter.Builder()
      .measures(ImmutableList.of(LINEAR_SCORE))
      .percentilesToPrint(ImmutableList.of(0.005, 0.025, 0.05, 0.25, 0.5, 0.75, 0.95, 0.975, 0.995))
      .percentileComputer(PercentileComputer.nistPercentileComputer())
      .build();

  @Override
  public void observeSample(
      final Collection<Map<String, FMeasureCounts>> observationSummaries) {
    final ImmutableSetMultimap.Builder<String, FMeasureCounts> allCountsB =
        ImmutableSetMultimap.builder();

    for (final Map<String, FMeasureCounts> observationSummary : observationSummaries) {
      allCountsB.putAll(Multimaps.forMap(observationSummary));
    }

    for (final Map.Entry<String, Collection<FMeasureCounts>> breakdownKeySamples : allCountsB.build().asMap().entrySet()) {
      double scoreAggregator = 0.0;
      double normalizer = 0.0;

      for (final FMeasureCounts fMeasureCounts : breakdownKeySamples.getValue()) {
        // per-doc scores clipped at 0
        scoreAggregator += Math.max(fMeasureCounts.truePositives()
            - alpha()*fMeasureCounts.falsePositives(), 0);
        normalizer += fMeasureCounts.truePositives() + fMeasureCounts.falseNegatives();
      }
      double normalizedScore = 100.0*scoreAggregator/normalizer;
      linearScoresB.put(breakdownKeySamples.getKey(), normalizedScore);
    }
  }

  @Override
  public void finish() throws IOException {
    writer.writeBootstrapData(name(),
        ImmutableMap.of(LINEAR_SCORE, linearScoresB.build()),
        outputDir());
  }

  public static class Builder extends ImmutableBrokenDownLinearScoreAggregator.Builder {}
}

