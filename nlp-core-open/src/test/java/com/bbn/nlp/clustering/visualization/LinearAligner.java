package com.bbn.nlp.clustering.visualization;

import com.bbn.bue.common.primitives.DoubleUtils;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.concat;

/**
 * Given two sets of sets of objects and an object-level alignment, places them in order so that the
 * divergence in position between elements is minimized.
 *
 * Note this aligner assumes aligned base items are the same by .hashCode and .equals(). If this is
 * not true, simply define an equivalence relation for your alignment and wrap each item in an
 * {@link com.google.common.base.Equivalence.Wrapper}.
 *
 * This class is experimental and subject to backwards-incompatible changes. Use at your own risk. ~
 * rgabbard
 */
@Beta
public final class LinearAligner<ItemType> {

  private static final Logger log = LoggerFactory.getLogger(LinearAligner.class);

  private final ObjectiveFunction<ItemType> objectiveFunction;
  private final boolean insertGaps;

  private LinearAligner(ObjectiveFunction<ItemType> objectiveFunction, boolean allowGaps) {
    this.objectiveFunction = checkNotNull(objectiveFunction);
    this.insertGaps = allowGaps;
  }

  public static <ItemType> LinearAligner<ItemType> createWithLinearPenalty(boolean allowGaps) {
    return new LinearAligner<ItemType>(new LinearDistance<ItemType>(), allowGaps);
  }

  private static final double TOLERANCE = 1e-3;

  public Result<ItemType> align(Iterable<? extends Iterable<ItemType>> leftClusters,
      Iterable<? extends Iterable<ItemType>> rightClusters,
      int iterations)

  {
    final MutableAlignment<ItemType> mutableAlignment =
        MutableAlignment.create(leftClusters, rightClusters, insertGaps);

    for (int i = 0; i < iterations; ++i) {
      double preIt = mutableAlignment.score(objectiveFunction);
      mutableAlignment.doBestSwap(
          MutableAlignment.Side.Left, objectiveFunction);
      mutableAlignment.doBestSwap(
          MutableAlignment.Side.Right, objectiveFunction);
      double val = mutableAlignment.score(objectiveFunction);
      if (DoubleUtils.withinEpsilonOf(val, 0, TOLERANCE)
          || DoubleUtils.withinEpsilonOf(val, preIt, TOLERANCE)) {
        break;
      }
    }
    return mutableAlignment.asResult();
  }


  public static final class Result<ItemType> {

    private final ImmutableList<ClusterOrdering<ItemType>> left;
    private final ImmutableList<ClusterOrdering<ItemType>> right;

    private Result(Iterable<ClusterOrdering<ItemType>> left,
        Iterable<ClusterOrdering<ItemType>> right) {
      this.left = ImmutableList.copyOf(left);
      this.right = ImmutableList.copyOf(right);
    }

    public ImmutableList<ClusterOrdering<ItemType>> left() {
      return left;
    }

    public ImmutableList<ClusterOrdering<ItemType>> right() {
      return right;
    }

    @Override
    public String toString() {
      return "LinearAligner.Result(\n\tleft=" + left().toString()
          + "\n\tright=" + right.toString() + ")";
    }
  }

  public static class ClusterOrdering<ItemType> implements Iterable<ItemType> {

    private Iterable<ItemType> originalCollection;
    private List<ItemType> orderedItems;

    public ClusterOrdering(Iterable<ItemType> originalCollection,
        List<ItemType> orderedItems) {
      this.originalCollection = originalCollection;
      this.orderedItems = checkNotNull(orderedItems);
    }

    public static <ItemType> ClusterOrdering<ItemType> createFor(
        Iterable<ItemType> coll) {
      final ArrayList<ItemType> orderedItems = Lists.newArrayList(coll);
      return new ClusterOrdering<ItemType>(coll, orderedItems);
    }

    public static <ItemType> ClusterOrdering<ItemType> createGap() {
      return new ClusterOrdering<ItemType>(null, Lists.<ItemType>newArrayList());
    }

    public boolean isGap() {
      return originalCollection == null;
    }

    @Override
    public String toString() {
      return orderedItems.toString();
    }

    @Override
    public Iterator<ItemType> iterator() {
      return orderedItems.iterator();
    }


  }

  private static interface ObjectiveFunction<ItemType> {

    double score(Multimap<ItemType, Integer> leftPositions,
        Multimap<ItemType, Integer> rightPositions);
  }


  private static class MutableAlignment<ItemType> {

    private enum Side {Left, Right}

    private List<ClusterOrdering<ItemType>> leftOrdering;
    private List<ClusterOrdering<ItemType>> rightOrdering;

    public MutableAlignment(List<ClusterOrdering<ItemType>> leftOrdering,
        List<ClusterOrdering<ItemType>> rightOrdering) {
      this.leftOrdering = leftOrdering;
      this.rightOrdering = rightOrdering;
    }

