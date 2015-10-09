package com.bbn.bue.common.evaluation;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A default implementation for {@link Alignment}.
 */
@Beta
public final class MultimapAlignment<LeftT, RightT> implements Alignment<LeftT, RightT> {

  final ImmutableSet<LeftT> leftItems;
  final ImmutableSet<RightT> rightItems;
  final ImmutableSetMultimap<LeftT, RightT> leftToRight;
  final ImmutableSetMultimap<RightT, LeftT> rightToLeft;

  private MultimapAlignment(final Iterable<? extends LeftT> leftItems,
      final Iterable<? extends RightT> rightItems,
      final Multimap<? extends LeftT, ? extends RightT> leftToRight,
      final Multimap<? extends RightT, ? extends LeftT> rightToLeft) {
    this.leftItems = ImmutableSet.copyOf(leftItems);
    this.rightItems = ImmutableSet.copyOf(rightItems);
    this.leftToRight = ImmutableSetMultimap.copyOf(leftToRight);
    this.rightToLeft = ImmutableSetMultimap.copyOf(rightToLeft);
    checkArgument(this.leftItems.containsAll(leftToRight.keySet()));
    checkArgument(this.rightItems.containsAll(rightToLeft.keySet()));
  }

  /**
   * Creates an {@link Alignment} from {@link Multimap}s.
   *
   * @param leftItems   all items in the left alignment set.
   * @param rightItems  all items in the right alignment set.
   * @param leftToRight the mapping from left items to their right partners.
   * @param rightToLeft the mapping from right items to their left partners.
   */
  public static <LeftT, RightT> Alignment<LeftT, RightT> create(
      final Iterable<? extends LeftT> leftItems,
      final Iterable<? extends RightT> rightItems,
      final Multimap<? extends LeftT, ? extends RightT> leftToRight,
      final Multimap<? extends RightT, ? extends LeftT> rightToLeft) {
    return new MultimapAlignment<LeftT, RightT>(leftItems, rightItems, leftToRight, rightToLeft);
  }

  @Override
  public Set<LeftT> leftUnaligned() {
    return Sets.difference(leftItems, leftToRight.keySet());
  }

  @Override
  public Set<RightT> rightUnaligned() {
    return Sets.difference(rightItems, rightToLeft.keySet());
  }

  @Override
  public Set<LeftT> leftAligned() {
    return leftToRight.keySet();
  }

  @Override
  public Set<RightT> rightAligned() {
    return rightToLeft.keySet();
  }

  @Override
  public Set<LeftT> alignedToRightItem(final RightT rightItem) {
    return rightToLeft.get(rightItem);
  }

  @Override
  public Set<RightT> alignedToLeftItem(final LeftT leftItem) {
    return leftToRight.get(leftItem);
  }
}
