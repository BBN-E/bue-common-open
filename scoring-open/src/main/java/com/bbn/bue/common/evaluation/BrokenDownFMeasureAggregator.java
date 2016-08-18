package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.math.PercentileComputer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimaps;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * If we have some metric broken down by categories which results in F-measure counts, these
 * will print bootstrapped precision/recall/F-measure scores.
 *
 * Each of these is provided with an output directory (<tt>outputDir</tt>) and a name. The
 * following outputs will be written:
 * <ul>
 *   <li>{@code outputDir/name.bootstrapped.txt}: A human readable chart with the botostrapped
 *   percentiles for precision, recall, F1, and accuracy.</li>
 *   <li>{@code outputDir/name.bootstrapped.csv}: the same information in a machine-friendly
 *   CSV form.</li>
 *   <li>{@code outputDir/name.bootstrapped.medians.csv}: CSV for the median bootstrapped
 *   values only.</li>
 *   <li>{@code outputDir/name.bootstrapped.raw}: Machine-consumable list of all the
 *   bootstrap samples for all four metrics.  This is useful for passing to gnuplot to
 *   make box and whisker graphs.</li>
 * </ul>
 */
public final class BrokenDownFMeasureAggregator
    implements BootstrapInspector.SummaryAggregator<Map<String, FMeasureCounts>> {
  private final String name;
  private final File outputDir;

  private final ImmutableListMultimap.Builder<String, Double> f1sB = ImmutableListMultimap.builder();
  private final ImmutableListMultimap.Builder<String, Double> precisionsB =
      ImmutableListMultimap.builder();
  private final ImmutableListMultimap.Builder<String, Double> recallsB =
      ImmutableListMultimap.builder();
  private final ImmutableListMultimap.Builder<String, Double> accuraciesB =
      ImmutableListMultimap.builder();

  private static final String F1 = "F1";
  private static final String PRECISION = "Precision";
  private static final String RECALL = "Recall";
  private static final String ACCURACY = "Accuracy";

  private final BootstrapWriter writer = BootstrapWriter.builder()
      .measures(ImmutableList.of(F1, PRECISION, RECALL, ACCURACY))
      .percentilesToPrint(ImmutableList.of(0.005, 0.025, 0.05, 0.25, 0.5, 0.75, 0.95, 0.975, 0.995))
      .percentileComputer(PercentileComputer.nistPercentileComputer())
      .build();

  private BrokenDownFMeasureAggregator(String name, File outputDir) {
    this.name = checkNotNull(name);
    checkArgument(!name.endsWith(Character.toString(File.separatorChar)),
        "Aggregation name cannot end with %s, but got %s", File.separatorChar, name);
    this.outputDir = checkNotNull(outputDir);
  }

  public static BrokenDownFMeasureAggregator create(String name, File outputDir) {
    return new BrokenDownFMeasureAggregator(name, outputDir);
  }

  @Override
  public void observeSample(
      final Collection<Map<String, FMeasureCounts>> observationSummaries) {
    final ImmutableSetMultimap.Builder<String, FMeasureCounts> allCountsB =
        ImmutableSetMultimap.builder();

    for (final Map<String, FMeasureCounts> observationSummary : observationSummaries) {
      allCountsB.putAll(Multimaps.forMap(observationSummary));
    }

    for (final Map.Entry<String, Collection<FMeasureCounts>> aggregate : allCountsB.build().asMap().entrySet()) {
      final FMeasureCounts fMeasureInfo = FMeasureCounts.combineToMicroFMeasure(aggregate.getValue());
      f1sB.put(aggregate.getKey(), fMeasureInfo.F1());
      precisionsB.put(aggregate.getKey(), fMeasureInfo.precision());
      recallsB.put(aggregate.getKey(), fMeasureInfo.recall());
    }
  }

  @Override
  public void finish() throws IOException {
    writer.writeBootstrapData(name,
        ImmutableMap.of(
            F1, f1sB.build(),
            PRECISION, precisionsB.build(),
            RECALL, recallsB.build(),
            ACCURACY, accuraciesB.build()
        ),
        outputDir);
  }
}

