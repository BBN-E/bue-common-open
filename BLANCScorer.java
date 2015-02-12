package com.bbn.nlp.coreference.measures;

import com.bbn.bue.common.collections.CollectionUtils;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Scores coreference clusterings by the BLANC measure. See Marta Recasens Potau.  "Coreference:
 * Theory, Annotation, Resolution, and Evaluation." PhD. Dissertation. University of Barcelona
 * http://stel.ub.edu/cba2010/phd/phd.pdf
 */
public final class BLANCScorer {

  private BLANCScorer() {
  }

  public static BLANCScorer create() {
    return new BLANCScorer();
  }

  /**
   * A BLANC score. This is not represented using {@link com.bbn.bue.common.evaluation.FMeasureInfo}
   * because, although the terms precision, recall, and F-measure are used, each results from
   * averaging other P, R, and Fs, so they don't have the same relationship between themselves you
   * would normally expect.
   */
  public static final class BLANCResult {

    /**
     * The actual data
     */
    // number of pairwise coreference links agreed on by system and gold
    private final double rc;
    // number of pairwise non-coreference links agreed on by system and gold
    private final double rn;
    // number of pairwise links claimed by gold but not system
    private final double wn;
    // number of pairwise links claimed by system but not gold
    private final double wc;

    /**
     * cached computations derived from the data above
     */
    // precision of coref links
    private final double P_C;
    // precision of non-coref links
    private final double P_N;
    // recall of coref links
    private final double R_C;
    // recall of non-coref links
    private final double R_N;
    // F1 of coref links
    private final double F_C;
    // F1 of non-coref links
    private final double F_N;

    private final double blancP;
    private final double blancR;
    private final double blancF;

    private BLANCResult(double rc, double rn, double wn, double wc) {
      checkArgument(rc >= 0.0);
      checkArgument(rn >= 0.0);
      checkArgument(wc >= 0.0);
      checkArgument(wn >= 0.0);

      this.rc = rc;
      this.rn = rn;
      this.wn = wn;
      this.wc = wc;

      // compute and cache derived scores
      // TODO: nan-to-zero may not be the correct thing here in all cases.
      // consider a fully unlinked answer key: R_C has denominator zero,
      // but we don't want to penalize systems for this.  Possibly we
      // can fall back to only using the other value. But then we have sharp
      // transitions between 0 links and 1 link. This bears careful thought.
      this.P_C = nanToZero(rc / (rc + wc));
      this.P_N = nanToZero(rn / (rn + wn));
      this.R_C = nanToZero(rc / (rc + wn));
      this.R_N = nanToZero(rn / (rn + wc));
      this.F_C = nanToZero(2.0 * P_C * R_C / (P_C + R_C));
      this.F_N = nanToZero(2.0 * P_N * R_N / (P_N + R_N));

      this.blancP = 0.5 * P_C + 0.5 * P_N;
      this.blancR = 0.5 * R_C + 0.5 * R_N;
      this.blancF = 0.5 * F_C + 0.5 * F_N;
    }

    public double numCorrectCorefLinks() {
      return rc;
    }

    public double numCorrectNonCorefLinks() {
      return rn;
    }

    public double numMissedNonCorefLinks() {
      return wn;
    }

    public double numMissedCorefLinks() {
      return wc;
    }

    public double corefLinkPrecision() {
      return P_C;
    }

    public double nonCorefLinkPrecision() {
      return P_N;
    }

    public double corefLinkRecall() {
      return R_C;
    }

    public double nonCorefLinkRecall() {
      return R_N;
    }

    public double corefLinkF1() {
      return F_C;
    }

    public double nonCorefLinkF1() {
      return F_N;
    }

    public double blancPrecision() {
      return blancP;
    }

    public double blancRecall() {
      return blancR;
    }

    public double blancF1() {
      return blancF;
    }
  }

  public Optional<BLANCResult> score(final Iterable<? extends Iterable<?>> predicted,
      final Iterable<? extends Iterable<?>> gold) {
    final Iterable<Set<Object>> predictedAsSets = CorefScorerUtils.toSets(predicted);
    final Iterable<Set<Object>> goldAsSets = CorefScorerUtils.toSets(gold);

    return scoreSets(predictedAsSets, goldAsSets);
  }

  private Optional<BLANCResult> scoreSets(final Iterable<Set<Object>> predicted,
      final Iterable<Set<Object>> gold) {

    final Map<Object, Set<Object>> predictedItemToGroup =
        CollectionUtils.makeElementsToContainersMap(predicted);
    final Map<Object, Set<Object>> goldItemToGroup =
        CollectionUtils.makeElementsToContainersMap(gold);

    CorefScorerUtils.checkPartitionsOverSameElements(predictedItemToGroup.keySet(),
        goldItemToGroup.keySet());

    // if this is empty, we know the other is too,
    // by the above
    if (predictedItemToGroup.isEmpty()) {
      // result is undefined
      return Optional.absent();
    }

    // number of pairwise coreference links agreed on by system and gold
    int rc = 0;
    // number of pairwise non-coreference links agreed on by system and gold
    int rn = 0;
    // number of pairwise links claimed by gold but not system
    int wn = 0;
    // number of pairwise links claimed by system but not gold
    int wc = 0;

    final Set<Object> allItems = predictedItemToGroup.keySet();
    for (final Object item : allItems) {
      final Set<Object> predictedCluster = predictedItemToGroup.get(item);
      final Set<Object> goldCluster = goldItemToGroup.get(item);

      // the number of correct coref links for this item is the size of the intersection
      // of the gold and predicted clusters, minus this item itself.
      final int goldPredictedIntersectionSize =
          Sets.intersection(predictedCluster, goldCluster).size();
      rc += goldPredictedIntersectionSize - 1;
      // the number of links claimed by the system but not gold (wc)
      // is the size of the difference between the system and gold cluster
      wc += Sets.difference(predictedCluster, goldCluster).size();
      // the number of links claimed by gold but not the system (wn)
      // is the size of the difference between the gold and system cluster
      wn += Sets.difference(goldCluster, predictedCluster).size();
      // the number of coref non-coreference links is the number of items minus the items in the union of
      // the gold and predicted clusters
      final int goldPredictedUnionSize =
          predictedCluster.size() + goldCluster.size() - goldPredictedIntersectionSize;
      rn += allItems.size() - goldPredictedUnionSize;
    }

    // all the links got counted twice above, so we divide by 2
    return Optional.of(new BLANCResult(rc / 2.0, rn / 2.0, wn / 2.0, wc / 2.0));
  }

  private static double nanToZero(double x) {
    return Double.isNaN(x) ? 0.0 : x;
  }
}
