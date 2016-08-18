package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.OptionalUtils;
import com.bbn.bue.common.math.PercentileComputer;

import com.google.common.base.Charsets;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.io.Files;
import com.google.common.primitives.Doubles;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
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

  private final PercentileComputer percentileComputer = PercentileComputer.nistPercentileComputer();

  private static final ImmutableList<Double> PERCENTILES_TO_PRINT =
      ImmutableList.of(0.005, 0.025, 0.05, 0.25, 0.5, 0.75, 0.95, 0.975, 0.995);
  private static final ImmutableList<String> MEASURES = ImmutableList.of(
      "F1",
      "Precision",
      "Recall",
      "Accuracy"
  );

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
    final StringBuilder chart = new StringBuilder();
    final StringBuilder delim = new StringBuilder();
    final StringBuilder mediansDelim = new StringBuilder();
    final ImmutableListMultimap<String, Double> f1s = f1sB.build();
    final ImmutableListMultimap<String, Double> precisions = precisionsB.build();
    final ImmutableListMultimap<String, Double> recalls = recallsB.build();
    final ImmutableListMultimap<String, Double> accuracies = accuraciesB.build();

    // Set up chart title
    chart.append(name).append("\n\n");

    // Set up delim headers
    addDelimPercentileHeader(name, delim);
    addDelimMediansHeader(name, MEASURES, mediansDelim);

    // Set up sample storage
    final ImmutableMap.Builder<String, ImmutableMap<String, ImmutableList<Double>>> samples =
        ImmutableMap.builder();

    // all four multimaps have the same keyset
    for (final String key : f1s.keySet()) {
      final ImmutableMap<String, PercentileComputer.Percentiles> percentileMap =
          ImmutableMap.of(
              "F1", percentileComputer
                  .calculatePercentilesAdoptingData(Doubles.toArray(f1s.get(key))),
              "Precision",
              percentileComputer
                  .calculatePercentilesAdoptingData(Doubles.toArray(precisions.get(key))),
              "Recall",
              percentileComputer
                  .calculatePercentilesAdoptingData(Doubles.toArray(recalls.get(key))),
              "Accuracy",
              percentileComputer
                  .calculatePercentilesAdoptingData(Doubles.toArray(accuracies.get(key))));

      // Raw samples
      final ImmutableMap<String, ImmutableList<Double>> keySamples =
          ImmutableMap.of(
              "F1", f1s.get(key),
              "Precision", precisions.get(key),
              "Recall", recalls.get(key),
              "Accuracy", accuracies.get(key));
      samples.put(key, keySamples);

      // Aggregate medians
      final ImmutableMap.Builder<String, Double> mediansMapBuilder =
          ImmutableMap.builder();
      for (final Map.Entry<String, PercentileComputer.Percentiles> percentileEntry :
          percentileMap.entrySet()) {
        final Optional<Double> optPercentile = percentileEntry.getValue().median();
        mediansMapBuilder.put(percentileEntry.getKey(), optPercentile.or(Double.NaN));
      }

      // Write to chart
      dumpPercentilesForMetric(key, percentileMap, chart);
      chart.append("\n");

      // Write to delim
      addDelimPercentilesForMetric(key, percentileMap, delim);
      addMediansRow(key, mediansMapBuilder.build(), mediansDelim);
    }

    // Make output dir as needed
    outputDir.mkdir();

    // Write chart
    Files.asCharSink(new File(outputDir, name + ".bootstrapped.txt"),
        Charsets.UTF_8).write(chart.toString());
    // Write delim
    Files.asCharSink(new File(outputDir, name + ".bootstrapped.csv"),
        Charsets.UTF_8).write(delim.toString());
    // Write means-only delimited
    Files.asCharSink(new File(outputDir, name + ".bootstrapped.medians.csv"),
        Charsets.UTF_8).write(mediansDelim.toString());
    // Write raw data
    Files.asCharSink(new File(outputDir, name + ".bootstrapped.raw"),
        Charsets.UTF_8).write(renderSamples(samples.build()));
  }

  private void dumpPercentilesForMetric(String chartTitle,
      ImmutableMap<String, PercentileComputer.Percentiles> percentilesByRow,
      StringBuilder output) {
    output.append(chartTitle).append("\n");
    final String header = renderLine("Measure", PERCENTILES_TO_PRINT);
    output.append(header);
    // Offset length by one since it include a newline
    output.append(Strings.repeat("*", header.length() - 1)).append("\n");
    for (final Map.Entry<String, PercentileComputer.Percentiles> percentileEntry : percentilesByRow
        .entrySet()) {
      output.append(renderLine(percentileEntry.getKey(),
          Lists.transform(percentileEntry.getValue().percentiles(PERCENTILES_TO_PRINT),
              OptionalUtils.deoptionalizeFunction(Double.NaN))));
    }
    output.append("\n");
  }

  private void addDelimMediansHeader(final String name, final List<String> measures,
      final StringBuilder output) {
    final ImmutableList.Builder<String> header = ImmutableList.builder();
    header.add(name);
    header.addAll(measures);
    renderCells(header.build(), output);
  }

  private void addMediansRow(final String name, Map<String, Double> measures,
      final StringBuilder output) {
    final ImmutableList.Builder<String> row = ImmutableList.builder();
    row.add(name);
    final ImmutableList.Builder<Double> scores = ImmutableList.builder();
    for (Map.Entry<String, Double> measure : measures.entrySet()) {
      scores.add(measure.getValue());
    }
    row.addAll(renderDoubles(scores.build()));
    renderCells(row.build(), output);
  }

  private void addDelimPercentileHeader(final String name, final StringBuilder output) {
    final ImmutableList.Builder<String> header = ImmutableList.builder();
    header.add(name);
    header.add("Measure");
    header.addAll(renderDoubles(PERCENTILES_TO_PRINT));
    renderCells(header.build(), output);
  }

  private void addDelimPercentilesForMetric(
      final String key,
      final ImmutableMap<String, PercentileComputer.Percentiles> percentilesByRow,
      final StringBuilder output) {
    // Add entries for each row to the builder
    for (final Map.Entry<String, PercentileComputer.Percentiles> percentileEntry : percentilesByRow
        .entrySet()) {
      final ImmutableList.Builder<String> row = ImmutableList.builder();
      row.add(key);
      row.add(percentileEntry.getKey());
      row.addAll(renderDoubles(Lists.transform(
          percentileEntry.getValue().percentiles(PERCENTILES_TO_PRINT),
          OptionalUtils.deoptionalizeFunction(Double.NaN))));
      renderCells(row.build(), output);
    }
  }

  private String renderLine(final String name, final List<Double> values) {
    final StringBuilder ret = new StringBuilder();

    ret.append(String.format("%20s", name));
    for (double val : values) {
      ret.append(String.format("%15.2f", 100.0 * val));
    }
    ret.append("\n");
    return ret.toString();
  }

  private List<String> renderDoubles(final List<Double> values) {
    final ImmutableList.Builder<String> ret = ImmutableList.builder();
    for (final double val : values) {
      ret.add(String.format("%.2f", 100.0 * val));
    }
    return ret.build();
  }

  private void renderCells(final List<String> cells, final StringBuilder builder) {
    Joiner.on(",").appendTo(builder, cells);
    builder.append("\n");
  }

  private String renderSamples(
      final ImmutableMap<String, ImmutableMap<String, ImmutableList<Double>>> samples) {
    final StringBuilder ret = new StringBuilder();
    for (final Map.Entry<String, ImmutableMap<String, ImmutableList<Double>>> entry :
        samples.entrySet()) {
      final String key = entry.getKey();
      final ImmutableMap<String, ImmutableList<Double>> keySamples = entry.getValue();
      for (final Map.Entry<String, ImmutableList<Double>> innerEntry: keySamples.entrySet()) {
        final ImmutableList.Builder<String> row = ImmutableList.builder();
        row.add(key);
        row.add(innerEntry.getKey());
        row.addAll(Iterables.transform(innerEntry.getValue(), Functions.toStringFunction()));
        renderCells(row.build(),ret);
      }
    }
    return ret.toString();
  }
}
