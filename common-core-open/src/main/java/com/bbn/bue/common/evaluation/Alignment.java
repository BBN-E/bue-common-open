package com.bbn.bue.common.evaluation;

import com.google.common.annotations.Beta;

import java.util.Collection;
import java.util.Set;

/**
 * Represents a 'pairing up' of two groups of objects of the same type, the {@code key} set and the
 * {@code test} set.  Alignments are intended to be
 * immutable, so it is always safe to case an {@code Alignment<T>} to {@code Alignment<S>} for any
 * super-type of {@code T}.
 *
 * The objects being aligned must have hash codes and equality methods compatible with use in sets
 * and maps.
 *
 * @param <LeftT>  The type of objects in the left set. Covariant.
 * @param <RightT> The type of objects in the right set. Covariant.
 */
@Beta
public interface Alignment<LeftT, RightT> {
  /**
   * Those items in the left set which were not paired with anything in the right.
   */
  Set<LeftT> leftUnaligned();

  /**
   * Those items in the right set which were not paired with anything in the right.
   */
  Set<RightT> rightUnaligned();

  /**
   * Those items in the left set which were paired with something in the right.
   */
  Set<LeftT> leftAligned();

  /**
   * Those items in the right set which were paired with something in the right.
   */
  Set<RightT> rightAligned();

  /**
   * Get all items in the left set, if any, which align to the provided right set item.  If x is in
   * {@code alignedToRightItem(y)}, then y is in {@code alignedToRighItem(x)}.
   */
  Collection<LeftT> alignedToRightItem(Object rightItem);

  /**
   * Get all items in the right set, if any, which align to the provided left set item.  If x is in
   * {@code alignedToRightItem(y)}, then y is in {@code alignedToRighItem(x)}.
   */
  Collection<RightT> alignedToLeftItem(Object leftItem);

  Set<LeftT> allLeftItems();

  Set<RightT> allRightItems();
}

