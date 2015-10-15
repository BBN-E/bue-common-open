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

  private FMeasureCounts(final float truePositives,
      final float falsePositives, final float falseNegatives) {
    this.truePositives = truePositives;
    this.falsePositives = falsePositives;
    this.falseNegatives = falseNegatives;
  }

  public static FMeasureCounts from(final float truePositives,
      final float falsePositives, final float falseNegatives) {
    return new FMeasureCounts(truePositives, falsePositives, falseNegatives);
  }

  public static FMeasureCounts from(final int truePositives,
      final int falsePositives, final int falseNegatives) {
    return new FMeasureCounts(truePositives, falsePositives, falseNegatives);
  }

  public float truePositives() {
    return truePositives;
  }

  public float falsePositives() {
    return falsePositives;
  }

  public float falseNegatives() {
    return falseNegatives;
  }

  public float numPredicted() {
    return truePositives() + falsePositives();
  }

  @Override
  public float precision() {
    if (truePositives < EPSILON) {
      return 0.0f;
    } else {
      return truePositives / (truePositives + falsePositives);
    }
  }

  @Override
  public float recall() {
    if (truePositives < EPSILON) {
      return 0.0f;
    } else {
      return truePositives / (truePositives + falseNegatives);
    }
  }

  public String compactPrettyString() {
    return String.format("TP: %.2f, FP: %.2f, FN: %.2f; P: %3.2f; R: %3.2f; F: %3.2f",
        truePositives, falsePositives, falseNegatives, 100.0 * precision(), 100.0 * recall(),
        100.0 * F1());
  }

  public static FMeasureCounts combineToMicroFMeasure(final Iterable<FMeasureCounts> infos) {
    float truePositives = 0;
    float falsePositives = 0;
    float falseNegatives = 0;

    for (final FMeasureCounts info : infos) {
      truePositives += info.truePositives();
      falsePositives += info.falsePositives();
      falseNegatives += info.falseNegatives();
    }

    return FMeasureCounts.from(truePositives, falsePositives, falseNegatives);
  }

  private final float truePositives;
  private final float falsePositives;
  private final float falseNegatives;

  private static float EPSILON = 0.000001f;

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
      ret.put(label, new FMeasureCounts(truePositives.count(label),
          falsePositives.count(label), falseNegatives.count(label)));
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
    out.writeFloat(truePositives);
    out.writeFloat(falsePositives);
    out.writeFloat(falseNegatives);
  }

  public static FMeasureCounts readFrom(final DataInputStream in) throws IOException {
    final float truePositives = in.readFloat();
    final float falsePositives = in.readFloat();
    final float falseNegatives = in.readFloat();
    return new FMeasureCounts(truePositives, falsePositives, falseNegatives);
  }

  public static FMeasureCounts microAverage(
      final Iterable<FMeasureCounts> counts) {
    int truePositives = 0;
    int falsePositives = 0;
    int falseNegatives = 0;
    float count = 0.0f;

    for (final FMeasureCounts fcounts : counts) {
      truePositives += fcounts.truePositives();
      falsePositives += fcounts.falsePositives();
      falseNegatives += fcounts.falseNegatives();
      count += 1.0;
    }

    return new FMeasureCounts(truePositives / count,
        falsePositives / count, falseNegatives / count);
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
    return Objects.hashCode(truePositives, falsePositives, falseNegatives);
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
    return Objects.equal(this.truePositives, other.truePositives) && Objects
        .equal(this.falsePositives, other.falsePositives) && Objects
        .equal(this.falseNegatives, other.falseNegatives);
  }

  @Override
  public String toString() {
    return String.format("TP=%.3f;FP=%.3f;FN=%.3f", truePositives, falsePositives, falseNegatives);
  }
}
