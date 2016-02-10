package com.bbn.bue.common.evaluation;

import com.google.common.annotations.Beta;
import com.google.common.base.Objects;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

public final class FMeasureCounts extends FMeasureInfo {
  private FMeasureCounts(final double falsePositives,
      final double falseNegatives, final double keyCount, final double sysCount) {
    checkArgument(falsePositives >= 0.0f);
    this.falsePositives = falsePositives;
    checkArgument(falseNegatives >= 0.0f);
    this.falseNegatives = falseNegatives;
    checkArgument(keyCount >= 0.0f);
    this.keyCount = keyCount;
    checkArgument(sysCount >= 0.0f);
    this.systemCount = sysCount;
    checkArgument(falseNegatives <= keyCount);
    checkArgument(falsePositives <= sysCount);
  }

  /**
   * Creates an {@link FMeasureCounts} from counts of true positives, false positives, and false
   * negatives.
   */
  public static FMeasureCounts fromTPFPFN(final double truePositives,
      final double falsePositives, final double falseNegatives) {
    return fromFPFNKeyCountSysCount(falsePositives, falseNegatives,
        truePositives + falseNegatives, truePositives + falseNegatives);
  }

  /**
   * Creates an {@link FMeasureCounts} from counts of true positives, false positives, the number of
   * items in the key, and the number of items in the system response.
   */
  public static FMeasureCounts fromFPFNKeyCountSysCount(final double falsePositives,
      final double falseNegatives, final double keyCount, final double sysCount) {
    return new FMeasureCounts(falsePositives, falseNegatives, keyCount, sysCount);
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

  public double truePositives() {
    return systemCount-falsePositives;
  }

  public double falsePositives() {
    return falsePositives;
  }

  public double falseNegatives() {
    return falseNegatives;
  }

  public double numPredicted() {
    return systemCount;
  }

  public double numItemsInKey() {
    return keyCount;
  }

  @Override
  public double precision() {
    if (systemCount < EPSILON) {
      return 0.0;
    } else {
      return 1.0-falsePositives / systemCount;
    }
  }

  @Override
  public double recall() {
    if (keyCount < EPSILON) {
      return 0.0;
    } else {
      return 1.0 - falseNegatives / keyCount;
    }
  }

  public String compactPrettyString() {
    return String.format("TP: %.2f, FP: %.2f, FN: %.2f; P: %3.2f; R: %3.2f; F: %3.2f",
        truePositives(), falsePositives, falseNegatives, 100.0 * precision(), 100.0 * recall(),
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
      keyCount += info.keyCount;
      sysCount += info.systemCount;
    }

    return FMeasureCounts
        .fromFPFNKeyCountSysCount(falsePositives, falseNegatives, keyCount, sysCount);
  }

  private final double falsePositives;
  private final double falseNegatives;
  private final double keyCount;
  private final double systemCount;

  private static double EPSILON = 0.000001;

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
    out.writeDouble(falsePositives);
    out.writeDouble(falseNegatives);
    out.writeDouble(keyCount);
    out.writeDouble(systemCount);
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
  public int hashCode() {
    return Objects.hashCode(falsePositives, falseNegatives, keyCount, systemCount);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final FMeasureCounts other = (FMeasureCounts) obj;
    return Objects.equal(this.falsePositives, other.falsePositives)
        && Objects.equal(this.falseNegatives, other.falseNegatives)
        && Objects.equal(this.keyCount, other.keyCount)
        && Objects.equal(this.systemCount, other.systemCount);
  }

  @Override
  public String toString() {
    return String.format("TP=%.3f;FP=%.3f;#Key=%.3f;#Sys=%.3F", truePositives(), falsePositives,
        keyCount, systemCount);
  }
}