    public static <ItemType> MutableAlignment<ItemType> create(
        Iterable<? extends Iterable<ItemType>> left,
        Iterable<? extends Iterable<ItemType>> right, boolean insertGaps) {
      int leftGapsToInsert = 0;
      int rightGapsToInsert = 0;

      if (insertGaps) {
        final int leftSize = Iterables.size(concat(left));
        final int rightSize = Iterables.size(concat(right));
        if (leftSize > rightSize) {
          rightGapsToInsert = leftSize - rightSize;
        } else {
          leftGapsToInsert = rightSize - leftSize;
        }
      }

      return new MutableAlignment(listify(left, leftGapsToInsert),
          listify(right, rightGapsToInsert));
    }

    public ImmutableMultimap<ItemType, Integer> leftPositions() {
      return positionMultimap(leftOrdering);
    }

    public ImmutableMultimap<ItemType, Integer> rightPositions() {
      return positionMultimap(rightOrdering);
    }

    private ImmutableMultimap<ItemType, Integer> positionMultimap(
        List<ClusterOrdering<ItemType>> ordering) {
      final ImmutableMultimap.Builder<ItemType, Integer> ret =
          ImmutableMultimap.builder();

      int idx = 0;
      for (final ClusterOrdering<ItemType> clusterOrdering : ordering) {
        if (clusterOrdering.isGap()) {
          ++idx;
        } else {
          for (ItemType item : clusterOrdering.orderedItems) {
            ret.put(item, idx++);
          }
        }
      }

      return ret.build();
    }

    public double score(ObjectiveFunction<ItemType> objectiveFunction) {
      return objectiveFunction.score(leftPositions(), rightPositions());
    }

    public Result<ItemType> asResult() {
      return new Result<ItemType>(leftOrdering, rightOrdering);
    }

    private static final int NO_SWAP = -1;

    public void doBestSwap(Side side, ObjectiveFunction<ItemType> objectiveFunction) {
      int orderingSize = side == Side.Left ? leftOrdering.size()
                                           : rightOrdering.size();

      int best1 = NO_SWAP;
      int best2 = NO_SWAP;
      double bestVal = score(objectiveFunction);

      for (int idx1 = 0; idx1 < orderingSize; ++idx1) {
        for (int idx2 = idx1 + 1; idx2 < orderingSize; ++idx2) {
          // do and score test swap
          swap(side, idx1, idx2);
          double swapObjVal = score(objectiveFunction);
          if (swapObjVal < bestVal) {
            best1 = idx1;
            best2 = idx2;
            bestVal = swapObjVal;
          }
          // undo test swap
          swap(side, idx2, idx1);
        }
      }

      if (best1 != NO_SWAP) {
        swap(side, best1, best2);
      }
    }

    private void swap(Side side, int idx1, int idx2) {
      final List<ClusterOrdering<ItemType>> ordering =
          side == Side.Left ? leftOrdering : rightOrdering;

      Collections.swap(ordering, idx1, idx2);
    }

    private static <ItemType> List<ClusterOrdering<ItemType>> listify(
        Iterable<? extends Iterable<ItemType>> items, int gapsToInsert) {
      final List<ClusterOrdering<ItemType>> ret = Lists.newArrayList();

      for (final Iterable<ItemType> coll : items) {
        ret.add(ClusterOrdering.createFor(coll));
      }

      for (int i = 0; i < gapsToInsert; ++i) {
        ret.add(ClusterOrdering.<ItemType>createGap());
      }

      return ret;
    }
  }

  // Scores an alignment by the linear distance between each item on one side
  // and its closest aligned item on the other.
  private static class LinearDistance<ItemType> implements ObjectiveFunction<ItemType> {

    @Override
    public double score(Multimap<ItemType, Integer> leftPositionMap,
        Multimap<ItemType, Integer> rightPositionMap) {
      double score = 0.0;
      // because things which are unaligned do not contribute to the score,
      // we only need to iterate over one side
      for (final Map.Entry<ItemType, Collection<Integer>> leftPositions
          : leftPositionMap.asMap().entrySet()) {
        final Collection<Integer> rightPositions = rightPositionMap.get(leftPositions.getKey());
        score += bestScore(leftPositions.getValue(), rightPositions)
            + bestScore(rightPositions, leftPositions.getValue());
      }
      return score;
    }

    // Recall an item may appear multiple times on each side.
    // For each position an item occurs in, we find the 'closest'
    // position to *that* position the item occurs in on the other side.
    // The distance between these two positions is contributed to the score.
    // If an item appears on only one side, it makes no contribution to
    // the score because we don't care where unaligned clusters go.
    private double bestScore(Collection<Integer> primaryPositions,
        Collection<Integer> secondaryPositions) {
      if (primaryPositions.isEmpty() || secondaryPositions.isEmpty()) {
        // we don't care where unaligned clusters go, so they don't
        // contribute to the score
        return 0.0;
      }

      double score = 0.0;
      for (final int primaryPos : primaryPositions) {
        int bestDist = Integer.MAX_VALUE;
        for (final int secondaryPos : secondaryPositions) {
          int dist = Math.abs(primaryPos - secondaryPos);
          if (dist < bestDist) {
            bestDist = dist;
          }
        }
        score += (double) bestDist;
      }
      return score;
    }
  }

}
