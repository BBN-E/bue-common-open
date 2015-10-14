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

  // if it's in the map, it must be RightT
  @SuppressWarnings("unchecked")
  @Override
  public Set<LeftT> alignedToRightItem(final Object rightItem) {
    if (rightToLeft.containsKey(rightItem)) {
      return rightToLeft.get((RightT)rightItem);
    } else {
      return ImmutableSet.of();
    }
  }

  // if it's in the map, it must be LeftT
  @SuppressWarnings("unchecked")
  @Override
  public Set<RightT> alignedToLeftItem(final Object leftItem) {
    if (leftToRight.containsKey(leftItem)) {
      return leftToRight.get((LeftT)leftItem);
    } else {
      return ImmutableSet.of();
    }
  }


  @Override
  public Set<LeftT> allLeftItems() {
    return leftItems;
  }

  @Override
  public Set<RightT> allRightItems() {
    return rightItems;
  }

  public static <LeftT, RightT> Builder<LeftT, RightT> builder() {
    return new Builder<LeftT, RightT>();
  }

  public static final class Builder<LeftT, RightT> {
    private final ImmutableSet.Builder<LeftT> allLeftItems = ImmutableSet.builder();
    private final ImmutableSet.Builder<RightT> allRightItems = ImmutableSet.builder();
    private final ImmutableSetMultimap.Builder<LeftT, RightT> leftToRight = ImmutableSetMultimap.builder();
    private final ImmutableSetMultimap.Builder<RightT, LeftT> rightToLeft = ImmutableSetMultimap.builder();

    private Builder() {}

    public Builder addLeftItem(LeftT left) {
      allLeftItems.add(left);
      return this;
    }

    public Builder addRightItem(RightT right) {
      allRightItems.add(right);
      return this;
    }

    public Builder align(LeftT left, RightT right) {
      leftToRight.put(left, right);
      rightToLeft.put(right, left);
      return this;
    }

    public MultimapAlignment<LeftT,RightT> build() {
      return new MultimapAlignment<LeftT, RightT>(allLeftItems.build(), allRightItems.build(),
          leftToRight.build(), rightToLeft.build());
    }
  }
}
