package com.bbn.bue.common.evaluation;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.Sets.difference;

/**
 * A {@link ProvenancedAlignment} based on grouping items into equivalence classes. The items
 * aligned are the equivalence classes and the provenances are the original items.
 */
@Beta
public final class EquivalenceBasedProvenancedAlignment<EqClassT, LeftT, RightT>
    implements ProvenancedAlignment<EqClassT, LeftT, EqClassT, RightT> {

  private final ImmutableMultimap<EqClassT, LeftT> leftEquivalenceClassesToProvenances;
  private final ImmutableMultimap<EqClassT, RightT> rightEquivalenceClassesToProvenances;

  private EquivalenceBasedProvenancedAlignment(
      final Multimap<? extends EqClassT, ? extends LeftT> leftEquivalenceClassesToProvenances,
      final Multimap<? extends EqClassT, ? extends RightT> rightEquivalenceClassesToProvenances) {
    this.leftEquivalenceClassesToProvenances = ImmutableSetMultimap
        .copyOf(leftEquivalenceClassesToProvenances);
    this.rightEquivalenceClassesToProvenances = ImmutableSetMultimap.copyOf(
        rightEquivalenceClassesToProvenances);
  }

  // package-private
  static <EqClassT, LeftProvT, RightProvT> EquivalenceBasedProvenancedAlignment<EqClassT, LeftProvT, RightProvT> fromEquivalenceClassMaps(
      final Multimap<? extends EqClassT, ? extends LeftProvT> leftEquivalenceClassesToProvenances,
      final Multimap<? extends EqClassT, ? extends RightProvT> rightEquivalenceClassesToProvenances) {
    return new EquivalenceBasedProvenancedAlignment<EqClassT, LeftProvT, RightProvT>(
        leftEquivalenceClassesToProvenances, rightEquivalenceClassesToProvenances);
  }

  @Override
  public Collection<LeftT> provenancesForLeftItem(final EqClassT item) {
    return leftEquivalenceClassesToProvenances.get(item);
  }

  @Override
  public Collection<RightT> provenancesForRightItem(final EqClassT item) {
    return rightEquivalenceClassesToProvenances.get(item);
  }

  @Override
  public Set<EqClassT> leftUnaligned() {
    return difference(leftEquivalenceClassesToProvenances.keySet(),
        rightEquivalenceClassesToProvenances.keySet());
  }

  @Override
  public Set<EqClassT> rightUnaligned() {
    return difference(rightEquivalenceClassesToProvenances.keySet(),
        leftEquivalenceClassesToProvenances.keySet());
  }

  @Override
  public Set<EqClassT> leftAligned() {
    return Sets.intersection(leftEquivalenceClassesToProvenances.keySet(),
        rightEquivalenceClassesToProvenances.keySet()).immutableCopy();
  }

  @Override
  public Set<EqClassT> rightAligned() {
    return leftAligned();
  }

  // if it appears in the multimap, it's got to be an EqClassT
  @SuppressWarnings("unchecked")
  @Override
  public Collection<EqClassT> alignedToRightItem(final Object rightItem) {
    return getAlignedTo(rightItem);
  }

  // if it appears in the multimap, it's got to be an EqClassT
  @SuppressWarnings("unchecked")
  @Override
  public Collection<EqClassT> alignedToLeftItem(final Object leftItem) {
    return getAlignedTo(leftItem);
  }

  @Override
  public Set<EqClassT> allLeftItems() {
    return leftEquivalenceClassesToProvenances.keySet();
  }

  @Override
  public Set<EqClassT> allRightItems() {
    return rightEquivalenceClassesToProvenances.keySet();
  }

  /**
   * Any equivalence class is by definition aligned to itself if it is present on both the left
   * and the right. Otherwise, it has no alignment.
   */
  private Collection<EqClassT> getAlignedTo(final Object item) {
    if (rightEquivalenceClassesToProvenances.containsKey(item)
        && leftEquivalenceClassesToProvenances.containsKey(item)) {
      return ImmutableList.of((EqClassT) item);
    } else {
      return ImmutableList.of();
    }
  }
}
