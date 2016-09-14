package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.TextGroupImmutable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;

import org.immutables.func.Functional;
import org.immutables.value.Value;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

@TextGroupImmutable
@Value.Immutable
@Functional
@JsonSerialize(as=ImmutableFMeasureCounts.class)
@JsonDeserialize(as=ImmutableFMeasureCounts.class)
public abstract class FMeasureCounts extends FMeasureInfo {
  public abstract double falsePositives();
  public abstract double falseNegatives();
  public abstract double numPredicted();
  public abstract double numItemsInKey();

  @Value.Check
  protected void check() {
    checkArgument(falsePositives() >= 0.0);
    checkArgument(falseNegatives() >= 0.0);
    checkArgument(numItemsInKey() >= 0.0);
    checkArgument(numPredicted() >= 0.0);
    checkArgument(falseNegatives() <= numItemsInKey());
    checkArgument(falsePositives() <= numPredicted());
  }

  /**
   * Creates an {@link FMeasureCounts} from counts of true positives, false positives, and false
   * negatives.
   */
  public static FMeasureCounts fromTPFPFN(final double truePositives,
      final double falsePositives, final double falseNegatives) {
    return fromFPFNKeyCountSysCount(falsePositives, falseNegatives,
        truePositives + falseNegatives, truePositives + falsePositives);
  }

  /**
   * Creates an {@link FMeasureCounts} from counts of true positives, false positives, the number of
   * items in the key, and the number of items in the system response.
   */
  public static FMeasureCounts fromFPFNKeyCountSysCount(final double falsePositives,
      final double falseNegatives, final double keyCount, final double sysCount) {
    return new Builder()
      .falsePositives(falsePositives)
      .falseNegatives(falseNegatives)
      .numItemsInKey(keyCount)
      .numPredicted(sysCount).build();
  }

  /**
   * @deprecated Prefer {@link #fromTPFPFN(double, double, double)}.
   */
  @Deprecated
  public static FMeasureCounts from(final double truePositives,
      final double falsePositives, final double falseNegatives) {
    return fromTPFPFN(truePositives, falsePositives, falseNegatives);
  }

  /**
   * @deprecated Prefer {@link #fromTPFPFN(double, double, double)}.
   */
  @Deprecated
  public static FMeasureCounts from(final int truePositives,
      final int falsePositives, final int falseNegatives) {
    return fromTPFPFN(truePositives, falsePositives, falseNegatives);
  }

  // derived accessors

  public final double truePositives() {
    return numPredicted() -falsePositives();
  }

  @Override
  public final double precision() {
    if (numPredicted() < EPSILON) {
      return 0.0;
    } else {
      return 1.0-falsePositives() / numPredicted();
    }
  }

  @Override
  public final double recall() {
    if (numItemsInKey() < EPSILON) {
      return 0.0;
    } else {
      return 1.0 - falseNegatives() / numItemsInKey();
    }
  }

  public final String compactPrettyString() {
    return String.format("TP: %.2f, FP: %.2f, FN: %.2f; P: %3.2f; R: %3.2f; F: %3.2f",
        truePositives(), falsePositives(), falseNegatives(), 100.0 * precision(), 100.0 * recall(),
        100.0 * F1());
  }

  public static FMeasureCounts combineToMicroFMeasure(final Iterable<FMeasureCounts> infos) {
    double falsePositives = 0;
    double falseNegatives = 0;
    double keyCount = 0;
    double sysCount = 0;

    for (final FMeasureCounts info : infos) {
      falsePositives += info.falsePositives();
      falseNegatives += info.falseNegatives();
      keyCount += info.numItemsInKey();
      sysCount += info.numPredicted();
    }

    return FMeasureCounts
        .fromFPFNKeyCountSysCount(falsePositives, falseNegatives, keyCount, sysCount);
  }

  private static final double EPSILON = 0.000001;

  @Beta
  public static <T> Map<T, FMeasureCounts> fromLabels(final List<T> goldLabels,
      final List<T> predictedLabels) {
    checkArgument(goldLabels.size() == predictedLabels.size());

    final Multiset<T> truePositives = HashMultiset.create();
    final Multiset<T> falsePositives = HashMultiset.create();
    final Multiset<T> falseNegatives = HashMultiset.create();

    for (int i = 0; i < goldLabels.size(); ++i) {
      final T gold = goldLabels.get(i);
      final T predicted = predictedLabels.get(i);

      if (gold.equals(predicted)) {
        truePositives.add(gold);
      } else {
        falsePositives.add(predicted);
        falseNegatives.add(gold);
      }
    }

    final Set<T> labels = Sets.newHashSet();
    labels.addAll(truePositives);
    labels.addAll(falsePositives);
    labels.addAll(falseNegatives);

    final ImmutableMap.Builder<T, FMeasureCounts> ret = ImmutableMap.builder();

    for (final T label : labels) {
      ret.put(label, FMeasureCounts.fromFPFNKeyCountSysCount(falsePositives.count(label),
          falseNegatives.count(label), goldLabels.size(), predictedLabels.size()));
    }

    return ret.build();
  }

  public static <T> FMeasureCounts fromHashableItems(final Set<T> predictedPositiveItems,
      final Set<T> allPositiveItems) {
    final int truePositives = Sets.intersection(predictedPositiveItems, allPositiveItems).size();
    final int falsePositives = predictedPositiveItems.size() - truePositives;
    final int falseNegatives = allPositiveItems.size() - truePositives;

    return from(truePositives, falsePositives, falseNegatives);
  }

  public void writeTo(final DataOutputStream out) throws IOException {
    out.writeDouble(falsePositives());
    out.writeDouble(falseNegatives());
    out.writeDouble(numItemsInKey());
    out.writeDouble(numPredicted());
  }

  public static FMeasureCounts readFrom(final DataInputStream in) throws IOException {
    final double falsePositives = in.readDouble();
    final double falseNegatives = in.readDouble();
    final double keyCount = in.readDouble();
    final double sysCount = in.readDouble();

    return FMeasureCounts
        .fromFPFNKeyCountSysCount(falsePositives, falseNegatives, keyCount, sysCount);
  }

  public static Ordering<FMeasureCounts> byF1Ordering() {
    return new Ordering<FMeasureCounts>() {
      @Override
      public int compare(final FMeasureCounts left, final FMeasureCounts right) {
        return Doubles.compare(left.F1(), right.F1());
      }

    };
  }

  @Override
  public String toString() {
    return String.format("TP=%.3f;FP=%.3f;#Key=%.3f;#Sys=%.3F", truePositives(), falsePositives(),
        numItemsInKey(), numPredicted());
  }

  public static class Builder extends ImmutableFMeasureCounts.Builder {}
}
