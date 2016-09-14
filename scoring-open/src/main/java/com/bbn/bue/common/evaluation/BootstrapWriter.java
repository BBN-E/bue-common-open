package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.OptionalUtils;
import com.bbn.bue.common.TextGroupImmutable;
import com.bbn.bue.common.collections.MapUtils;
import com.bbn.bue.common.math.PercentileComputer;

import com.google.common.base.Charsets;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.primitives.Doubles;

import org.immutables.value.Value;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Writes files describing some scoring metric which has been bootstrapped.
 */
@TextGroupImmutable
@Value.Immutable
abstract class BootstrapWriter {
  /**
   * The names of the scoring measures to output. For example, {@code ["Precision", "Recall", "F1"}.
   * In charts, the order given will be preserved.
   */
  @Value.Parameter
  public abstract ImmutableList<String> measures();

  /**
   * What percentiles of the bootstrapped scores to print.
   */
  @Value.Default
  public ImmutableList<Double> percentilesToPrint() {
    return ImmutableList.of(0.025, 0.05, 0.25, 0.5, 0.75, 0.95, 0.975);
  }

  /**
   * Which of the multiple ways of calculating percentiles to use. Defaults to
   * {@link PercentileComputer#nistPercentileComputer()}
   */
  @Value.Default
  public PercentileComputer percentileComputer() {
    return PercentileComputer.nistPercentileComputer();
  }

  @Value.Check
  protected void check() {
    for (final Double percentileToPrint : percentilesToPrint()) {
      checkArgument(percentileToPrint > 0.0 && percentileToPrint < 1.0,
          "Invalid percentile %s", percentileToPrint);
    }
  }

  /**
   * Given bootstrapping samples, writes the scores to multiple files in {@code outputDir}.
   * @param name The name to give to this score output. E.g. "ByType" for bootstrapped scores
   *             broken down by event type
   * @param measuresToBreakdownsToStats The bootstrap samples for scores.  The outermost key
   *                                    is the scoring measure. These keys must be a subset of
   *                                    those in {@link #measures()}.  The inner key should be the
   *                                    different groups for whatever score breakdown we are doing
   *                                    (e.g. event types). If no score breakdown is being used,
   *                                    use some dummy key, like "Aggregate".  The innermost values
   *                                    should be a list of bootstrap samples for that measure/
   *                                    breakdown key combination.
   * @param outputDir The directory to write the output to.  Human readable bootstrap confidence
   *                  intervals will be written to {@code outputDir/name.bootstrapped.txt}. Raw
   *                  samples will be written to {@code outputDir/name.bootstrapped.raw}. Medians
   *                  will be written as a {@code .csv} to {@code outputDir/name.bootstrapped.csv}.
   * @throws IOException
   */
  public void writeBootstrapData(String name,
      ImmutableMap<String, ImmutableListMultimap<String, Double>> measuresToBreakdownsToStats,
      File outputDir) throws IOException {
    final StringBuilder chart = new StringBuilder();
    final StringBuilder delim = new StringBuilder();
    final StringBuilder mediansDelim = new StringBuilder();

    final ImmutableSet<String> breakdownKeys =
        MapUtils.allMultimapKeys(measuresToBreakdownsToStats.values());

    // Set up chart title, delimited file headers
    chart.append(name).append("\n\n");
    addDelimPercentileHeader(name, delim);
    addDelimMediansHeader(name, measures(), mediansDelim);

    final ImmutableMap.Builder<String, ImmutableMap<String, ImmutableList<Double>>> samples =
        ImmutableMap.builder();

    // all four multimaps have the same keyset
    for (final String breakdownKey : breakdownKeys) {
      final ImmutableMap.Builder<String, Double> mediansMapBuilder =
          ImmutableMap.builder();

      final ImmutableMap.Builder<String, PercentileComputer.Percentiles> percentileMapB = ImmutableMap.builder();
      final ImmutableMap.Builder<String, ImmutableList<Double>> keySamples =
          ImmutableMap.builder();

      for (final Map.Entry<String, ImmutableListMultimap<String, Double>> e : measuresToBreakdownsToStats
          .entrySet()) {
        final String measureName = e.getKey();
        final ImmutableList<Double> samplesForBreakdownKey = e.getValue().get(breakdownKey);
        final PercentileComputer.Percentiles percentiles =
            percentileComputer().calculatePercentilesAdoptingData(
                Doubles.toArray(samplesForBreakdownKey));
        percentileMapB.put(measureName, percentiles);

        // Raw samples
        keySamples.put(measureName, samplesForBreakdownKey);

        // Aggregate medians
        mediansMapBuilder.put(measureName, percentiles.median().or(Double.NaN));
      }

      // Write to chart
      final ImmutableMap<String, PercentileComputer.Percentiles> percentilesMap = percentileMapB.build();
      dumpPercentilesForMetric(breakdownKey, percentilesMap, chart);
      chart.append("\n");

      // Write to delim
      addDelimPercentilesForMetric(breakdownKey, percentilesMap, delim);
      addMediansRow(breakdownKey, mediansMapBuilder.build(), mediansDelim);
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
    final String header = renderLine("Measure", percentilesToPrint());
    output.append(header);
    // Offset length by one since it include a newline
    output.append(Strings.repeat("*", header.length() - 1)).append("\n");
    for (final Map.Entry<String, PercentileComputer.Percentiles> percentileEntry : percentilesByRow
        .entrySet()) {
      output.append(renderLine(percentileEntry.getKey(),
          Lists.transform(percentileEntry.getValue().percentiles(percentilesToPrint()),
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
    header.addAll(renderDoubles(percentilesToPrint()));
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
          percentileEntry.getValue().percentiles(percentilesToPrint()),
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

  public static class Builder extends ImmutableBootstrapWriter.Builder {}
}
