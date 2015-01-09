package com.bbn.nlp.coreference.measures;

import com.bbn.bue.common.collections.CollectionUtils;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Scores coreference clusterings by the BLANC measure.
 * See Marta Recasens Potau.  "Coreference: Theory, Annotation, Resolution, and Evaluation." PhD. Dissertation.
 * University of Barcelona
 * http://stel.ub.edu/cba2010/phd/phd.pdf
 */
public final class BLANCScorer {
    private BLANCScorer() {
    }

    public static BLANCScorer create() {
        return new BLANCScorer();
    }

    /**
     * A BLANC score. This is not represented using {@link com.bbn.bue.common.evaluation.FMeasureInfo} because,
     * although the terms precision, recall, and F-measure are used, each results from averaging other P, R, and Fs,
     * so they don't have the same relationship between themselves you would normally expect.
     */
    public static final class BLANCResult {
        private final double blancP;
        private final double blancR;
        private final double blancF;

        public BLANCResult(double blancP, double blancR, double blancF) {
            checkArgument(blancP >= 0.0 && blancP <= 1.0);
            checkArgument(blancR >= 0.0 && blancR <= 1.0);
            checkArgument(blancF >= 0.0 && blancF <= 1.0);

            this.blancP = blancP;
            this.blancR = blancR;
            this.blancF = blancF;
        }

        public double blancP() {
            return blancP;
        }

        public double blancR() {
            return blancR;
        }

        public double blancF() {
            return blancF;
        }

        private static BLANCResult fromBLANCPRF(double p, double r, double f) {
            return new BLANCResult(p,r,f);
        }
    }

    public BLANCResult score(final Iterable<? extends Iterable<?>> predicted,
                              final Iterable<? extends Iterable<?>> gold)
    {
        final Iterable<Set<Object>> predictedAsSets = CorefScorerUtils.toSets(predicted);
        final Iterable<Set<Object>> goldAsSets = CorefScorerUtils.toSets(gold);

        return scoreSets(predictedAsSets, goldAsSets);
    }

    private BLANCResult scoreSets(final Iterable<Set<Object>> predicted, final Iterable<Set<Object>> gold)
    {

        final Map<Object, Set<Object>> predictedItemToGroup =
                CollectionUtils.makeElementsToContainersMap(predicted);
        final Map<Object, Set<Object>> goldItemToGroup =
                CollectionUtils.makeElementsToContainersMap(gold);

        CorefScorerUtils.checkPartitionsOverSameElements(predictedItemToGroup.keySet(),
                goldItemToGroup.keySet());

        // if this is empty, we know the other is too,
        // by the above
        if (predictedItemToGroup.isEmpty()) {
            return new BLANCResult(0.0, 0.0, 0.0);
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
            final int goldPredictedIntersectionSize = Sets.intersection(predictedCluster, goldCluster).size();
            rc += goldPredictedIntersectionSize - 1;
            // the number of links claimed by the system but not gold (wc)
            // is the size of the difference between the system and gold cluster
            wc += Sets.difference(predictedCluster, goldCluster).size();
            // the number of links claimed by gold but not the system (wn)
            // is the size of the difference between the gold and system cluster
            wn += Sets.difference(goldCluster, predictedCluster).size();
            // the number of coref non-coreference links is the number of items minus the items in the union of
            // the gold and predicted clusters
            final int goldPredictedUnionSize = predictedCluster.size() + goldCluster.size() - goldPredictedIntersectionSize;
            rn += allItems.size() - goldPredictedUnionSize;
        }

        // each link got counted twice above but it doesn't matter
        final double P_C = nanToZero(((double)rc)/(rc+wc));
        final double P_N = nanToZero(((double)rn)/(rn+wn));
        final double R_C = nanToZero(((double)rc)/(rc+wn));
        final double R_N = nanToZero(((double)rn)/(rn+wc));
        final double F_C = nanToZero(2.0*P_C*R_C/(P_C+R_C));
        final double F_N = nanToZero(2.0*P_N*R_N/(P_N+R_N));

        return BLANCResult.fromBLANCPRF(0.5*(P_C+P_N), 0.5*(R_C+R_N), 0.5*(F_C+F_N));
    }

    private static double nanToZero(double x) {
        return Double.isNaN(x)?0.0:x;
    }
}
